package cf.bautroixa.maptest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.interfaces.OnDrawRouteRequest;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.ImageHelper;
import cf.bautroixa.maptest.utils.RecyclerViewOnScrollListener;

public class BottomMembersFragment extends Fragment {
    public static final String ARG_USER_NAME = "user_name";
    // const
    private static final String TAG = "FriendFragment";
    // Data and state
    private MainAppManager manager;
    private ArrayList<User> members;
    private int activePos;
    private String lastActiveCheckpointId = "";

    // Listener
    private DatasManager.OnItemInsertedListener<User> onUserItemInsertedListener;
    private DatasManager.OnItemChangedListener<User> onUserItemChangedListener;
    private DatasManager.OnItemRemovedListener<User> onUserItemRemovedListener;
    private DatasManager.OnDataSetChangedListener<User> onUserDataSetChangedListener;
    private Data.OnNewValueListener<Trip> onTripChanged;

    private OnDrawRouteRequest onDrawRouteRequest;
    private OnDataItemSelected<User> onUserItemSelected;

    // View
    private RecyclerView rv;
    private TextView tvCount;

    // adapter
    private FriendsAdapter adapter;


    public BottomMembersFragment() {
        manager = MainAppManager.getInstance();
        members = manager.getMembers();
        activePos = 0;

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

        onUserItemInsertedListener = new DatasManager.OnItemInsertedListener<User>() {
            @Override
            public void onItemInserted(int position, User data) {
                adapter.notifyItemInserted(position);
            }
        };

        onUserItemChangedListener = new DatasManager.OnItemChangedListener<User>() {
            @Override
            public void onItemChanged(int position, User data) {
                adapter.notifyItemChanged(position);
            }
        };

        onUserItemRemovedListener = new DatasManager.OnItemRemovedListener<User>() {
            @Override
            public void onItemRemoved(int position, User data) {
                adapter.notifyItemRemoved(position);
            }
        };
        onUserDataSetChangedListener = new DatasManager.OnDataSetChangedListener<User>() {
            @Override
            public void onDataSetChanged(ArrayList<User> datas) {
                adapter.notifyDataSetChanged();
            }
        };
    }

    public void setOnDrawRouteButtonClickedListener(OnDrawRouteRequest onDrawRouteRequest) {
        this.onDrawRouteRequest = onDrawRouteRequest;
    }

    public void setOnChangeSelectedUserListener(OnDataItemSelected<User> onUserChangedListener) {
        this.onUserItemSelected = onUserChangedListener;
    }

    public void selectUser(String userId) {
        for (int i = 0; i < members.size(); i++) {
            if (userId.equals(members.get(i).getId())) {
                activePos = i;
                if (isResumed()) {
                    scrollToSelectedUser();
                }
                return;
            }
        }
    }

    private void scrollToSelectedUser() {
        onUserItemSelected.selectItem(members.get(activePos));
        rv.smoothScrollToPosition(activePos);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollToSelectedUser();
        onTripChanged.onNewData(manager.getCurrentTrip());
        manager.getCurrentTrip().addOnNewValueListener(onTripChanged);
        manager.getMembersManager().addOnItemInsertedListener(onUserItemInsertedListener)
                .addOnItemChangedListener(onUserItemChangedListener)
                .addOnItemRemovedListener(onUserItemRemovedListener)
                .addOnDataSetChangedListener(onUserDataSetChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getCurrentTrip().removeOnNewValueListener(onTripChanged);
        manager.getMembersManager().removeOnItemInsertedListener(onUserItemInsertedListener)
                .removeOnItemChangedListener(onUserItemChangedListener)
                .removeOnItemRemovedListener(onUserItemRemovedListener)
                .removeOnDataSetChangedListener(onUserDataSetChangedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bottom_members, container, false);

        tvCount = v.findViewById(R.id.tv_count_frag_friend);
        Button btnCall = v.findViewById(R.id.btn_call_frag_friend);
        Button btnDirection = v.findViewById(R.id.btn_direction_frag_friend);
        Button btnMessage = v.findViewById(R.id.btn_message_frag_friend);
        Button btnShowSos = v.findViewById(R.id.btn_view_sos_frag_friend);

        tvCount.setText(String.format("%d/%d", 1, members.size()));
        final User activeUser = members.get(activePos);
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
                onDrawRouteRequest.drawRouteTo(activeUser.getLatLng());
            }
        });
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", activeUser.getPhoneNumber(), null)));
            }
        });
        btnShowSos.setVisibility(manager.getSosRequestsManager().contains(activeUser.getId()) ? View.VISIBLE : View.GONE);
        btnShowSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SosRequestViewDialogFragment.newInstance(onDrawRouteRequest, activeUser.getId()).show(getChildFragmentManager(), "sos viewer");
            }
        });

        adapter = new FriendsAdapter();
        SnapHelper snapHelper = new PagerSnapHelper();
        rv = v.findViewById(R.id.rv_friends);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        snapHelper.attachToRecyclerView(rv);
        rv.addOnScrollListener(new RecyclerViewOnScrollListener(RecyclerViewOnScrollListener.ScrollMode.SCROLL_IDLE, snapHelper, new RecyclerViewOnScrollListener.OnNewPosition() {
            @Override
            public void onNewPosition(int position) {
                tvCount.setText(String.format("%d/%d", position + 1, members.size()));
                activePos = position;
                if (onUserItemSelected != null)
                    onUserItemSelected.selectItem(members.get(position));
            }
        }));
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onDrawRouteRequest = null;
        onUserItemSelected = null;
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
                ImageHelper.loadImage(user.getAvatar(), imgAvatar, 100, 100);
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
                    // TODO: userVisit.getTime() may NULL here
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
}
