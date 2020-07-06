package cf.bautroixa.maptest.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.ActivityNavigationInterface;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.objects.Notification;
import cf.bautroixa.maptest.model.firestore.objects.UserNotification;
import cf.bautroixa.maptest.ui.friends.ProfileActivity;
import cf.bautroixa.maptest.ui.theme.OneRecyclerView;
import cf.bautroixa.maptest.ui.theme.RoundedImageView;
import cf.bautroixa.maptest.ui.trip_view.TripActivity;
import cf.bautroixa.maptest.utils.ui_utils.DateFormatter;
import cf.bautroixa.maptest.utils.ui_utils.ImageHelper;

public class UserNotificationAdapter extends OneRecyclerView.Adapter<UserNotificationAdapter.NotificationVH> {
    ModelManager manager;
    Context context;
    ActivityNavigationInterface navigationInterface;
    private SortedList<UserNotification> userNotifications;

    public UserNotificationAdapter(Context context, ActivityNavigationInterface navigationInterface) {
        this.manager = ModelManager.getInstance(context);
        this.context = context;
        this.navigationInterface = navigationInterface;
    }

    public void setUserNotifications(SortedList<UserNotification> userNotifications) {
        this.userNotifications = userNotifications;
    }

    @NonNull
    @Override
    public NotificationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_tab_notification_item, parent, false);
        return new NotificationVH(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationVH holder, final int position) {
        final UserNotification userNotification = userNotifications.get(position);
        holder.bind(userNotification);
    }

    @Override
    public int getItemCount() {
        return userNotifications.size();
    }

    public class NotificationVH extends OneRecyclerView.ViewHolder {
        View badgeSeen;
        RoundedImageView imgAvatar, imgType;
        TextView tvContent, tvTime;

        public NotificationVH(@NonNull View itemView, int viewType) {
            super(itemView, viewType);
            badgeSeen = itemView.findViewById(R.id.badge_seen_item_noti);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_noti);
            imgType = itemView.findViewById(R.id.img_type_item_noti);
            tvContent = itemView.findViewById(R.id.tv_content_item_noti);
            tvTime = itemView.findViewById(R.id.tv_time_item_noti);
        }

        public void updateSeen(UserNotification userNotification) {
            if (!userNotification.isSeen()) {
                userNotification.sendUpdate(null, Notification.SEEN, true);
            }
        }

        public void bind(final UserNotification userNotification) {
            if (userNotification == null) return;
            tvContent.setText(Html.fromHtml(userNotification.getRenderedMessage(context, !userNotification.isSeen())));
            tvTime.setText(DateFormatter.format(userNotification.getTime()));
            ImageHelper.loadCircleImage(userNotification.getAvatar(), imgAvatar);
            int typeIndex = Notification.UserType.userTypes.indexOf(userNotification.getType());
            imgType.setImageResource(Notification.UserIcon.userIcons.get(typeIndex));
            badgeSeen.setVisibility(userNotification.isSeen() ? View.INVISIBLE : View.VISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSeen(userNotification);
                    Intent intent;
                    switch (userNotification.getType()) {
                        case UserNotification.UserType.ADD_FRIEND:
                        case UserNotification.UserType.FRIEND_ACCEPTED:
                            intent = new Intent(context, ProfileActivity.class);
                            intent.putExtra(ProfileActivity.ARG_USER_ID, userNotification.getUserRef().getId());
                            context.startActivity(intent);
                            break;
                        case UserNotification.UserType.INVITE_TO_TRIP:
                        case UserNotification.UserType.TRIP_JOIN_REJECTED:
                            intent = new Intent(context, TripActivity.class);
                            intent.putExtra(TripActivity.ARG_TRIP_ID, userNotification.getTripRef().getId());
                            context.startActivity(intent);
                            break;
                        case UserNotification.UserType.TRIP_JOIN_ACCEPTED:
                            break;
                    }

                }
            });

        }
    }
}
