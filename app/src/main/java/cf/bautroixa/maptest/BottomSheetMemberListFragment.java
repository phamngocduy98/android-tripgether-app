package cf.bautroixa.maptest;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.firestore.Visit;
import cf.bautroixa.maptest.interfaces.Navigable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.ImageHelper;

public class BottomSheetMemberListFragment extends Fragment implements Navigable {
    private static final String TAG = "FriendListStatusFrag";

    // data and state
    private MainAppManager manager;
    private ArrayList<User> members;

    // listener
    private DatasManager.OnDatasChangedListener<User> onMembersChangedListener;
    private NavigationInterfaces navigationInterfaces;
//    private OnFilterUser onFilterUser, defaultUserFilter;

    // view
    private TextView dragMark;
    private RecyclerView rvFriendList, rvFriendListLite;

    // adapter
    private FriendStatusAdapter friendStatusAdapter, friendStatusLiteAdapter;

    public BottomSheetMemberListFragment() {
        manager = MainAppManager.getInstance();
        members = manager.getMembers();
//        defaultUserFilter = new OnFilterUser() {
//            @Override
//            public boolean onUserFiltering(User user) {
//                return true;
//            }
//        };
//        onFilterUser = defaultUserFilter;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        members.addAll(manager.getMembers());
        friendStatusAdapter = new FriendStatusAdapter();
        friendStatusLiteAdapter = new FriendStatusLiteAdapter();

        onMembersChangedListener = new DatasManager.OnDatasChangedListener<User>() {
            @Override
            public void onItemInserted(int position, User data) {
//                if (members.size() == manager.getMembers().size() || onFilterUser == defaultUserFilter){
//                    applyFilter(onFilterUser);
//                } else {
                friendStatusAdapter.notifyItemInserted(position);
                friendStatusLiteAdapter.notifyItemInserted(position);
                Log.d(TAG, "insert" + position);
//                }
            }

            @Override
            public void onItemChanged(int position, User data) {
                friendStatusAdapter.notifyItemChanged(position);
                friendStatusLiteAdapter.notifyItemChanged(position);
                Log.d(TAG, "change" + position);
            }

            @Override
            public void onItemRemoved(int position, User data) {
                friendStatusAdapter.notifyItemRemoved(position);
                friendStatusLiteAdapter.notifyItemRemoved(position);
                Log.d(TAG, "remove" + position);
            }

            @Override
            public void onDataSetChanged(ArrayList<User> datas) {
                members = datas;
                friendStatusAdapter.notifyDataSetChanged();
                friendStatusLiteAdapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_member_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dragMark = view.findViewById(R.id.drag_mark_frag_friend_list_status);

        rvFriendList = view.findViewById(R.id.rv_friend_list);
        rvFriendListLite = view.findViewById(R.id.rv_friend_list_lite);
        rvFriendList.setAdapter(friendStatusAdapter);
        rvFriendListLite.setAdapter(friendStatusLiteAdapter);
        rvFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendListLite.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvFriendListLite);

        ViewAnim.toggleHideShow(dragMark, true, ViewAnim.HIDE_DIRECTION_UP);
        ViewAnim.toggleHideShow(rvFriendListLite, true, ViewAnim.HIDE_DIRECTION_UP);
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getMembersManager().addOnDatasChangedListener(onMembersChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getMembersManager().removeOnDatasChangedListener(onMembersChangedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        this.removeFilter();
        navigationInterfaces = null;
        onMembersChangedListener = null;
    }

//    public void applyFilter(OnFilterUser onFilterUser) {
//        this.onFilterUser = onFilterUser;
//        members.clear();
//        for (User user : manager.getMembers()) {
//            if (onFilterUser.onUserFiltering(user)) {
//                members.add(user);
//            }
//        }
//        if (this.friendStatusAdapter != null) this.friendStatusAdapter.notifyDataSetChanged();
//        if (this.friendStatusLiteAdapter != null)
//            this.friendStatusLiteAdapter.notifyDataSetChanged();
//    }
//
//    public void removeFilter() {
//        onFilterUser = defaultUserFilter;
//        members.clear();
//        members.addAll(manager.getMembers());
//        if (this.friendStatusAdapter != null) this.friendStatusAdapter.notifyDataSetChanged();
//        if (this.friendStatusLiteAdapter != null)
//            this.friendStatusLiteAdapter.notifyDataSetChanged();
//    }

    public void onBottomSheetStateChanged(int newState) {
        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            // hide Lite list
            ViewAnim.toggleHideShow(dragMark, false, ViewAnim.HIDE_DIRECTION_UP);
            ViewAnim.toggleHideShow(rvFriendListLite, false, ViewAnim.HIDE_DIRECTION_UP);
        }
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            // show lite list
            ViewAnim.toggleHideShow(dragMark, true, ViewAnim.HIDE_DIRECTION_UP);
            ViewAnim.toggleHideShow(rvFriendListLite, true, ViewAnim.HIDE_DIRECTION_UP);
        }
    }

    public void onSlideBottomSheet(float percent) {
        rvFriendListLite.setTranslationY(-rvFriendListLite.getHeight() * percent);
        rvFriendListLite.setAlpha(1 - percent);
        rvFriendList.setTranslationY(-rvFriendListLite.getHeight() * percent);
        rvFriendList.setAlpha(percent);
    }

    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
    }

