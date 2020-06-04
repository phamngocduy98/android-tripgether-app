package cf.bautroixa.maptest.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.Calendar;
import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.Message;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.presenter.impl.ChatPresenterImpl;
import cf.bautroixa.maptest.ui.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.ImageHelper;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessagesViewHolder> {
    ChatPresenterImpl chatPresenter;
    SortedList<Message> messageSortedList;

    public ChatAdapter(ChatPresenterImpl chatPresenter) {
        this.chatPresenter = chatPresenter;
    }

    public void setMessageSortedList(SortedList<Message> messageSortedList) {
        this.messageSortedList = messageSortedList;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessagesViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder holder, int position) {
        final Message message = messageSortedList.get(position);
        boolean isEnd = position + 1 == messageSortedList.size();
        if (position > 0) {
            holder.bind(messageSortedList.get(position - 1), message, isEnd);
        } else {
            holder.bind(null, message, isEnd);
        }
    }

    @Override
    public int getItemCount() {
        return messageSortedList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0 && Objects.equals(messageSortedList.get(position - 1).getFromUser().getId(), messageSortedList.get(position).getFromUser().getId())) {
            return MessageViewType.CONTINUOUS;
        }
        return MessageViewType.NORMAL;
    }

    public interface MessageViewType {
        int NORMAL = 0;
        int CONTINUOUS = 1;
    }

    /**
     * Message List Recycler View: ViewHolder
     */
    public class MessagesViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvMessageSender, tvMessageContent, tvMessageContent2, tvMessageTime, tvMessageTime2;
        LinearLayout linearContainerMessageItem, linearIncomingMessageItem, linearOutcomingMessageItem;
        RoundedImageView imgMessageItemAvatar, imgSeen;
        View view;
        String lastAvatar = "";

        public MessagesViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            view = itemView;
            linearContainerMessageItem = itemView.findViewById(R.id.linear_container_message_item);
            tvDate = itemView.findViewById(R.id.tv_date_item_message);

            linearIncomingMessageItem = itemView.findViewById(R.id.linear_incoming_message_item);
            imgMessageItemAvatar = itemView.findViewById(R.id.img_avatar_item_message);
            tvMessageSender = itemView.findViewById(R.id.tv_sender_item_message);
            tvMessageContent = itemView.findViewById(R.id.tv_content_item_message);
            tvMessageTime = itemView.findViewById(R.id.tv_time_item_message);

            linearOutcomingMessageItem = itemView.findViewById(R.id.linear_outcoming_message_item);
            tvMessageContent2 = itemView.findViewById(R.id.tv_content2_item_message);
            tvMessageTime2 = itemView.findViewById(R.id.tv_time2_item_message);
            imgSeen = itemView.findViewById(R.id.img_seen_item_message);

            if (viewType == MessageViewType.CONTINUOUS) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearContainerMessageItem.getLayoutParams();
                layoutParams.topMargin = 0;
                layoutParams.bottomMargin = 0;
                linearContainerMessageItem.setLayoutParams(layoutParams);
                tvMessageSender.setVisibility(View.GONE);
                imgMessageItemAvatar.setVisibility(View.INVISIBLE);
            }
        }

        public void bind(@Nullable Message preMessage, Message message, boolean isEnd) {
            Context context = itemView.getContext();
            User sender = chatPresenter.getUser((message.getFromUser().getId()));

            Calendar calendar = Calendar.getInstance();
            String timeText = "";
            if (message.getTime() != null && calendar.getTimeInMillis() - message.getTime().toDate().getTime() > 15 * 60 * 1000) {
                // 15 minutes
                timeText = DateFormatter.formatTime(message.getTime());
            }
            if (message.getTime() != null && (preMessage == null || (preMessage.getTime() != null && message.getTime().getSeconds() - preMessage.getTime().getSeconds() >= 24 * 60 * 60))) {
                // 1 day
                tvDate.setText(DateFormatter.formatDate(message.getTime()));
                tvDate.setVisibility(View.VISIBLE);
            } else {
                tvDate.setVisibility(View.GONE);
            }

            if (chatPresenter.isMe(message.getFromUser().getId())) {
                bindOutcomingMessage(message, sender, timeText, isEnd);
                linearIncomingMessageItem.setVisibility(View.GONE);
                linearOutcomingMessageItem.setVisibility(View.VISIBLE);
            } else {
                bindIncomingMessage(message, sender, timeText);
                linearIncomingMessageItem.setVisibility(View.VISIBLE);
                linearOutcomingMessageItem.setVisibility(View.GONE);
            }
        }

        public void bindIncomingMessage(Message message, User sender, String timeText) {
            tvMessageContent.setText(message.getText());
            tvMessageTime.setText(timeText);
            tvMessageSender.setText(sender.getName());

            if (!lastAvatar.equals(sender.getAvatar())) {
                ImageHelper.loadCircleImage(sender.getAvatar(), imgMessageItemAvatar);
                lastAvatar = sender.getAvatar();
            }
        }

        public void bindOutcomingMessage(Message message, User sender, String timeText, boolean isEnd) {
            if (tvMessageSender.getVisibility() == View.VISIBLE) {
                tvMessageSender.setVisibility(View.GONE);
            }
            tvMessageContent2.setText(message.getText());
            tvMessageTime2.setText(timeText);
            if (isEnd && message.getTime() != null) {
                imgSeen.setVisibility(View.VISIBLE);
            } else {
                if (imgSeen.getVisibility() == View.VISIBLE) imgSeen.setVisibility(View.INVISIBLE);
            }
        }
    }
}
