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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.theme.RoundedImageView;

public class FriendListStatusFragment extends Fragment {

    private static final String TAG = "FriendListStatusFrag";
    private FirebaseFirestore db;
    private HashMap<String, User> friends;

    TextView dragMark;
    SnapHelper snapHelper;

    String userNameSelected = null;

    public interface OnFriendItemClickListener {
        void onClick(User user);
    }

    private OnFriendItemClickListener onFriendItemClickListener = null;
    private RecyclerView rvFriendList, rvFriendListLite;
    private FriendStatusAdapter friendStatusAdapter, friendStatusLiteAdapter;

    public FriendListStatusFragment() {
        friends = new HashMap<>();
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

        // get
        db = FirebaseFirestore.getInstance();
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
                ArrayList<User> userList = new ArrayList<>(friends.values());
                friendStatusAdapter.updateDataSet(userList);
                friendStatusLiteAdapter.updateDataSet(userList);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend_list_status, container, false);
        Context context = v.getContext();

        dragMark = v.findViewById(R.id.drag_mark_frag_friend_list_status);

        rvFriendList = v.findViewById(R.id.rv_friend_list);
        rvFriendListLite = v.findViewById(R.id.rv_friend_list_lite);
        friendStatusAdapter = new FriendStatusAdapter(context, this.onFriendItemClickListener);
        friendStatusLiteAdapter = new FriendStatusLiteAdapter(context, this.onFriendItemClickListener);
        rvFriendList.setAdapter(friendStatusAdapter);
        rvFriendListLite.setAdapter(friendStatusLiteAdapter);
        rvFriendList.setLayoutManager(new LinearLayoutManager(context));
        rvFriendListLite.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvFriendListLite);

        hideToolbar();
        return v;
    }

    public void onBottomSheetStateChanged(int newState) {
        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            showToolbar();
        }
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            hideToolbar();
        }
    }

    public void onSlideBottomSheet(float percent) {
        rvFriendListLite.setTranslationY(-rvFriendListLite.getHeight() * percent);
        rvFriendList.setTranslationY(-rvFriendListLite.getHeight() * percent);
    }

    public void hideToolbar() {
//        dragMark.setVisibility(View.INVISIBLE);
        dragMark.animate().translationY(0).alpha(1);
//        rvFriendListLite.setVisibility(View.VISIBLE);
        rvFriendListLite.animate().translationY(0).alpha(1);
    }

    public void showToolbar() {
//        dragMark.setVisibility(View.VISIBLE);
        dragMark.animate().translationY(dragMark.getHeight()).alpha(0);
//        rvFriendListLite.setVisibility(View.INVISIBLE);
        rvFriendListLite.animate().translationY(rvFriendListLite.getHeight()).alpha(0);
//        rvFriendListLite.setVisibility(View.GONE);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        onFriendItemClickListener = null;
    }

    public static class FriendStatusViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvCount, tvBattery;
        RoundedImageView imgAvatar;
        View view;

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
//            tvCount.setText("1");
            tvBattery.setText(user.getBattery() + "%");
            tvLocation.setText(user.getCurrentLocation());
        }

        public void bind(User user) {
            tvName.setText(user.getName());
            this.update(user);
        }
    }

    public static class FriendStatusAdapter extends RecyclerView.Adapter<FriendStatusViewHolder> {

        Context context;
        ArrayList<User> userList;
        OnFriendItemClickListener onClickListener;

        public FriendStatusAdapter(Context context, OnFriendItemClickListener onFriendItemClickListener) {
            this.context = context;
            this.onClickListener = onFriendItemClickListener;
            this.userList = new ArrayList<>();
            if (onFriendItemClickListener == null) {
                Log.i(TAG, "null onFriendItemClickListener");
            }
        }

        public void setOnFriendItemClickListener(OnFriendItemClickListener onFriendItemClickListener) {
            this.onClickListener = onFriendItemClickListener;
        }

        public void updateDataSet(ArrayList<User> newUserList) {
            this.userList = newUserList;
            this.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FriendStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_status, parent, false);
            return new FriendStatusViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendStatusViewHolder holder, final int position) {
            final User user = this.userList.get(position);
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
                final User user = this.userList.get(position);
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
            return userList.size();
        }
    }

    public static class FriendStatusLiteViewHolder extends FriendStatusViewHolder {
        public FriendStatusLiteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void findView() {
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_friend_status_lite);
            tvCount = itemView.findViewById(R.id.tv_count_status_item_friend);
        }

        @Override
        public void bind(User user) {
            if (user != null) {
//                imgAvatar.setImageDrawable(context.getDrawable(R.drawable.avatar_item_room));
            }
//            tvCount.setText("1");
        }

        @Override
        public void update(User user) {
        }
    }

    public static class FriendStatusLiteAdapter extends FriendStatusAdapter {
        public FriendStatusLiteAdapter(Context context, OnFriendItemClickListener onFriendItemClickListener) {
            super(context, onFriendItemClickListener);
        }

        @NonNull
        @Override
        public FriendStatusLiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_status_lite, parent, false);
            return new FriendStatusLiteViewHolder(v);
        }
    }

    public static class Payload {
        public static final int UPDATED = 1;
    }
}
