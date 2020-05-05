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
import java.util.Calendar;
import java.util.Objects;

import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.Message;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.ImageHelper;

public class TabChatFragment extends Fragment {
    MainAppManager manager;
    ArrayList<Message> messages;

    DatasManager.OnDatasChangedListener<Message> onMessagesChangedListener;

    private MessagesAdapter mAdapter;
    private RecyclerView rvMessages;
    private EditText editMessage;

    public TabChatFragment() {
        manager = MainAppManager.getInstance();
        messages = manager.getMessagesManager().getData();
        onMessagesChangedListener = new DatasManager.OnDatasChangedListener<Message>() {
            @Override
            public void onItemInserted(int position, Message data) {
                mAdapter.notifyItemInserted(position);
                rvMessages.smoothScrollToPosition(position);
            }

            @Override
            public void onItemChanged(int position, Message data) {

            }

            @Override
            public void onItemRemoved(int position, Message data) {

            }

            @Override
            public void onDataSetChanged(ArrayList<Message> datas) {
                mAdapter.notifyDataSetChanged();
                rvMessages.smoothScrollToPosition(datas.size());
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
        rvMessages = v.findViewById(R.id.rv_messages);
        mAdapter = new MessagesAdapter();
        rvMessages.setAdapter(mAdapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getMessagesManager().addOnDatasChangedListener(onMessagesChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getMessagesManager().removeOnDatasChangedListener(onMessagesChangedListener);
    }

    public interface MessageViewType {
        int NORMAL = 0;
        int CONTINUOUS = 1;
    }

    /**
     * Message List Recycler View: ViewHolder
     */
    public class MessagesViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageContent, tvMessageSender, tvMessageTime;
        LinearLayout linearContainerMessageItem, linearMainMessageItem;
        RoundedImageView imgMessageItemAvatar;
        View view;

        public MessagesViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            view = itemView;
            linearContainerMessageItem = itemView.findViewById(R.id.linear_container_message_item);
            tvMessageContent = itemView.findViewById(R.id.tv_content_item_message);
            tvMessageSender = itemView.findViewById(R.id.tv_sender_item_message);
            linearMainMessageItem = itemView.findViewById(R.id.linear_main_message_item);
            imgMessageItemAvatar = itemView.findViewById(R.id.img_avatar_item_message);
            tvMessageTime = itemView.findViewById(R.id.tv_time_item_message);

            if (viewType == MessageViewType.CONTINUOUS) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearContainerMessageItem.getLayoutParams();
                layoutParams.topMargin = 0;
                layoutParams.bottomMargin = 0;
                linearContainerMessageItem.setLayoutParams(layoutParams);
                tvMessageSender.setVisibility(View.GONE);
                imgMessageItemAvatar.setVisibility(View.INVISIBLE);
            }
        }

        public void bind(Message message) {
            tvMessageContent.setText(message.getText());
            User sender = manager.getMembersManager().get(message.getFromUser().getId());
            Calendar calendar = Calendar.getInstance();
            if (message.getTime() != null && calendar.getTimeInMillis() - message.getTime().toDate().getTime() > 15 * 60 * 1000) { // 15 minutes
                tvMessageTime.setText(DateFormatter.formatDateTime(message.getTime()));
            } else {
                tvMessageTime.setText("");
            }
            if (sender != null) {
                tvMessageSender.setText(sender.getName());
                ImageHelper.loadCircleImage(sender.getAvatar(), imgMessageItemAvatar);
            }
            if (message.getFromUser().getId().equals(manager.getCurrentUser().getId())) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearMainMessageItem.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                linearMainMessageItem.setLayoutParams(layoutParams);
                tvMessageSender.setVisibility(View.GONE);
                imgMessageItemAvatar.setVisibility(View.GONE);
                tvMessageContent.setTextColor(Color.WHITE);
                tvMessageContent.setBackground(getResources().getDrawable(R.drawable.bg_radius_full_color));

                ArrayList<View> views = new ArrayList<View>();
                for (int x = 0; x < linearMainMessageItem.getChildCount(); x++) {
                    views.add(linearMainMessageItem.getChildAt(x));
                }
                linearMainMessageItem.removeAllViews();
                for (int x = views.size() - 1; x >= 0; x--) {
                    linearMainMessageItem.addView(views.get(x));
                }
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
            return new MessagesViewHolder(view, viewType);
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

        @Override
        public int getItemViewType(int position) {
            if (position > 0 && Objects.equals(messages.get(position - 1).getFromUser().getId(), messages.get(position).getFromUser().getId())) {
                return MessageViewType.CONTINUOUS;
            }
            return MessageViewType.NORMAL;
        }
    }
}
