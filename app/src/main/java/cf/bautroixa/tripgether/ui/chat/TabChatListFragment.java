package cf.bautroixa.tripgether.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Discussion;
import cf.bautroixa.tripgether.ui.adapter.ChatListAdapter;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.friends.FriendListActivity;
import cf.bautroixa.ui.OneLiteAppbarFragment;

public class TabChatListFragment extends OneLiteAppbarFragment implements Toolbar.OnMenuItemClickListener, NavigationInterfaceOwner {
    ModelManager manager;
    ArrayList<Discussion> discussions;
    ChatListAdapter adapter;
    RecyclerView rvDiscussions;
    private NavigationInterface mNavigationInterface;

    public TabChatListFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.manager = ModelManager.getInstance(context);
        discussions = this.manager.getDiscussionsManagers().getList();
        adapter = new ChatListAdapter(context, discussions, manager.getCurrentUser());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("Tin nhắn");
        setBackButtonIcon(R.drawable.ic_menu_black_24dp);
        setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNavigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_ANY, MainActivityPagerAdapter.Tabs.STATE_OPEN_DRAWER);
            }
        });
        setToolbarMenu(R.menu.fragment_chat_list);
        getToolbar().setOnMenuItemClickListener(this);
        rvDiscussions = view.findViewById(R.id.rv_discussions_frag_chat_list);
        rvDiscussions.setAdapter(adapter);
        rvDiscussions.setLayoutManager(new LinearLayoutManager(requireContext()));
        manager.getDiscussionsManagers().attachAdapter(this, adapter);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_discussion) {
            Intent intent = new Intent(requireContext(), FriendListActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.mNavigationInterface = navigationInterface;
    }
}