    public interface OnFilterUser {
        boolean onUserFiltering(User user);
    }

    public static class Payload {
        public static final int UPDATED = 1;
    }

    public class FriendStatusViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvName, tvLocation, tvCount, tvLastUpdate, tvNameInAvatar, tvOnlineIndicator;
        protected RoundedImageView imgAvatar;
        protected View view;
        protected User currentUser;
        CircularProgressBar progressBattery;

        public FriendStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            this.findView();
        }

        public void findView() {
            progressBattery = itemView.findViewById(R.id.progress_battery_item_friend);
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_friend);
            imgAvatar = itemView.findViewById(R.id.img_avatar_status_item_friend);
            tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
            tvLastUpdate = itemView.findViewById(R.id.tv_last_update_item_friend);
            tvLocation = itemView.findViewById(R.id.tv_location_status_item_friend);
            tvCount = itemView.findViewById(R.id.tv_count_status_item_friend);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_friend);
        }

        public void update(User user) {
            if (user.getLastUpdate() != null) {
                tvLastUpdate.setText(DateFormatter.format(user.getLastUpdate()));
                if (Calendar.getInstance().getTimeInMillis() - user.getLastUpdate().toDate().getTime() < 5 * 60 * 1000) { // online in less than 5 mins
                    tvOnlineIndicator.setSelected(true);// green background
                } else {
                    tvOnlineIndicator.setSelected(false);// red background
                }
            }
            progressBattery.setProgress(user.getBattery());
            tvLocation.setText(user.getCurrentLocation());
            if (!user.getAvatar().equals(currentUser.getAvatar())) {
                ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
            }
            if (manager.getSosRequestsManager().contains(user.getId())) {
                tvCount.setText("SOS");
            } else {
                if (user.getId().equals(manager.getCurrentTrip().getLeader().getId())) {
                    tvCount.setText("Leader");
                } else {
                    tvCount.setVisibility(View.GONE);
                }
            }
            currentUser = user;
            Checkpoint activeCheckpoint = manager.getActiveCheckpoint();
            if (activeCheckpoint != null) {
                Visit userVisit = activeCheckpoint.getVisitsManager().get(user.getId());
                if (userVisit != null) {
                    tvLocation.setText(String.format("Đã có mặt lúc %s", DateFormatter.format(userVisit.getTime())));
                    tvCount.setText(R.string.tv_user_checked_in);
                    tvCount.setSelected(true); // green background
                }
            }
        }

        public void bind(User user) {
            currentUser = user;
            tvName.setText(user.getName());
            if (user.getAvatar() == null || Objects.equals(user.getAvatar(), User.DEFAULT_AVATAR)) {
                imgAvatar.setVisibility(View.INVISIBLE);
                tvNameInAvatar.setText(user.getShortName());
            }
            ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
            this.update(user);
        }
    }

    public class FriendStatusLiteAdapter extends FriendStatusAdapter {

        @NonNull
        @Override
        public FriendStatusLiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bottom_sheet_member_list_item_lite, parent, false);
            return new FriendStatusLiteViewHolder(v);
        }
    }

    public class FriendStatusLiteViewHolder extends FriendStatusViewHolder {
        public FriendStatusLiteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void findView() {
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_friend_status_lite);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_friend_lite);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_friend_status_lite);
            tvCount = itemView.findViewById(R.id.tv_messages_count_item_friend_status_lite);
        }

        @Override
        public void bind(User user) {
            ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
            if (user.getId().equals(manager.getCurrentTrip().getLeader().getId())) {
                tvCount.setText("L");
            } else {
                tvCount.setVisibility(View.GONE);
            }
            this.update(user);
        }

        @Override
        public void update(User user) {
            if (user.getLastUpdate() != null && Calendar.getInstance().getTimeInMillis() - user.getLastUpdate().toDate().getTime() < 5 * 60 * 1000) {
                // online in less than 5 mins
                tvOnlineIndicator.setSelected(true);// green background
            } else {
                tvOnlineIndicator.setSelected(false);// red background
            }
        }
    }

    public class FriendStatusAdapter extends RecyclerView.Adapter<FriendStatusViewHolder> {
        public FriendStatusAdapter() {
        }

        @NonNull
        @Override
        public FriendStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bottom_sheet_member_list_item, parent, false);
            return new FriendStatusViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendStatusViewHolder holder, final int position) {
            final User user = members.get(position);
            holder.bind(user);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterfaces.navigate(MainActivity.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, user);
                }
            });
        }

        @Override
        public void onBindViewHolder(@NonNull FriendStatusViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (!payloads.isEmpty()) {
                final User user = members.get(position);
                holder.update(user);
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigationInterfaces.navigate(MainActivity.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, user);
                    }
                });
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }

        }

        @Override
        public int getItemCount() {
            return members.size();
        }
    }
}
