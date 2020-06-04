package cf.bautroixa.maptest.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.Discussion;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.ui.adapter.ChatListAdapter;
import cf.bautroixa.maptest.ui.theme.OneLiteAppbarFragment;

public class TabChatListFragment extends OneLiteAppbarFragment {
    ModelManager manager;
    ArrayList<Discussion> discussions;
    ChatListAdapter adapter;
    RecyclerView rvDiscussions;

    public TabChatListFragment() {
        this.manager = ModelManager.getInstance();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
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
        rvDiscussions = view.findViewById(R.id.rv_discussions_frag_chat_list);
        rvDiscussions.setAdapter(adapter);
        rvDiscussions.setLayoutManager(new LinearLayoutManager(requireContext()));
        manager.getDiscussionsManagers().attachAdapter(this, adapter);
    }
}
