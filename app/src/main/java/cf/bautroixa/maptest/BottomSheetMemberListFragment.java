package cf.bautroixa.maptest;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.firestore.Visit;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.ImageHelper;

public class BottomSheetMemberListFragment extends Fragment {
    // const
    private static final String TAG = "FriendListStatusFrag";

    // data and state
    private MainAppManager manager;
    private ArrayList<User> members;

    // listener
    private DatasManager.OnItemInsertedListener<User> onUserInsertedListener;
    private DatasManager.OnItemChangedListener<User> onUserChangedListener;
    private DatasManager.OnItemRemovedListener<User> onUserRemovedListener;
    private OnDataItemSelected<User> onFriendItemClickListener = null;
    private OnFilterUser onFilterUser, defaultUserFilter;

    // view
    private TextView dragMark;
    private RecyclerView rvFriendList, rvFriendListLite;

    // adapter
    private FriendStatusAdapter friendStatusAdapter, friendStatusLiteAdapter;

    public BottomSheetMemberListFragment() {
        members = new ArrayList<>();
        defaultUserFilter = new OnFilterUser() {
            @Override
            public boolean onUserFiltering(User user) {
                return true;
            }
        };
        onFilterUser = defaultUserFilter;
    }

    public void setOnFriendItemClickListener(OnDataItemSelected<User> onFriendItemClickListener) {
        this.onFriendItemClickListener = onFriendItemClickListener;
    }

    public void applyFilter(OnFilterUser onFilterUser) {
        this.onFilterUser = onFilterUser;
        members.clear();
        for (User user : manager.getMembers()) {
            if (onFilterUser.onUserFiltering(user)) {
                members.add(user);
            }
        }
        if (this.friendStatusAdapter != null) this.friendStatusAdapter.notifyDataSetChanged();
        if (this.friendStatusLiteAdapter != null)
            this.friendStatusLiteAdapter.notifyDataSetChanged();
    }

    public void removeFilter() {
        onFilterUser = defaultUserFilter;
        members.clear();
        members.addAll(manager.getMembers());
        if (this.friendStatusAdapter != null) this.friendStatusAdapter.notifyDataSetChanged();
        if (this.friendStatusLiteAdapter != null)
            this.friendStatusLiteAdapter.notifyDataSetChanged();
    }

    public void onBottomSheetStateChanged(int newState) {
        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            // hide Lite list
            ViewAnim.toggleHideShow(dragMark, false, ViewAnim.DIRECTION_UP);
            ViewAnim.toggleHideShow(rvFriendListLite, false, ViewAnim.DIRECTION_UP);
        }
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            // show lite list
            ViewAnim.toggleHideShow(dragMark, true, ViewAnim.DIRECTION_UP);
            ViewAnim.toggleHideShow(rvFriendListLite, true, ViewAnim.DIRECTION_UP);
        }
    }

    public void onSlideBottomSheet(float percent) {
        rvFriendListLite.setTranslationY(-rvFriendListLite.getHeight() * percent);
        rvFriendListLite.setAlpha(1 - percent);
        rvFriendList.setTranslationY(-rvFriendListLite.getHeight() * percent);
        rvFriendList.setAlpha(percent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = MainAppManager.getInstance();
        members = new ArrayList<>();
        members.addAll(manager.getMembers());
        friendStatusAdapter = new FriendStatusAdapter();
        friendStatusLiteAdapter = new FriendStatusLiteAdapter();

        onUserInsertedListener = new DatasManager.OnItemInsertedListener<User>() {
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
        };
        onUserChangedListener = new DatasManager.OnItemChangedListener<User>() {
            @Override
            public void onItemChanged(int position, User data) {
                friendStatusAdapter.notifyItemChanged(position);
                friendStatusLiteAdapter.notifyItemChanged(position);
                Log.d(TAG, "change"+position);
            }
        };
        onUserRemovedListener = new DatasManager.OnItemRemovedListener<User>() {
            @Override
            public void onItemRemoved(int position, User data) {
                friendStatusAdapter.notifyItemRemoved(position);
                friendStatusLiteAdapter.notifyItemRemoved(position);
                Log.d(TAG, "remove"+position);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getMembersManager().addOnItemInsertedListener(onUserInsertedListener);
        manager.getMembersManager().addOnItemChangedListener(onUserChangedListener);
        manager.getMembersManager().addOnItemRemovedListener(onUserRemovedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getMembersManager().removeOnItemInsertedListener(onUserInsertedListener);
        manager.getMembersManager().removeOnItemChangedListener(onUserChangedListener);
        manager.getMembersManager().removeOnItemRemovedListener(onUserRemovedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bottom_sheet_member_list, container, false);

        dragMark = v.findViewById(R.id.drag_mark_frag_friend_list_status);

        rvFriendList = v.findViewById(R.id.rv_friend_list);
        rvFriendListLite = v.findViewById(R.id.rv_friend_list_lite);
        rvFriendList.setAdapter(friendStatusAdapter);
        rvFriendListLite.setAdapter(friendStatusLiteAdapter);
        rvFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendListLite.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvFriendListLite);

        ViewAnim.toggleHideShow(dragMark, true, ViewAnim.DIRECTION_UP);
        ViewAnim.toggleHideShow(rvFriendListLite, true, ViewAnim.DIRECTION_UP);

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFriendItemClickListener = null;
        this.removeFilter();
    }

    public interface OnFilterUser {
        boolean onUserFiltering(User user);
    }

    public static class Payload {
        public static final int UPDATED = 1;
    }

    public class FriendStatusViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvName, tvLocation, tvCount, tvLastUpdate, tvNameInAvatar;
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
        }

        public void update(User user) {
            if (user.getLastUpdate() != null){
                tvLastUpdate.setText(DateFormatter.format(user.getLastUpdate()));
            }
            progressBattery.setProgress(user.getBattery());
            tvLocation.setText(user.getCurrentLocation());
            if (!user.getAvatar().equals(currentUser.getAvatar())) {
                ImageHelper.loadImage(user.getAvatar(), imgAvatar);
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
                    // TODO: userVisit.getTime() may NULL here OR NOT :V
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
            ImageHelper.loadImage(user.getAvatar(), imgAvatar);
            this.update(user);
        }
    }

    public class FriendStatusLiteViewHolder extends FriendStatusViewHolder {
        public FriendStatusLiteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void findView() {
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_friend_status_lite);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_friend_status_lite);
            tvCount = itemView.findViewById(R.id.tv_messages_count_item_friend_status_lite);
        }

        @Override
        public void bind(User user) {
            ImageHelper.loadImage(user.getAvatar(), imgAvatar);
            if (user.getId().equals(manager.getCurrentTrip().getLeader().getId())) {
                tvCount.setText("L");
            } else {
                tvCount.setVisibility(View.GONE);
            }
        }

        @Override
        public void update(User user) {
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
                    onFriendItemClickListener.selectItem(user);
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
                        onFriendItemClickListener.selectItem(user);
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

    public class FriendStatusLiteAdapter extends FriendStatusAdapter {

        @NonNull
        @Override
        public FriendStatusLiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bottom_sheet_member_list_item_lite, parent, false);
            return new FriendStatusLiteViewHolder(v);
        }
    }
}
