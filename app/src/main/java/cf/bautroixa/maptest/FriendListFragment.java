package cf.bautroixa.maptest;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.FireStoreManager;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.ImageHelper;

import static android.content.Context.MODE_PRIVATE;

public class FriendListFragment extends Fragment {

    private static final String TAG = "FriendListStatusFrag";
    private FirebaseFirestore db;
    private FireStoreManager manager;
    private SharedPreferences sharedPref;
    private ArrayList<User> members;
    DatasManager.OnItemInsertedListener onUserInsertedListener;
    DatasManager.OnItemChangedListener onUserChangedListener;
    DatasManager.OnItemRemovedListener onUserRemovedListener;

    TextView dragMark;
    SnapHelper snapHelper;

    String userNameSelected = null;

    public interface OnFriendItemClickListener {
        void onClick(User user);
    }

    private OnFriendItemClickListener onFriendItemClickListener = null;
    private RecyclerView rvFriendList, rvFriendListLite;
    private FriendStatusAdapter friendStatusAdapter, friendStatusLiteAdapter;

    public FriendListFragment() {
        members = new ArrayList<>();
    }

    public void setOnFriendItemClickListener(OnFriendItemClickListener onFriendItemClickListener) {
        this.onFriendItemClickListener = onFriendItemClickListener;
        if (this.friendStatusAdapter != null) {
            this.friendStatusAdapter.setOnFriendItemClickListener(onFriendItemClickListener);
        }
        if (this.friendStatusLiteAdapter != null) {
            this.friendStatusLiteAdapter.setOnFriendItemClickListener(onFriendItemClickListener);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        sharedPref = getContext().getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        manager = FireStoreManager.getInstance(sharedPref.getString(User.USER_NAME, User.NO_USER));
        members = manager.getMembers();
        friendStatusAdapter = new FriendStatusAdapter(getContext(), this.onFriendItemClickListener);
        friendStatusLiteAdapter = new FriendStatusLiteAdapter(getContext(), this.onFriendItemClickListener);

        onUserInsertedListener = new DatasManager.OnItemInsertedListener() {
            @Override
            public void onItemInserted(int position) {
                friendStatusAdapter.notifyItemInserted(position);
                friendStatusLiteAdapter.notifyItemInserted(position);
                Log.d(TAG, "insert"+position);
            }
        };
        onUserChangedListener = new DatasManager.OnItemChangedListener() {
            @Override
            public void onItemChanged(int position) {
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
        manager.getMembersManager().addOnItemInsertedListener(onUserInsertedListener)
                .addOnItemChangedListener(onUserChangedListener)
                .addOnItemRemovedListener(onUserRemovedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getMembersManager().removeOnItemInsertedListener(onUserInsertedListener)
                .removeOnItemChangedListener(onUserChangedListener)
                .removeOnItemRemovedListener(onUserRemovedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend_list, container, false);

        dragMark = v.findViewById(R.id.drag_mark_frag_friend_list_status);

        rvFriendList = v.findViewById(R.id.rv_friend_list);
        rvFriendListLite = v.findViewById(R.id.rv_friend_list_lite);
        rvFriendList.setAdapter(friendStatusAdapter);
        rvFriendListLite.setAdapter(friendStatusLiteAdapter);
        rvFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendListLite.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvFriendListLite);

        ViewAnim.toggleHideShow(dragMark, true, ViewAnim.DIRECTION_UP);
        ViewAnim.toggleHideShow(rvFriendListLite, true, ViewAnim.DIRECTION_UP);

        return v;
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
        rvFriendListLite.setAlpha(percent);
        rvFriendList.setTranslationY(-rvFriendListLite.getHeight() * percent);
        rvFriendList.setAlpha(percent);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        onFriendItemClickListener = null;
    }

    public class FriendStatusViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvCount, tvBattery;
        RoundedImageView imgAvatar;
        View view;
        User currentUser;

        public FriendStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            this.findView();
        }

        public void findView() {
            imgAvatar = itemView.findViewById(R.id.img_avatar_status_item_friend);
            tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
            tvBattery = itemView.findViewById(R.id.tv_battery_status_item_friend);
            tvLocation = itemView.findViewById(R.id.tv_location_status_item_friend);
            tvCount = itemView.findViewById(R.id.tv_count_status_item_friend);
        }

        public void update(User user) {
            tvBattery.setText(user.getBattery() + "%");
            tvLocation.setText(user.getCurrentLocation());
            if (!user.getAvatar().equals(currentUser.getAvatar())) {
                ImageHelper.loadImage(user.getAvatar(), imgAvatar);
            }
            if (user.getId().equals(manager.getCurrentTrip().getLeader().getId())){
                tvCount.setText("LEADER");
            } else {
                tvCount.setVisibility(View.GONE);
            }
            currentUser = user;
        }

        public void bind(User user) {
            currentUser = user;
            tvName.setText(user.getName());
            ImageHelper.loadImage(user.getAvatar(), imgAvatar);
            this.update(user);
        }
    }

    public class FriendStatusAdapter extends RecyclerView.Adapter<FriendStatusViewHolder> {

        Context context;
        OnFriendItemClickListener onClickListener;

        public FriendStatusAdapter(Context context, OnFriendItemClickListener onFriendItemClickListener) {
            this.context = context;
            this.onClickListener = onFriendItemClickListener;
            if (onFriendItemClickListener == null) {
                Log.i(TAG, "null onFriendItemClickListener");
            }
        }

        public void setOnFriendItemClickListener(OnFriendItemClickListener onFriendItemClickListener) {
            this.onClickListener = onFriendItemClickListener;
        }

        @NonNull
        @Override
        public FriendStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_list_item, parent, false);
            return new FriendStatusViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendStatusViewHolder holder, final int position) {
            final User user = members.get(position);
            holder.bind(user);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(user);
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
                        onClickListener.onClick(user);
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

    public class FriendStatusLiteViewHolder extends FriendStatusViewHolder {
        public FriendStatusLiteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void findView() {
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_friend_status_lite);
            tvCount = itemView.findViewById(R.id.tv_messages_count_item_friend_status_lite);
        }

        @Override
        public void bind(User user) {
            ImageHelper.loadImage(user.getAvatar(), imgAvatar);
            if (user.getId().equals(manager.getCurrentTrip().getLeader().getId())){
                tvCount.setText("L");
            } else {
                tvCount.setVisibility(View.GONE);
            }
        }

        @Override
        public void update(User user) {
        }
    }

    public class FriendStatusLiteAdapter extends FriendStatusAdapter {
        public FriendStatusLiteAdapter(Context context, OnFriendItemClickListener onFriendItemClickListener) {
            super(context, onFriendItemClickListener);
        }

        @NonNull
        @Override
        public FriendStatusLiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_list_item_lite, parent, false);
            return new FriendStatusLiteViewHolder(v);
        }
    }

    public static class Payload {
        public static final int UPDATED = 1;
    }
}
