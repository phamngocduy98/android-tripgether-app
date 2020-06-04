package cf.bautroixa.maptest.ui.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.RefsArrayManager;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.ui.adapter.FriendListAdapter;
import cf.bautroixa.maptest.ui.theme.OneAppbarActivity;

public class FriendListActivity extends OneAppbarActivity implements Toolbar.OnMenuItemClickListener {
    ModelManager manager;
    ArrayList<User> friends;
    FriendListAdapter adapter;

    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        manager = ModelManager.getInstance();
        RefsArrayManager<User> friendsManager = manager.getCurrentUser().getFriendsManager();
        friends = friendsManager.getList();

        setTitle("Tất cả bạn bè");
        setSubtitle(String.format("Bạn có %d người bạn", friends.size()));
        setToolbarMenu(R.menu.activity_friend_list);
        getToolbar().setOnMenuItemClickListener(this);

        rv = findViewById(R.id.rv_friend_list_activity_friend_list);

        adapter = new FriendListAdapter(FriendListActivity.this, friends);
        friendsManager.attachAdapter(this, adapter);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
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
