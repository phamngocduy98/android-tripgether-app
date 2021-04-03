package cf.bautroixa.tripgether.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Discussion;
import cf.bautroixa.tripgether.model.firestore.objects.Message;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.chat.ChatActivity;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelperExt;
import cf.bautroixa.ui.RoundedImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.DiscussionVH> {
    Context context;
    ArrayList<Discussion> discussions;
    User currentUser;

    public ChatListAdapter(Context context, ArrayList<Discussion> discussions, User currentUser) {
        this.context = context;
        this.discussions = discussions;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public DiscussionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiscussionVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DiscussionVH holder, int position) {
        holder.bind(discussions.get(position));
    }

    @Override
    public int getItemCount() {
        return discussions.size();
    }

    public class DiscussionVH extends RecyclerView.ViewHolder {
        //        protected CircularProgressBar progressBattery;
        protected TextView tvNameInAvatar, tvOnlineIndicator;
        protected RoundedImageView imgAvatar;
//        protected ImageView imgIsLeader;

        protected TextView tvName, tvLastChat, tvLastUpdate;
        protected ImageView imgIsSent;
        protected RipplePulseLayout ripplePulseSos;
        private Checkpoint activeCheckpoint;

        public DiscussionVH(@NonNull View itemView) {
            super(itemView);
//            progressBattery = itemView.findViewById(R.id.progress_battery_item_friend);
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_avatar);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_avatar);
//            imgIsLeader = itemView.findViewById(R.id.img_is_leader_item_friend);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_avatar);

            ripplePulseSos = itemView.findViewById(R.id.ripple_pulse_sos);

            tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
            tvLastChat = itemView.findViewById(R.id.tv_location_status_item_friend);
            tvLastUpdate = itemView.findViewById(R.id.tv_last_update_item_friend);
            imgIsSent = itemView.findViewById(R.id.img_ticker_item_friend);
        }

        public void bind(final Discussion discussion) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra(ChatActivity.ARG_DISCUSSION_ID, discussion.getId());
                    context.startActivity(intent);
                }
            });
            if (discussion.getTripRef() != null) {
                // trip group discussion
                imgAvatar.setVisibility(View.INVISIBLE);
                tvName.setText(discussion.getName());
                tvNameInAvatar.setText(discussion.getName().substring(0, 1));
            } else if (discussion.getMembers().size() == 2) {
                for (User user : discussion.getMembersManager().getList()) {
                    if (!user.getId().equals(currentUser.getId())) {
                        bindUser(user);
                    }
                }
            }

            // latest message
            Message latestMessage = discussion.getMessagesManager().getLatestMessage();
            if (latestMessage != null) {
                tvLastChat.setText(latestMessage.getText());
                if (latestMessage.getTime() != null) {
                    tvLastUpdate.setText(DateFormatter.format(latestMessage.getTime()));
                } else {
                    tvLastUpdate.setText("");
                }
            } else {
                tvLastChat.setText("Không có tin nhắn");
                tvLastUpdate.setVisibility(View.GONE);
            }
        }

        public void bindUser(User user) {
            // name
            tvName.setText(user.getName());
            // online
            Timestamp lastUpdate = user.getLastUpdate();
            if (lastUpdate != null) {
                boolean isOnline = Calendar.getInstance().getTimeInMillis() - lastUpdate.toDate().getTime() < 5 * 60 * 1000; // online in less than 5 mins
                tvOnlineIndicator.setSelected(isOnline); // green background / red background
            }
            // avatar
            ImageHelperExt.loadUserAvatar(imgAvatar, tvNameInAvatar, user);
        }
    }
}
