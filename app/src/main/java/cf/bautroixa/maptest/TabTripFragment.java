package cf.bautroixa.maptest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import cf.bautroixa.maptest.dialogs.DialogCheckpointEditFragment;
import cf.bautroixa.maptest.dialogs.DialogQRScanFragment;
import cf.bautroixa.maptest.dialogs.DialogSelectCheckpoint;
import cf.bautroixa.maptest.dialogs.SosRequestEditDialogFragment;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.interfaces.NavigableToMainTab;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.interfaces.OnNavigationToMainTab;
import cf.bautroixa.maptest.theme.OneAppbarFragment;
import cf.bautroixa.maptest.theme.OneDialog;
import cf.bautroixa.maptest.theme.OnePromptDialog;
import cf.bautroixa.maptest.utils.UrlParser;


public class TabTripFragment extends OneAppbarFragment implements Toolbar.OnMenuItemClickListener, NavigableToMainTab {
    private static final String TAG = "TabTripFragment";
    public static final int STATE_NONE = 0;
    public static final int STATE_NO_TRIP = 1;
    public static final int STATE_TRIP = 2;
    String[] tabNames = {"Chuyến đi", "Điểm đến"};
    Button btnCreateTrip, btnJoinTrip;
    ViewPager2 pager;
    TabLayout tabLayout;
    TabAdapter adapter;
    private MainAppManager manager;
    private int currentState = STATE_NONE;
    private OnNavigationToMainTab onNavigationToMainTab = null;
    private Data.OnNewValueListener<Trip> tripOnNewValueListener;

