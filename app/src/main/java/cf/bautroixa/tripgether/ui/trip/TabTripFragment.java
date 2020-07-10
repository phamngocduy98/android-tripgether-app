package cf.bautroixa.tripgether.ui.trip;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.model.http.HttpService;
import cf.bautroixa.tripgether.model.http.TripHttpService;
import cf.bautroixa.tripgether.ui.dialogs.CheckpointEditDialogFragment;
import cf.bautroixa.tripgether.ui.dialogs.SelectCheckpointDialogFragment;
import cf.bautroixa.tripgether.ui.dialogs.SosRequestEditDialogFragment;
import cf.bautroixa.tripgether.ui.bottomsheet.BottomSheetCheckpointListFragment;
import cf.bautroixa.tripgether.ui.theme.OneAppbarFragment;
import cf.bautroixa.tripgether.ui.theme.OneDialog;
import cf.bautroixa.tripgether.ui.theme.OnePromptDialog;
import cf.bautroixa.tripgether.ui.trip_invite.TripInvitationActivity;


public class TabTripFragment extends OneAppbarFragment implements Toolbar.OnMenuItemClickListener, NavigationInterfaceOwner {
    private static final String TAG = "TabTripFragment";
    String[] tabNames = {"Chuyến đi", "Hành trình"};
    Button btnCreateTrip, btnJoinTrip;
    ViewPager2 pager;
    TabLayout tabLayout;
    TabAdapter adapter;
    private ModelManager manager;
    private NavigationInterface navigationInterface = null;
    private Document.OnValueChangedListener<Trip> tripOnNewValueListener;

    public TabTripFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
        tripOnNewValueListener = new Document.OnValueChangedListener<Trip>() {
            @Override
            public void onValueChanged(@Nullable Trip trip) {
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

    private void updateTrip(Trip trip) {
        if (trip.isAvailable()) {
            if (manager.getCurrentTripRef() == null) {
                // this happen when user leave trip
                // first user was remove from Trip.members list, so a trip update is triggered (THIS ERROR HAPPENS HERE)
                // then user trip is null so a new trip update is triggered with both trip and tripRef is null
                Log.e(TAG, "Trip instance nonnull while tripRef Null");
            }
        }
    }

    private void updateState() {

    }

    private void handleState(int state) {

        updateState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_trip, container, false);
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
        setBackButtonIcon(R.drawable.ic_menu_black_24dp);
        setToolbar();
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
                CheckpointEditDialogFragment.newInstance(new CheckpointEditDialogFragment.OnCheckpointSetListener() {
                    @Override
                    public void onCheckpointSet(Checkpoint checkpoint) {
                        manager.getCurrentTrip().getCheckpointsManager().create(null, checkpoint);
                    }
                }).show(getChildFragmentManager(), "add checkpoint");
                return true;
            case R.id.menu_send_sos_frag_trip:
            case R.id.menu_send_sos_2_frag_trip:
                new SosRequestEditDialogFragment().show(getChildFragmentManager(), "add edit sos");
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
                            final SelectCheckpointDialogFragment selectCheckpointDialogFragment = new SelectCheckpointDialogFragment();
                            selectCheckpointDialogFragment.setOnCheckpointSelectedListener(new SelectCheckpointDialogFragment.OnCheckpointSelectedListener() {
                                @Override
                                public void onCheckpointSelected(Checkpoint checkpoint) {
                                    selectCheckpointDialogFragment.toggleProgressBar(true);
//                                    manager.sendAddCheckInLocation(requireContext(), null, checkpoint.getRef()).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            selectCheckpointDialogFragment.toggleProgressBar(false);
//                                            selectCheckpointDialogFragment.dismiss();
//                                            dialog.dismiss();
//                                        }
//                                    });
                                }
                            });
                            selectCheckpointDialogFragment.setButtonClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectCheckpointDialogFragment.dismiss();
                                }
                            });
                            selectCheckpointDialogFragment.show(getChildFragmentManager(), "select cp dialog");
                        } else {
                            // vị trí hiện tại
                            new OnePromptDialog.Builder().title(R.string.dialog_title_enter_checkpoint_name).onResult(new OnePromptDialog.OnDialogResult() {
                                @Override
                                public void onDialogResult(final OnePromptDialog enterCheckpointNameDialog, boolean isCanceled, String value) {
                                    if (!isCanceled) {
                                        enterCheckpointNameDialog.toggleProgressBar(true);
//                                        manager.sendAddCheckInLocation(requireContext(), value).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                enterCheckpointNameDialog.toggleProgressBar(false);
//                                                enterCheckpointNameDialog.dismiss();
//                                                dialog.dismiss();
//                                            }
//                                        });
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
                intent.putExtra(Trip.ID, manager.getCurrentUser().getActiveTripRef().getId());
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
                            TripHttpService.leaveTrip().addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
                                @Override
                                public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
                                    HttpService.APIResponse apiResponse = task.getResult();
                                    Toast.makeText(getContext(), "Rời phòng " + (task.isSuccessful() && apiResponse != null && apiResponse.success ? "thành công!" : "thất bại"), Toast.LENGTH_LONG).show();
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

    void setToolbar() {
        if (manager.getCurrentTripRef() != null) {
            if (manager.isTripLeader()) {
                setToolbarMenu(R.menu.fragment_trip_for_leader);
            } else {
                setToolbarMenu(R.menu.fragment_trip_for_members);
            }
        } else {
            setToolbarMenu(R.menu.fragment_trip_no_trip);
        }
    }

    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof BottomSheetCheckpointListFragment) {
            ((BottomSheetCheckpointListFragment) childFragment).setNavigationInterface(navigationInterface);
        } else if (childFragment instanceof TabTripFragmentTrip) {
            ((TabTripFragmentTrip) childFragment).setNavigationInterface(navigationInterface);
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
            return new BottomSheetCheckpointListFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

}
