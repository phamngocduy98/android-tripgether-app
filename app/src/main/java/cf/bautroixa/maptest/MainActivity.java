package cf.bautroixa.maptest;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.theme.OneDialog;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.BatteryHelper;
import cf.bautroixa.maptest.utils.ShakePhoneHelper;
import cf.bautroixa.maptest.utils.UrlParser;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, MapFragment.OnMapClicked {
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private SharedPreferences sharedPref;
    private HashMap<String, User> friends;

    private static final int STATE_HIDE = -1;
    private static final int STATE_FRIEND_LIST = 0;
    private static final int STATE_FRIEND_LIST_EXPANDED = 1;
    private static final int STATE_FRIEND_STATUS = 10;
    private static final int STATE_CHECKPOINT = 20;

    int state, lastState = 0;
    String userName = "notLoggedIn";
    User currentUser;
    Trip currentTrip;
    DocumentReference currentUserRef, currentTripRef;

    User selectedUser = null;

    // fragment for tab
    FriendListStatusFragment friendListStatusFragment;
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
    TextView tvTripName, tvTitle;
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
        userName = sharedPref.getString("userName", userName);
        // get
        currentUserRef = db.collection(Collections.USERS).document(userName);
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    User user = documentSnapshot.toObject(User.class);
                    onUpdateCurrentUser(user);
                }
            }
        });
        // listen changed
        currentUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "error listen changed" + e.getMessage());
                } else {
                    User user = documentSnapshot.toObject(User.class);
                    onUpdateCurrentUser(user);
                }
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
        friendListStatusFragment = (FriendListStatusFragment) getSupportFragmentManager().findFragmentById(R.id.frag_friend_list);
        tripOverviewFragment = new TripOverviewFragment();

        bottomSheet();

        // lac de diem danh
        shakePhoneHelper = new ShakePhoneHelper(this, new ShakePhoneHelper.OnShakeListener() {
            @Override
            public void onShake() {

            }
        });

        // service background
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d(TAG, "emiting status ...");
//                    CurrentUserStatus.getInstance(MainActivity.this).sendStatus();
//                }
//            }
//        }).start();
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
        if (fragment instanceof FriendListStatusFragment) {
            ((FriendListStatusFragment) fragment).setOnFriendItemClickListener(new FriendListStatusFragment.OnFriendItemClickListener() {
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
                public void onClick(LatLng latLng) {
                    mapFragment.drawRoute(null, latLng, null);
                }
            });
            ((TripOverviewFragment) fragment).setOnCheckpointChanged(new TripOverviewFragment.OnCheckpointChanged() {
                @Override
                public void onChanged(int newPosition) {
                    mapFragment.targetCheckpoint(newPosition);
                }
            });
        } else if (fragment instanceof MapFragment) {
            ((MapFragment) fragment).setOnMapClicked(this);
        }
    }

    private void onUpdateCurrentUser(User user) {
        currentUser = user;
        if (!currentUser.getAvatar().equals(user.getAvatar())) {
            Picasso.get().load(currentUser.getAvatar()).placeholder(R.drawable.user).into(imgAvatar);
        }
        currentTripRef = user.getActiveTrip();
        if (currentTripRef != null) {
            currentTripRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        currentTrip = task.getResult().toObject(Trip.class);
                        tvTripName.setText(currentTrip.getName());
                        tvTitle.setText(currentTrip.getName());
                    }
                }
            });
        } else {
            Log.d(TAG, "null trip");
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
                toggleToolbar(currentTripRef != null);
                ViewAnim.toggleHideShow(tvTitle, currentTripRef != null, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(containerToolbar, false, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(bottomSheet, false, ViewAnim.DIRECTION_DOWN);
                ViewAnim.toggleHideShow(bottomSpace, true, ViewAnim.DIRECTION_DOWN);
                replaceBottomSpace(FriendFragment.newInstance(selectedUser.getUserName()));
                if (mapFragment != null) mapFragment.cameraTarget(null, selectedUser.getLatLng());
                break;
            case STATE_CHECKPOINT:
                toggleToolbar(currentTripRef != null);
                ViewAnim.toggleHideShow(tvTitle, currentTripRef != null, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(containerToolbar, false, ViewAnim.DIRECTION_UP);
                ViewAnim.toggleHideShow(bottomSheet, false, ViewAnim.DIRECTION_DOWN);
                ViewAnim.toggleHideShow(bottomSpace, true, ViewAnim.DIRECTION_DOWN);
                replaceBottomSpace(new TripOverviewFragment());
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
        tvTripName = findViewById(R.id.tv_trip_name_activity_main);
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
                        currentTripRef = db.collection(Collections.TRIPS).document(tripCode);
                        currentUserRef.update(User.ACTIVE_TRIP, currentTripRef);
                        currentTripRef.update(Trip.MEMBERS, FieldValue.arrayUnion(currentUserRef));
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
            case R.id.menu_share_activity_main:
                Intent intent = new Intent(MainActivity.this, TripInvitationActivity.class);
                intent.putExtra(Trip.ID, currentTripRef.getId());
                startActivity(intent);
                return true;
            case R.id.menu_leave_trip_activity_main:
                LeaveTripConfirmDialog dialog = new LeaveTripConfirmDialog();
                dialog.setPositiveBtnClick(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeBtnClick(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentUserRef.update(User.ACTIVE_TRIP, null);
                    }
                });
                dialog.show(getSupportFragmentManager(), "leave trip");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class LeaveTripConfirmDialog extends OneDialog {
        @Override
        public int getTitleRes() {
            return super.getTitleRes();
        }

        @Override
        public int getMessageRes() {
            return R.string.dialog_message_leave_trip;
        }

        @Override
        public int getPositiveButtonTextRes() {
            return R.string.btn_cancel;
        }

        @Override
        public int getNegativeButtonTextRes() {
            return R.string.btn_leave_trip;
        }

        @Override
        public boolean isEnableNegativeButton() {
            return true;
        }
    }
}