    public TabTripFragment() {
        manager = MainAppManager.getInstance();
        tripOnNewValueListener = new Data.OnNewValueListener<Trip>() {
            @Override
            public void onNewData(Trip trip) {
                updateTrip(trip);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getCurrentTrip().addOnNewValueListener(tripOnNewValueListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getCurrentTrip().removeOnNewValueListener(tripOnNewValueListener);
    }

    private void updateTrip(@Nullable Trip trip) {
        if (currentState != STATE_TRIP && manager.getCurrentTripRef() != null) {
            if (trip == null) Log.w(TAG, "Trip instance null while tripRef nonNull");
            handleState(STATE_TRIP);
            return;
        }
        if (currentState != STATE_NO_TRIP && manager.getCurrentTripRef() == null) {
            if (trip != null) Log.w(TAG, "Trip instance NonNull while tripRef null");
            handleState(STATE_NO_TRIP);
        }
    }

    private void handleState(int state) {
        if (manager.getCurrentTripRef() != null) {
            btnCreateTrip.setVisibility(View.GONE);
            btnJoinTrip.setVisibility(View.GONE);
            tabLayout.setVisibility(View.VISIBLE);
            pager.setVisibility(View.VISIBLE);
            setTitle(manager.getCurrentTrip().getName());
            setSubtitle(String.format("%d thành viên, %d điểm đến", manager.getMembers().size(), manager.getCheckpoints().size()));
        } else {
            currentState = STATE_NO_TRIP;
            tabLayout.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);

            setTitle("Chuyến đi");
            setSubtitle("Bạn chưa tham gia chuyến đi nào");

            btnCreateTrip.setVisibility(View.VISIBLE);
            btnJoinTrip.setVisibility(View.VISIBLE);
            btnCreateTrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), CreateTripActivity.class);
                    startActivity(intent);
                }
            });
            btnJoinTrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OneDialog.Builder().enableNegativeButton(true).title(R.string.dialog_title_join_trip)
                            .message(R.string.dialog_message_join_trip)
                            .negBtnText(R.string.btn_enter_trip_code)
                            .posBtnText(R.string.btn_qr_scan)
                            .buttonClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface joinTripDialog, int which) {
                                    if (which == DialogInterface.BUTTON_POSITIVE) {
                                        joinTripDialog.dismiss();
                                        new DialogQRScanFragment(new DialogQRScanFragment.OnQrResultListener() {
                                            @Override
                                            public void onResult(String result) {
                                                if (getContext() != null) {
                                                    String tripCode = UrlParser.parseTripCode(getContext(), result);
                                                    manager.sendJoinTrip(tripCode, new MainAppManager.OnComplete() {
                                                        @Override
                                                        public void onComplete(boolean isSuccessful) {

                                                        }
                                                    });
                                                }
                                            }
                                        }).show(getChildFragmentManager(), "qr scanner");
                                    } else {
                                        new OnePromptDialog.Builder().title(R.string.dialog_title_enter_trip_code).onResult(new OnePromptDialog.OnDialogResult() {
                                            @Override
                                            public void onDialogResult(final OnePromptDialog onePromptDialog, boolean isCanceled, String value) {
                                                if (!isCanceled) {
                                                    onePromptDialog.toggleProgressBar(true);
                                                    manager.sendJoinTrip(value, new MainAppManager.OnComplete() {
                                                        @Override
                                                        public void onComplete(boolean isSuccessful) {
//                                                onePromptDialog.toggleProgressBar(false);
                                                            // TODO: if isSuccessful = false: notify user
                                                            onePromptDialog.dismiss();
                                                            joinTripDialog.dismiss();
                                                        }
                                                    });
                                                } else {
                                                    onePromptDialog.dismiss();
                                                }
                                            }
                                        }).show(getChildFragmentManager(), "enter trip code");
                                    }
                                }
                            }).show(getChildFragmentManager(), "join trip");
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_trip, container, false);

        currentState = STATE_NONE;
        // no trip
        btnCreateTrip = view.findViewById(R.id.btn_create_trip_dialog_no_trip);
        btnJoinTrip = view.findViewById(R.id.btn_join_trip_dialog_no_trip);
        // has trip
        pager = view.findViewById(R.id.pager_frag_trip);
        tabLayout = view.findViewById(R.id.tab_layout_frag_trip);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbarMenu(R.menu.fragment_trip);
        getToolbar().setOnMenuItemClickListener(this);

        adapter = new TabAdapter(this);
        pager.setAdapter(adapter);
        pager.setSaveEnabled(false);
        new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabNames[position]);
            }
        }).attach();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_frag_trip:
                if (!manager.isTripLeader()) return false;
                if (pager.getCurrentItem() == 0) {
                    DialogCheckpointEditFragment.newInstance(new DialogCheckpointEditFragment.OnCheckpointSetListener() {
                        @Override
                        public void onCheckpointSet(Checkpoint checkpoint) {
                            manager.getCheckpointsManager().create(null, checkpoint);
                        }
                    }).show(getChildFragmentManager(), "add checkpoint");
                } else {
                    new SosRequestEditDialogFragment().show(getChildFragmentManager(), "add edit sos");
                }
                return true;
            case R.id.menu_notification_frag_trip:
                return true;
            case R.id.menu_send_sos_frag_trip:
                SosRequestEditDialogFragment sosDialogFragment = new SosRequestEditDialogFragment();
                sosDialogFragment.show(getChildFragmentManager(), "send SOS");
                return true;
            case R.id.menu_request_check_in_frag_trip:
                OneDialog.Builder builder = new OneDialog.Builder();
                builder.title(R.string.dialog_title_gather);
                builder.message(R.string.dialog_message_choose_gather_position);
                builder.enableNegativeButton(true);
                builder.posBtnText(R.string.btn_pos_res_choose_checkpoint);
                builder.negBtnText(R.string.btn_neg_current_position);
                builder.buttonClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            // chọn checkpoint
                            final DialogSelectCheckpoint dialogSelectCheckpoint = new DialogSelectCheckpoint();
                            dialogSelectCheckpoint.setOnCheckpointSelectedListener(new DialogSelectCheckpoint.OnCheckpointSelectedListener() {
                                @Override
                                public void onCheckpointSelected(Checkpoint checkpoint) {
                                    dialogSelectCheckpoint.toggleProgressBar(true);
                                    manager.sendAddCheckInLocation(null, checkpoint.getRef(), new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dialogSelectCheckpoint.toggleProgressBar(false);
                                            dialogSelectCheckpoint.dismiss();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            });
                            dialogSelectCheckpoint.setButtonClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogSelectCheckpoint.dismiss();
                                }
                            });
                            dialogSelectCheckpoint.show(getChildFragmentManager(), "select cp dialog");
                        } else {
                            // vị trí hiện tại
                            new OnePromptDialog.Builder().title(R.string.dialog_title_enter_checkpoint_name).onResult(new OnePromptDialog.OnDialogResult() {
                                @Override
                                public void onDialogResult(final OnePromptDialog enterCheckpointNameDialog, boolean isCanceled, String value) {
                                    if (!isCanceled) {
                                        enterCheckpointNameDialog.toggleProgressBar(true);
                                        manager.sendAddCheckInLocation(value, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                enterCheckpointNameDialog.toggleProgressBar(false);
                                                enterCheckpointNameDialog.dismiss();
                                                dialog.dismiss();
                                            }
                                        });
                                    } else {
                                        enterCheckpointNameDialog.dismiss();
                                    }
                                }
                            }).show(getChildFragmentManager(), "enter cp name");
                        }
                    }
                });
                OneDialog selectRollupTypeDialog = builder.build();
                selectRollupTypeDialog.show(getChildFragmentManager(), "select gather position");
                return true;
            case R.id.menu_share_frag_trip:
                Intent intent = new Intent(getContext(), TripInvitationActivity.class);
                intent.putExtra(Trip.ID, manager.getCurrentUser().getActiveTrip().getId());
                startActivity(intent);
                return true;
            case R.id.menu_leave_trip_frag_trip:
                final OneDialog leaveTripConfirmDialog = new OneDialog.Builder().title(R.string.dialog_title_leave_trip)
                        .message(R.string.dialog_message_leave_trip)
                        .posBtnText(R.string.btn_leave_trip).enableNegativeButton(true)
                        .build();
                leaveTripConfirmDialog.setButtonClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            leaveTripConfirmDialog.toggleProgressBar(true);
                            manager.sendLeaveTrip(null, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    leaveTripConfirmDialog.toggleProgressBar(false);
                                    leaveTripConfirmDialog.dismiss();
                                }
                            });
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
                leaveTripConfirmDialog.show(getChildFragmentManager(), "leave trip");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setOnNavigationToMainTab(OnNavigationToMainTab onNavigationToMainTab) {
        this.onNavigationToMainTab = onNavigationToMainTab;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof TabTripFragmentCheckpoints) {
            ((TabTripFragmentCheckpoints) childFragment).setOnDataItemSelected(new OnDataItemSelected<Checkpoint>() {
                @Override
                public void selectItem(Checkpoint checkpoint) {
                    onNavigationToMainTab.navigate(MainActivity.TAB_MAP, TabMainFragment.STATE_CHECKPOINT, checkpoint);
                }
            });
        } else if (childFragment instanceof TabTripFragmentTrip) {
            ((TabTripFragmentTrip) childFragment).setOnNavigationToMainTab(onNavigationToMainTab);
        }
    }

    static class TabAdapter extends FragmentStateAdapter {
        TabAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new TabTripFragmentTrip();
            }
            return new TabTripFragmentCheckpoints();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

}
