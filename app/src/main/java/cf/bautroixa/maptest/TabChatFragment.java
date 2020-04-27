package cf.bautroixa.maptest;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.Message;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ImageHelper;

public class TabChatFragment extends Fragment {
    MainAppManager manager;
    ArrayList<Message> messages;

    DatasManager.OnItemInsertedListener<Message> onReceiveMessage;
    DatasManager.OnDataSetChangedListener<Message> onResetMessages;

    private MessagesAdapter mAdapter;
    private EditText editMessage;

    public TabChatFragment() {
        manager = MainAppManager.getInstance();
        messages = manager.getMessagesManager().getData();
        onReceiveMessage = new DatasManager.OnItemInsertedListener<Message>() {
            @Override
            public void onItemInserted(int position, Message data) {
                mAdapter.notifyItemInserted(position);
            }
        };
        onResetMessages = new DatasManager.OnDataSetChangedListener<Message>() {
            @Override
            public void onDataSetChanged(ArrayList<Message> datas) {
                mAdapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_chat, container, false);
        editMessage = v.findViewById(R.id.edit_enter_message_frag_chat);
        Button btnSendMessage = v.findViewById(R.id.btn_send_frag_chat);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editMessage.getText().toString();
                if (messageText.length() == 0) return;
                manager.getMessagesManager().create(null, new Message(manager.getCurrentUser().getRef(), messageText));
                editMessage.setText("");
            }
        });
        RecyclerView rvMessages = v.findViewById(R.id.rv_messages);
        mAdapter = new MessagesAdapter();
        rvMessages.setAdapter(mAdapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getMessagesManager().addOnItemInsertedListener(onReceiveMessage)
                .addOnDataSetChangedListener(onResetMessages);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getMessagesManager().removeOnItemInsertedListener(onReceiveMessage)
                .removeOnDataSetChangedListener(onResetMessages);
    }

    /**
     * Message List Recycler View: ViewHolder
     */
    public class MessagesViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessageContent, txtMessageSender;
        LinearLayout boxMessageItem, boxMessageItemText;
        RoundedImageView imgMessageItemAvatar;
        View view;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            txtMessageContent = itemView.findViewById(R.id.tv_message_item_content);
            txtMessageSender = itemView.findViewById(R.id.tv_message_item_sender);
            boxMessageItem = itemView.findViewById(R.id.box_message_item);
            imgMessageItemAvatar = itemView.findViewById(R.id.img_message_item_avatar);
            boxMessageItemText = itemView.findViewById(R.id.box_message_item_text);
        }

        public void bind(Message message) {
            txtMessageContent.setText(message.getText());
            User sender = manager.getMembersManager().get(message.getFromUser().getId());
            if (sender != null) {
                txtMessageSender.setText(sender.getName());
                ImageHelper.loadImage(sender.getAvatar(), imgMessageItemAvatar);
            }
            if (message.getFromUser().getId().equals(manager.getCurrentUser().getId())) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) boxMessageItem.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                boxMessageItem.setLayoutParams(layoutParams);
                txtMessageContent.setTextColor(Color.WHITE);
                txtMessageSender.setVisibility(View.GONE);
                imgMessageItemAvatar.setVisibility(View.GONE);
                boxMessageItemText.setBackground(getResources().getDrawable(R.drawable.bg_item_message_outcoming));
            }
        }
    }

    /**
     * Chat Recycler View: Adapter
     */
    public class MessagesAdapter extends RecyclerView.Adapter<MessagesViewHolder> {
        public MessagesAdapter() {
        }

        @NonNull
        @Override
        public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_message, parent, false);
            return new MessagesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MessagesViewHolder holder, int position) {
            final Message message = messages.get(position);
            holder.bind(message);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }
}
