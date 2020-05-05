package cf.bautroixa.maptest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;

import cf.bautroixa.maptest.dialogs.SosRequestViewDialogFragment;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.firestore.Visit;
import cf.bautroixa.maptest.interfaces.MapBackgroundControllable;
import cf.bautroixa.maptest.interfaces.MapBackgroundInterfaces;
import cf.bautroixa.maptest.interfaces.Navigable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.ImageHelper;
import cf.bautroixa.maptest.utils.RecyclerViewOnScrollListener;

public class BottomMembersFragment extends Fragment implements Navigable, MapBackgroundControllable {
    private static final String TAG = "FriendFragment";
    // Data and state
    private MainAppManager manager;
    private ArrayList<User> members;
    private int activePos = 0;
    private String lastActiveCheckpointId = "";

    // Listener
    private MapBackgroundInterfaces mapBackgroundInterfaces;
    private NavigationInterfaces navigationInterfaces;
    private DatasManager.OnDatasChangedListener<User> onMembersChangedListener;
    private Data.OnNewValueListener<Trip> onTripChanged;

    // View
    private RecyclerView rv;
    private TextView tvCount;
    private FriendsAdapter adapter;
    private Button btnCall, btnDirection, btnMessage, btnShowSos;

    public BottomMembersFragment() {
        manager = MainAppManager.getInstance();
        members = manager.getMembers();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onTripChanged = new Data.OnNewValueListener<Trip>() {
            @Override
            public void onNewData(Trip trip) {
                if (trip.getActiveCheckpoint() == null) {
                    if (lastActiveCheckpointId.length() > 0) adapter.notifyDataSetChanged();
                    lastActiveCheckpointId = "";
                } else if (!trip.getActiveCheckpoint().getId().equals(lastActiveCheckpointId)) {
                    // new active checkpoint
                    lastActiveCheckpointId = trip.getActiveCheckpoint().getId();
                    adapter.notifyDataSetChanged();
                }
            }
        };

        onMembersChangedListener = new DatasManager.OnDatasChangedListener<User>() {
            @Override
            public void onItemInserted(int position, User data) {
                adapter.notifyItemInserted(position);
            }

            @Override
            public void onItemChanged(int position, User data) {
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onItemRemoved(int position, User data) {
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onDataSetChanged(ArrayList<User> datas) {
                adapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCount = view.findViewById(R.id.tv_count_frag_friend);
        tvCount.setText(String.format("%d/%d", 1, members.size()));

        btnCall = view.findViewById(R.id.btn_call_frag_friend);
        btnDirection = view.findViewById(R.id.btn_direction_frag_friend);
        btnMessage = view.findViewById(R.id.btn_message_frag_friend);
        btnShowSos = view.findViewById(R.id.btn_view_sos_frag_friend);

        rv = view.findViewById(R.id.rv_friends);

        adapter = new FriendsAdapter();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        rv.addOnScrollListener(new RecyclerViewOnScrollListener(RecyclerViewOnScrollListener.ScrollMode.SCROLL_IDLE, snapHelper, new RecyclerViewOnScrollListener.OnNewPosition() {
            @Override
            public void onNewPosition(int position) {
                newSelectedPosition(position);
            }
        }));
        newSelectedPosition(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        smoothScrollToPosition(activePos);
        manager.getCurrentTrip().addOnNewValueListener(onTripChanged);
        manager.getMembersManager().addOnDatasChangedListener(onMembersChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getCurrentTrip().removeOnNewValueListener(onTripChanged);
        manager.getMembersManager().removeOnDatasChangedListener(onMembersChangedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mapBackgroundInterfaces = null;
        navigationInterfaces = null;
        onMembersChangedListener = null;
        onTripChanged = null;
    }


    public void selectUser(String userId) {
        for (int i = 0; i < members.size(); i++) {
            if (userId.equals(members.get(i).getId())) {
                activePos = i;
                if (isResumed()) smoothScrollToPosition(i);
                return;
            }
        }
    }

    public void smoothScrollToPosition(int position) {
        if (activePos < members.size()) {
            rv.smoothScrollToPosition(position);
        } else {
            Log.e(TAG, "scroll to index out of bounds");
        }
    }

    private void newSelectedPosition(int position) {
        final User activeUser = members.get(position);
        mapBackgroundInterfaces.cleanUpTempMarkerAndRoute();
        mapBackgroundInterfaces.target(activeUser);
        tvCount.setText(String.format("%d/%d", position + 1, members.size()));
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + activeUser.getPhoneNumber()));
                startActivity(intent);
            }
        });
        btnDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapBackgroundInterfaces.drawRoute(null, activeUser.getLatLng());
            }
        });
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: when chat one to one ready, use navigationInterfaces to navigate to chat
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", activeUser.getPhoneNumber(), null)));
            }
        });
        btnShowSos.setVisibility(manager.getSosRequestsManager().contains(activeUser.getId()) ? View.VISIBLE : View.GONE);
        btnShowSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SosRequestViewDialogFragment.newInstance(mapBackgroundInterfaces, activeUser.getId()).show(getChildFragmentManager(), "sos viewer");
            }
        });
        activePos = position;
    }

    public void setMapBackgroundInterfaces(MapBackgroundInterfaces mapBackgroundInterfaces) {
        this.mapBackgroundInterfaces = mapBackgroundInterfaces;
    }

    public class FriendsAdapter extends RecyclerView.Adapter<MemberViewHolder> {

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MemberViewHolder(getLayoutInflater().inflate(R.layout.fragment_bottom_members_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            holder.bind(members.get(position));
        }

        @Override
        public int getItemCount() {
            return members.size();
        }
    }

    @Override
    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvLocation, tvBattery, tvSpeed, tvStatus, tvCheckInTime;
        ImageView imgAvatar;
        String currentAvatar = "";

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar_bottom_member_item);
            tvStatus = itemView.findViewById(R.id.tv_status_bottom_member_item);
            tvName = itemView.findViewById(R.id.tv_name_bottom_member_item);
            tvLocation = itemView.findViewById(R.id.tv_location_frag_friend);
            tvCheckInTime = itemView.findViewById(R.id.tv_checked_in_time_bottom_member_item);
            tvBattery = itemView.findViewById(R.id.tv_battery_bottom_member_item);
            tvSpeed = itemView.findViewById(R.id.tv_speed_bottom_member_item);
        }

        public void bind(final User user) {
            if (!currentAvatar.equals(user.getAvatar())) {
                ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar, 100, 100);
                currentAvatar = user.getAvatar();
            }
            tvName.setText(user.getName());
            tvLocation.setText(user.getCurrentLocation());
            tvBattery.setText(String.format("%d%%", user.getBattery()));
            tvSpeed.setText(String.format("%d m/s", user.getSpeed()));

            SosRequest userSosRequest = manager.getSosRequestsManager().get(user.getId());
            // TODO: add listener update SOS
            if (userSosRequest != null) {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText(R.string.tv_user_need_support);
                tvStatus.setSelected(false); // red background
            } else {
                tvStatus.setVisibility(View.GONE);
            }

            Checkpoint activeCheckpoint = manager.getActiveCheckpoint();
            if (activeCheckpoint != null) {
                tvCheckInTime.setVisibility(View.VISIBLE);
                Visit userVisit = activeCheckpoint.getVisitsManager().get(user.getId());
                if (userVisit != null) {
                    tvCheckInTime.setText(DateFormatter.format(userVisit.getTime()));
                    if (userSosRequest == null) {
                        tvStatus.setVisibility(View.VISIBLE);
                        tvStatus.setText(R.string.tv_user_checked_in);
                        tvStatus.setSelected(true); // green background
                    }
                } else {
                    tvCheckInTime.setText(R.string.tv_user_not_checked_in);
                }
            } else {
                tvCheckInTime.setVisibility(View.GONE);
            }
        }
    }
}
