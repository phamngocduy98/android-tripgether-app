package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;
import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import java.util.Objects;

import cf.bautroixa.maptest.model.firestore.Discussion;
import cf.bautroixa.maptest.model.firestore.Message;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.presenter.ChatPresenter;
import cf.bautroixa.maptest.ui.adapter.ChatAdapter;

import static cf.bautroixa.maptest.ui.chat.ChatActivity.ARG_DISCUSSION_ID;

public class ChatPresenterImpl implements ChatPresenter {
    private final LifecycleOwner lifecycleOwner;
    Context context;
    View view;
    ModelManager manager;
    Discussion discussion;
    ChatAdapter adapter;
    SortedList<Message> messageSortedList;

    public ChatPresenterImpl(LifecycleOwner lifecycleOwner, Context context, View view) {
        this.lifecycleOwner = lifecycleOwner;
        this.context = context;
        this.view = view;
        this.manager = ModelManager.getInstance();
    }


    @Override
    public boolean handleIntent(Bundle bundle) {
        if (bundle != null) {
            String discussionId = bundle.getString(ARG_DISCUSSION_ID, null);
            if (discussionId != null) {
                discussion = manager.getDiscussionsManagers().get(discussionId);
                view.setupToolbar(discussion);
                return true;
            }
        }
        return false;
    }

    @Override
    public User getUser(String userId) {
        // TODO: use requestGet to make sure it work but may laggy
        return discussion.getMembersManager().get(userId);
    }

    @Override
    public boolean isMe(String userId) {
        return manager.getCurrentUser().getId().equals(userId);
    }

    @Override
    public void initAdapter() {
        adapter = new ChatAdapter(this);
        messageSortedList = new SortedList<>(Message.class, new SortedListAdapterCallback<Message>(adapter) {
            @Override
            public int compare(Message o1, Message o2) {
                if (Objects.equals(o1.getId(), o2.getId())) return 0;
                if ((o1.getTime() == null) ^ (o2.getTime() == null)) {
                    return o1.getTime() == null ? 1 : -1;
                }
                if ((o1.getTime() == null) && (o2.getTime() == null)) return 0;
                return o1.getTime().compareTo(o2.getTime());
            }

            @Override
            public boolean areContentsTheSame(Message oldItem, Message newItem) {
                boolean val = Objects.equals(oldItem.getFromUser(), newItem.getFromUser()) && Objects.equals(oldItem.getText(), newItem.getText()) && Objects.equals(oldItem.getTime(), newItem.getTime());
                return val;
            }

            @Override
            public boolean areItemsTheSame(Message item1, Message item2) {
                boolean val = Objects.equals(item1.getId(), item2.getId());
                return val;
            }
        });
//        messageSortedList.addAll(discussion.getMessagesManager().getList());
        adapter.setMessageSortedList(messageSortedList);
        discussion.getMessagesManager().attachSortedList(lifecycleOwner, messageSortedList);
        view.setUpAdapter(adapter);
//        onMessagesChangedListener = new DatasManager.OnDatasChangedListener<Message>() {
//            @Override
//            public void onItemInserted(int position, Message data) {
//                mAdapter.notifyItemInserted(position);
//                rvMessages.smoothScrollToPosition(position);
//            }
//
//            @Override
//            public void onItemChanged(int position, Message data) {
//
//            }
//
//            @Override
//            public void onItemRemoved(int position, Message data) {
//
//            }
//
//            @Override
//            public void onDataSetChanged(ArrayList<Message> datas) {
//                messages = datas;
//                mAdapter.notifyDataSetChanged();
//                rvMessages.smoothScrollToPosition(datas.size());
//            }
//        };
    }

    @Override
    public void sendMessage(String text) {
        discussion.getMessagesManager().create(new Message(manager.getCurrentUser().getRef(), text));
    }
}
