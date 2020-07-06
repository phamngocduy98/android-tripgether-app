package cf.bautroixa.tripgether.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedList;

import com.google.firebase.firestore.FieldValue;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.ActivityNavigationInterface;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Notification;
import cf.bautroixa.tripgether.model.firestore.objects.TripNotification;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.firestore.objects.UserNotification;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.map.TabMapFragment;
import cf.bautroixa.tripgether.ui.theme.OneRecyclerView;
import cf.bautroixa.tripgether.ui.theme.RoundedImageView;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class TripNotificationAdapter extends OneRecyclerView.Adapter<TripNotificationAdapter.NotificationVH> {
    ModelManager manager;
    Context context;
    ActivityNavigationInterface navigationInterface;
    private SortedList<TripNotification> tripNotifications;

    public TripNotificationAdapter(Context context, ActivityNavigationInterface navigationInterface) {
        this.manager = ModelManager.getInstance(context);
        this.context = context;
        this.navigationInterface = navigationInterface;
    }

    public void setTripNotifications(SortedList<TripNotification> tripNotifications) {
        this.tripNotifications = tripNotifications;
    }

    @NonNull
    @Override
    public NotificationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_tab_notification_item, parent, false);
        return new NotificationVH(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationVH holder, final int position) {
        final TripNotification tripNotification = tripNotifications.get(position);
        holder.bind(tripNotification);
    }

    @Override
    public int getItemCount() {
        return tripNotifications.size();
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

        public void updateSeen(TripNotification tripNotification) {
            if (!tripNotification.getSeenList().contains(manager.getCurrentUserRef())) {
                tripNotification.sendUpdate(null, TripNotification.SEEN_LIST, FieldValue.arrayUnion(manager.getCurrentUserRef()));
            }
        }

        public void bind(final TripNotification tripNotification) {
            if (tripNotification == null) return;
            boolean isSeen = tripNotification.getSeenList().contains(manager.getCurrentUserRef());
//            boolean isSeen = tripNotification.isSeen();
            tvContent.setText(Html.fromHtml(tripNotification.getRenderedMessage(context, !isSeen)));
            tvTime.setText(DateFormatter.format(tripNotification.getTime()));
            ImageHelper.loadCircleImage(tripNotification.getAvatar(), imgAvatar);
            int typeIndex = Notification.TripType.tripTypes.indexOf(tripNotification.getType());
            imgType.setImageResource(Notification.TripIcon.tripIcons.get(typeIndex));
            badgeSeen.setVisibility(isSeen ? View.INVISIBLE : View.VISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSeen(tripNotification);
                    switch (tripNotification.getType()) {
                        case UserNotification.TripType.USER_SOS_ADDED:
                        case UserNotification.TripType.USER_SOS_RESOLVED:
                        case UserNotification.TripType.USER_ADDED:
                        case UserNotification.TripType.USER_REMOVED:
                            if (!tripNotification.getType().equals(UserNotification.TripType.USER_REMOVED)) {
                                navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, User.class.getSimpleName(), tripNotification.getUserRef().getId());
                            }
                            break;
                        case UserNotification.TripType.CHECKPOINT_GATHER_REQUEST:
                        case UserNotification.TripType.CHECKPOINT_ADDED:
                        case UserNotification.TripType.CHECKPOINT_REMOVED:
                            if (!tripNotification.getType().equals(UserNotification.TripType.CHECKPOINT_REMOVED)) {
                                navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, Checkpoint.class.getSimpleName(), tripNotification.getCheckpointRef().getId());
                            }
                            break;
                        case UserNotification.TripType.TRIP_JOIN_REQUEST:
                            // TODO: handle here
                            break;
                    }
                }
            });
        }
    }
}
