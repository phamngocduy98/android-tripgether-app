package cf.bautroixa.maptest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.FireStoreManager;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.theme.OneDialog;
import cf.bautroixa.maptest.theme.OnePromptDialog;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.BatteryHelper;
import cf.bautroixa.maptest.utils.ImageHelper;
import cf.bautroixa.maptest.utils.ShakePhoneHelper;
import cf.bautroixa.maptest.utils.UrlParser;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, MapFragment.OnMapClicked {
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private FireStoreManager manager;
    private SharedPreferences sharedPref;
    private HashMap<String, User> friends;

    private static final int STATE_HIDE = -1;
    private static final int STATE_FRIEND_LIST = 0;
    private static final int STATE_FRIEND_LIST_EXPANDED = 1;
    private static final int STATE_FRIEND_STATUS = 10;
    private static final int STATE_CHECKPOINT = 20;

    int state, lastState = 0;
    String userName;
    User currentUser;
    Trip currentTrip;

    User selectedUser = null;

    // fragment for tab
    FriendListFragment friendListStatusFragment;
    MapFragment mapFragment;
    FriendFragment friendFragment;
    TripOverviewFragment tripOverviewFragment;

    // Views
    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout bottomSpace, bottomSheet;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fabMyLocation;

    // toolbar / status bar
    View statusBar;
    Toolbar toolbar;
    ActionBar actionBar;
    TextView tvSearchbarName, tvTitle;
    Button btnSos, btnQR;
    RoundedImageView imgAvatar;
    LinearLayout containerToolbar;

    // Utils / Helper
    ShakePhoneHelper shakePhoneHelper;
    BatteryHelper batteryHelper;

    public MainActivity() {
        friends = new HashMap<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        userName = sharedPref.getString(User.USER_NAME, User.NO_USER);
        // get
        manager = FireStoreManager.getInstance(userName);
//        onUpdateCurrentUser(manager.getCurrentUser());
        manager.getCurrentUser().addOnNewDocumentSnapshotListener(new Data.OnNewDocumentSnapshotListener<User>() {
            @Override
            public void onNewData(User user) {
                onUpdateCurrentUser(user);
                Log.d(TAG, "update user");
            }
        });

        // bind view
        initStatusBarToolbar();
        fabMyLocation = findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.targetMyLocation();
            }
        });
        bottomSpace = findViewById(R.id.bottom_space);
        bottomSheet = findViewById(R.id.bottom_sheet);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // get fragment
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map);
        friendListStatusFragment = (FriendListFragment) getSupportFragmentManager().findFragmentById(R.id.frag_friend_list);
        tripOverviewFragment = new TripOverviewFragment();

        bottomSheet();

        // lac de diem danh
        shakePhoneHelper = new ShakePhoneHelper(this, new ShakePhoneHelper.OnShakeListener() {
            @Override
            public void onShake() {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        shakePhoneHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shakePhoneHelper.stop();
    }

    @Override
    public void onBackPressed() {
        // this.lastState == STATE_HIDE means that no previous state, or can't back to hide state
        if (this.lastState != STATE_HIDE) {
            handleState(this.lastState);
            this.lastState = STATE_HIDE;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof FriendListFragment) {
            ((FriendListFragment) fragment).setOnFriendItemClickListener(new FriendListFragment.OnFriendItemClickListener() {
                @Override
                public void onClick(User user) {
                    selectedUser = user;
                    handleState(STATE_FRIEND_STATUS);
                }
            });
        } else if (fragment instanceof FriendFragment) {
            ((FriendFragment) fragment).setOnDrawRouteButtonClickedListener(new FriendFragment.OnDrawRouteButtonClickedListener() {
                @Override
                public void onClick(String userId, LatLng latLng) {
                    mapFragment.drawRoute(null, latLng, null);
                }
            });
        } else if (fragment instanceof TripOverviewFragment) {
            ((TripOverviewFragment) fragment).setOnDrawRouteButtonClickedListener(new TripOverviewFragment.OnDrawRouteButtonClickedListener() {
                @Override
                public void onClick(List<LatLng> latLng) {
                    mapFragment.drawRoute(latLng);
                }
            });
            ((TripOverviewFragment) fragment).setOnCheckpointChanged(new TripOverviewFragment.OnActiveCheckpointChanged() {
                @Override
                public void onCheckpointChanged(String checkpointId) {
                    mapFragment.targetCheckpoint(checkpointId);
                }
            });
        } else if (fragment instanceof MapFragment) {
            ((MapFragment) fragment).setOnMapClicked(this);
            ((MapFragment) fragment).setOnMarkerClickedListener(new MapFragment.OnMarkerClickedListener() {
                @Override
                public void onMarkerClick(String type, String id) {
                    Log.d(TAG, "marker click"+type+"id="+id);
                    if (type.equals(Collections.CHECKPOINTS)){
                        mapFragment.targetCheckpoint(id);
                        handleState(STATE_CHECKPOINT);
                        tripOverviewFragment.selectCheckpoint(id);
                    } else if (type.equals(Collections.USERS)){
//                        selectedUser = user;
//                        handleState(STATE_FRIEND_STATUS);
                    }
                }
            });
        }
    }

    private void onUpdateCurrentUser(User user) {
        tvSearchbarName.setText(user.getName());
        if (!user.getAvatar().equals(user.getAvatar())) {
            ImageHelper.loadImage(user.getAvatar(), imgAvatar);
        }
        if (manager.getCurrentTripRef() != null) {
            bottomSheet.setVisibility(View.VISIBLE);
            manager.getCurrentTrip().addOnNewDocumentSnapshotListener(new Data.OnNewDocumentSnapshotListener<Trip>() {
                @Override
                public void onNewData(Trip trip) {
                    tvTitle.setText(trip.getName());
                }
            });
        } else {
            bottomSheet.setVisibility(View.GONE);
        }
    }

    private void handleState(int newState) {
        state = newState;
        switch (state) {
            case STATE_FRIEND_LIST:
            case STATE_FRIEND_LIST_EXPANDED:
                if (state == STATE_FRIEND_LIST) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                toggleToolbar(false);
                ViewAnim.toggleHideShow(containerToolbar, true, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(tvTitle, false, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(bottomSheet, true, ViewAnim.DIRECTION_DOWN);
                ViewAnim.toggleHideShow(bottomSpace, false, ViewAnim.DIRECTION_DOWN);
                break;
            case STATE_FRIEND_STATUS:
                toggleToolbar(manager.getCurrentUser().getActiveTrip() != null);
                ViewAnim.toggleHideShow(tvTitle, manager.getCurrentUser().getActiveTrip() != null, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(containerToolbar, false, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(bottomSheet, false, ViewAnim.DIRECTION_DOWN);
                ViewAnim.toggleHideShow(bottomSpace, true, ViewAnim.DIRECTION_DOWN);
                replaceBottomSpace(FriendFragment.newInstance(selectedUser.getId()));
                if (mapFragment != null) mapFragment.targetCamera(true, selectedUser.getLatLng());
                break;
            case STATE_CHECKPOINT:
                toggleToolbar(manager.getCurrentUser().getActiveTrip() != null);
                ViewAnim.toggleHideShow(tvTitle, manager.getCurrentUser().getActiveTrip() != null, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(containerToolbar, false, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(bottomSheet, false, ViewAnim.DIRECTION_DOWN);
                ViewAnim.toggleHideShow(bottomSpace, true, ViewAnim.DIRECTION_DOWN);
                replaceBottomSpace(tripOverviewFragment);
                break;
        }
        Log.d(TAG, "new state= " + state);
    }

    private void replaceBottomSpace(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.bottom_space, fragment);
        ft.commit();
    }

    void toggleToolbar(boolean show) {
        ViewAnim.toggleHideShow(toolbar, show, ViewAnim.DIRECTION_UP);
        ViewAnim.toggleHideShow(statusBar, show, ViewAnim.DIRECTION_UP);
    }


    void bottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from((View) (bottomSheet));
        bottomSheetBehavior.setFitToContents(true);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                friendListStatusFragment.onBottomSheetStateChanged(i);
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                friendListStatusFragment.onSlideBottomSheet(v);
            }
        });
    }

    @Override
    public void onMapClicked(LatLng latLng) {
        Log.d(TAG, "last state= " + lastState);
        if (state != STATE_HIDE) {
            toggleToolbar(false);
            ViewAnim.toggleHideShow(containerToolbar, false, ViewAnim.DIRECTION_UP);
            ViewAnim.toggleHideShow(bottomNavigationView, false, ViewAnim.DIRECTION_DOWN);
            ViewAnim.toggleHideShow(bottomSheet, false, ViewAnim.DIRECTION_DOWN);
            ViewAnim.toggleHideShow(bottomSpace, false, ViewAnim.DIRECTION_DOWN);
            lastState = state;
            state = STATE_HIDE;
        } else {
            ViewAnim.toggleHideShow(bottomNavigationView, true, ViewAnim.DIRECTION_DOWN);
            handleState(lastState);
        }
        Log.d(TAG, "click new state= " + state);
    }

    private void initStatusBarToolbar() {
        // status bar
        statusBar = findViewById(R.id.view_status_bar_activity_main);
        statusBar.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                statusBar.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, insets.getSystemWindowInsetTop()));
                return insets;
            }
        });
        // toolbar
        containerToolbar = findViewById(R.id.container_toolbar_activity_main);
        tvTitle = findViewById(R.id.tv_title_toolbar_activity_main);
        toolbar = findViewById(R.id.toolbar_activity_main);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("");
        tvSearchbarName = findViewById(R.id.tv_trip_name_activity_main);
        btnQR = findViewById(R.id.btn_qr_code_activity_main);
        btnSos = findViewById(R.id.btn_sos_activity_main);
        imgAvatar = findViewById(R.id.img_avatar_activity_main);
        btnQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRScanDialogFragment qrScanDialogFragment = new QRScanDialogFragment(new QRScanDialogFragment.OnQrResultListener() {
                    @Override
                    public void onResult(String result) {
                        String tripCode = UrlParser.parseTripCode(MainActivity.this, result);
                        DocumentReference tripRef = db.collection(Collections.TRIPS).document(tripCode);
                        manager.getCurrentUser().joinTrip(tripRef);
                        tripRef.update(Trip.MEMBERS, FieldValue.arrayUnion(manager.getCurrentUserRef()));
                    }
                });
                qrScanDialogFragment.show(getSupportFragmentManager(), "QR scanner");
            }
        });
        btnSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendSosDialogFragment sosDialogFragment = new SendSosDialogFragment();
                sosDialogFragment.show(getSupportFragmentManager(), "send SOS");
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_location_tab:
                handleState(STATE_FRIEND_LIST);
                break;
            case R.id.nav_trip_tab:
                handleState(STATE_CHECKPOINT);
                break;
            case R.id.nav_bar_setting:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_gather_activity_main:
                OneDialog selectGatherTypeDialog = new OneDialog.Builder().title(R.string.dialog_title_gather).message(R.string.dialog_message_choose_gather_position)
                        .enableNegativeButton(true)
                        .posBtnText(R.string.btn_pos_res_choose_checkpoint)
                        .negBtnText(R.string.btn_neg_current_position)
                        .buttonClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE){
                                    // chọn checkpoint
                                    SelectCheckpointDialog selectCheckpointDialog = new SelectCheckpointDialog();
                                    selectCheckpointDialog.setTitleRes(R.string.dialog_title_select_checkpoint);
                                    selectCheckpointDialog.show(getSupportFragmentManager(), "select cp dialog");
                                } else {
                                    // vị trí hiện tại
                                    OneDialog enterCheckpointNameDialog = new OnePromptDialog.Builder().title(R.string.dialog_title_enter_checkpoint_name)
                                            .onResult(new OnePromptDialog.OnDialogResult() {
                                                @Override
                                                public void onDialogResult(Dialog dialog1, boolean isCanceled, String value) {
                                                    Log.d(TAG, value);
                                                    dialog1.dismiss();
                                                }
                                            }).build();
                                    enterCheckpointNameDialog.show(getSupportFragmentManager(), "enter cp name");
                                }
                            }
                        }).build();
                selectGatherTypeDialog.show(getSupportFragmentManager(), "select gather position");
                return true;
            case R.id.menu_share_activity_main:
                Intent intent = new Intent(MainActivity.this, TripInvitationActivity.class);
                intent.putExtra(Trip.ID, manager.getCurrentUser().getActiveTrip().getId());
                startActivity(intent);
                return true;
            case R.id.menu_leave_trip_activity_main:
                OneDialog leaveTripConfirmDialog = new OneDialog.Builder().message(R.string.dialog_message_leave_trip)
                        .enableNegativeButton(true).buttonClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE){
                                    manager.getCurrentUser().leaveTrip();
                                    dialog.dismiss();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        }).build();
                leaveTripConfirmDialog.show(getSupportFragmentManager(), "leave trip");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
