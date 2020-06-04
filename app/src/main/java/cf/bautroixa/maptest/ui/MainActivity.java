package cf.bautroixa.maptest.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.MapBackgroundControllable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.interfaces.OnAppbarStateChanged;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.http.HttpRequest;
import cf.bautroixa.maptest.presenter.MainActivityPresenter;
import cf.bautroixa.maptest.presenter.impl.MainActivityPresenterImpl;
import cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.map.MapBackgroundFragment;
import cf.bautroixa.maptest.ui.theme.OneAppbarFragment;
import cf.bautroixa.maptest.ui.theme.OneDialog;
import cf.bautroixa.maptest.utils.HandlerHelper;
import cf.bautroixa.maptest.utils.NavigableHelper;

import static cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter.Tabs.STATE_OPEN_DRAWER;
import static cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter.Tabs.TAB_ANY;
import static cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter.Tabs.TAB_MAP;

public class MainActivity extends AppCompatActivity implements MainActivityPresenter.View, NavigationInterfaces {
    private static final String TAG = "MainActivity";
    // back twice to exit
    HandlerHelper backHandlerHelper;
    boolean isBackPressed = false;

    MainActivityPresenterImpl mainActivityPresenter;
    MainActivityPagerAdapter adapter;
    MapBackgroundFragment mapBackgroundFragment;
    // Views
    View statusBar;
    ViewPager2 bottomNavPager;
    TabLayout tabLayout;
    private int appbarState = OnAppbarStateChanged.State.EXTENDED;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivityPresenter = new MainActivityPresenterImpl(this, this);
        backHandlerHelper = new HandlerHelper(this, new Runnable() {
            @Override
            public void run() {
                isBackPressed = false;
            }
        });

        // bind view
        initStatusBar();
        initTabAdapter();
        initDrawer();
        // TODO: this is temp fix
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NavigableHelper.handleNavigation(getIntent(), MainActivity.this);
            }
        }, 1000);
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            String tripNotificationId = bundle.getString(FcmMessage.NOTI_REF_ID, null);
//            if (tripNotificationId != null) {
//                mainActivityPresenter.handleTripNotification(tripNotificationId);
//            }
//        }
    }

    private void initDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout_root);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.menu_leave_trip:
                                final OneDialog leaveTripConfirmDialog = new OneDialog.Builder().title(R.string.dialog_title_leave_trip)
                                        .message(R.string.dialog_message_leave_trip)
                                        .posBtnText(R.string.btn_leave_trip).enableNegativeButton(true)
                                        .build();
                                leaveTripConfirmDialog.setButtonClickListener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            leaveTripConfirmDialog.toggleProgressBar(true);
                                            mainActivityPresenter.sendLeaveTrip().addOnCompleteListener(new OnCompleteListener<HttpRequest.APIResponse>() {
                                                @Override
                                                public void onComplete(@NonNull Task<HttpRequest.APIResponse> task) {
                                                    HttpRequest.APIResponse apiResponse = task.getResult();
                                                    Toast.makeText(MainActivity.this, "Rời phòng " + (task.isSuccessful() && apiResponse != null && apiResponse.success ? "thành công!" : "thất bại"), Toast.LENGTH_LONG).show();
                                                    leaveTripConfirmDialog.toggleProgressBar(false);
                                                    leaveTripConfirmDialog.dismiss();

                                                }
                                            });
                                        } else {
                                            dialog.dismiss();
                                        }
                                    }
                                });
                                leaveTripConfirmDialog.show(getSupportFragmentManager(), "leave trip");
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (bottomNavPager.getCurrentItem() == TAB_MAP && adapter.getTabMapFragment().onBackPressed())
            return;
        if (!isBackPressed) {
            isBackPressed = true;
            Toast.makeText(MainActivity.this, R.string.toast_back_again_to_exit, Toast.LENGTH_SHORT).show();
            backHandlerHelper.postDelayed(3000);
        } else {
            backHandlerHelper.removeCallback();
            super.onBackPressed();
        }
    }

    @Override
    public void onAttachFragment(@NotNull final Fragment fragment) {
        super.onAttachFragment(fragment);
        if (mapBackgroundFragment == null) {
            mapBackgroundFragment = (MapBackgroundFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map_background);
        }
        if (fragment instanceof OneAppbarFragment) {
            ((OneAppbarFragment) fragment).setAppbarState(appbarState);
            ((OneAppbarFragment) fragment).setOnAppbarStateChanged(new OnAppbarStateChanged() {
                @Override
                public void newState(int state) {
                    appbarState = state;
                }
            });
        }
        if (fragment instanceof NavigationInterfaceOwner) {
            ((NavigationInterfaceOwner) fragment).setNavigationInterfaces(this);
        }
        if (fragment instanceof MapBackgroundControllable) {
            ((MapBackgroundControllable) fragment).setMapBackgroundInterfaces(mapBackgroundFragment.getMapPresenter());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initStatusBar() {
        // status bar
        statusBar = findViewById(R.id.view_status_bar_activity_main);
        statusBar.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                statusBar.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, insets.getSystemWindowInsetTop()));
                return insets;
            }
        });
    }

    @Override
    public void initTabAdapter() {
        adapter = new MainActivityPagerAdapter(this);
        bottomNavPager = findViewById(R.id.bot_nav_pager);
        tabLayout = findViewById(R.id.bottom_navigation);
        bottomNavPager.setAdapter(adapter);
        bottomNavPager.setSaveEnabled(false);
        bottomNavPager.setUserInputEnabled(false);
        bottomNavPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        new TabLayoutMediator(tabLayout, bottomNavPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(MainActivityPagerAdapter.Tabs.tabNames[position]);
            }
        }).attach();
    }

    @Override
    public void navigate(int tab, int state, Object... data) {
        if (tab == TAB_ANY) {
            if (state == STATE_OPEN_DRAWER) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return;
        }
        Objects.requireNonNull(tabLayout.getTabAt(tab)).select();
        if (tab == TAB_MAP) {
            if (data.length > 0) {
                adapter.getTabMapFragment().pushState(state, data[0]);
            } else {
                adapter.getTabMapFragment().pushState(state, null);
            }
        } else {
            Log.e(TAG, "navigate to tab Not implemented");
        }
    }

    @Override
    public void navigate(int tab, int state, String className, String id) {
        if (className.equals(User.class.getSimpleName())) {
            User user = mainActivityPresenter.getUser(id);
            if (user != null) {
                navigate(tab, state, user);
            }
        } else if (className.equals(Checkpoint.class.getSimpleName())) {
            Checkpoint checkpoint = mainActivityPresenter.getCheckpoint(id);
            if (checkpoint != null) {
                navigate(tab, state, checkpoint);
            }
        }
    }

    @Override
    public void selectTab(int tabIndex) {
        Objects.requireNonNull(tabLayout.getTabAt(tabIndex)).select();
    }


}
