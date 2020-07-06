package cf.bautroixa.tripgether.ui.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.core.RefsArrayManager;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.adapter.FriendListAdapter;
import cf.bautroixa.tripgether.ui.theme.OneAppbarActivity;

public class FriendListActivity extends OneAppbarActivity implements Toolbar.OnMenuItemClickListener {
    ModelManager manager;
    ArrayList<User> friendsReq, friends;
    FriendListAdapter friendReqAdapter, friendListAdapter;

    RecyclerView rvFriends, rvFriendReq;
    TextView tvHeaderFriendReq, tvHeaderFriendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        tvHeaderFriendReq = findViewById(R.id.tv_header_friend_request);
        rvFriendReq = findViewById(R.id.rv_friend_request_activity_friend_list);
        tvHeaderFriendList = findViewById(R.id.tv_header_friend_list);
        rvFriends = findViewById(R.id.rv_friend_list_activity_friend_list);

        manager = ModelManager.getInstance(this);
        RefsArrayManager<User> friendsReqManager = manager.getCurrentUser().getFriendRequestsManager();
        RefsArrayManager<User> friendsManager = manager.getCurrentUser().getFriendsManager();
        friendsReq = friendsReqManager.getList();
        friends = friendsManager.getList();

        friendsReqManager.attachListener(this, new DocumentsManager.OnListChangedListener<User>() {
            @Override
            public void onListSizeChanged(ArrayList<User> list, int size) {
                tvHeaderFriendReq.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
                rvFriendReq.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
                tvHeaderFriendList.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
            }
        });

        setTitle("Bạn bè");
        setSubtitle(String.format("Bạn có %d lời mời và %d người bạn", friendsReq.size(), friends.size()));
        setToolbarMenu(R.menu.activity_friend_list);
        getToolbar().setOnMenuItemClickListener(this);

        // Friend Request Adapter
        friendReqAdapter = new FriendListAdapter(FriendListActivity.this, friendsReq);
        friendsReqManager.attachAdapter(this, friendReqAdapter);
        rvFriendReq.setAdapter(friendReqAdapter);
        rvFriendReq.setLayoutManager(new LinearLayoutManager(this));

        // Friend List Adapter
        friendListAdapter = new FriendListAdapter(FriendListActivity.this, friends);
        friendsManager.attachAdapter(this, friendListAdapter);
        rvFriends.setAdapter(friendListAdapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_friend_activity_friend_list:
                startActivity(new Intent(this, AddFriendActivity.class));
                break;
        }
        return false;
    }
}
