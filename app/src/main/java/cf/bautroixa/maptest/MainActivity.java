package cf.bautroixa.maptest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.BatteryHelper;
import cf.bautroixa.maptest.utils.ShakePhoneHelper;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, MapFragment.OnMapClicked {
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private HashMap<String, User> friends;

    private static final int STATE_HIDE = -1;
    private static final int STATE_FRIEND_LIST = 0;
    private static final int STATE_FRIEND_LIST_EXPANDED = 1;
    private static final int STATE_FRIEND_STATUS = 10;
    private static final int STATE_CHECKPOINT = 20;

    int state, lastState = 0;
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
    TextView tvTripName;
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
        // get
        db.collection(Collections.USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        user.setUserName(document.getId());
                        friends.put(document.getId(), user);
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
        // listen changed
        db.collection(Collections.USERS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    User user = documentSnapshot.toObject(User.class);
                    user.setUserName(documentSnapshot.getId());
                    friends.put(documentSnapshot.getId(), user);
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

    private void handleState(int newState){
        state = newState;
        switch (state){
            case STATE_FRIEND_LIST: case STATE_FRIEND_LIST_EXPANDED:
                if (state == STATE_FRIEND_LIST) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                toggleToolbar(false);
                toggleHideShow(containerToolbar, true, true);
                toggleHideShow(bottomSheet, true, true);
                toggleHideShow(bottomSpace, false, false);
                break;
            case STATE_FRIEND_STATUS:
                toggleToolbar(true);
                toggleHideShow(containerToolbar, false, true);
                toggleHideShow(bottomSheet, false, false);
                toggleHideShow(bottomSpace, true, false);
                replaceBottomSpace(FriendFragment.newInstance(selectedUser.getUserName()));
                if (mapFragment != null) mapFragment.cameraTarget(null, selectedUser.getLatLng());
                break;
            case STATE_CHECKPOINT:
                toggleToolbar(true);
                toggleHideShow(containerToolbar, false, true);
                toggleHideShow(bottomSheet, false, true);
                toggleHideShow(bottomSpace, true, false);
                replaceBottomSpace(new TripOverviewFragment());
                break;
        }
        Log.d(TAG, "new state= "+state);
    }

    private void replaceBottomSpace(Fragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.bottom_space, fragment);
        ft.commit();
    }

    void toggleToolbar(boolean show){
        toggleHideShow(toolbar, show, true);
        toggleHideShow(statusBar, show, true);
    }

    void toggleHideShow(View view, boolean show, boolean up){
        if (show){
            view.animate().translationY(0).alpha(1);
        } else {
            view.animate().translationY(view.getHeight()*(up?-1:1)).alpha(0);
        }
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
        Log.d(TAG, "last state= "+lastState);
        if (state != STATE_HIDE){
            toggleToolbar(false);
            toggleHideShow(containerToolbar, false, true);
            toggleHideShow(bottomNavigationView, false, false);
            toggleHideShow(bottomSheet, false, false);
            toggleHideShow(bottomSpace, false, false);
            lastState = state;
            state = STATE_HIDE;
        } else {
            toggleHideShow(bottomNavigationView, true, false);
            handleState(lastState);
        }
        Log.d(TAG, "click new state= "+state);
    }

    private void initStatusBarToolbar(){
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
        toolbar = findViewById(R.id.toolbar_main_activity);
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
        switch (item.getItemId()){
            case R.id.nav_location_tab:
                handleState(STATE_FRIEND_LIST);
                break;
            case R.id.nav_trip_tab:
                if (false) {
                    handleState(STATE_CHECKPOINT);
                } else {
                    NoTripDialogFragment noTripDialogFragment = new NoTripDialogFragment();
                    noTripDialogFragment.show(getSupportFragmentManager(), "no tripp");
                    return false;
                }
                break;
            case R.id.nav_bar_setting:
                break;
        }
        return true;
    }
}
