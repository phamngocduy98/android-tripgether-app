package cf.bautroixa.maptest;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.data.NotificationItem;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.theme.OneRecyclerView;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ImageHelper;

public class TabNotificationFragmentNotifications extends Fragment {
    RecyclerView rvNotifications;
    TabNotificationFragment.OnNotificationItemClickedListener mListener;
    DatasManager.OnItemInsertedListener<Event> onItemInsertedListener;
    MainAppManager manager;
    NotificationAdapter adapter;

    public TabNotificationFragmentNotifications() {
    }

    public void setListener(TabNotificationFragment.OnNotificationItemClickedListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = MainAppManager.getInstance();
        adapter = new NotificationAdapter();
        onItemInsertedListener = new DatasManager.OnItemInsertedListener<Event>() {
            @Override
            public void onItemInserted(int position, Event data) {
                adapter.notifyItemChanged(position);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getEventsManager().addOnItemInsertedListener(onItemInsertedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getEventsManager().removeOnItemInsertedListener(onItemInsertedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_notification_subtab_notifications, container, false);
        rvNotifications = v.findViewById(R.id.rv_notifications);
        rvNotifications.setAdapter(adapter);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public class NotificationVH extends OneRecyclerView.ViewHolder {
        View view;
        RoundedImageView imgAvatar, imgType;
        TextView tvContent, tvTime;

        public NotificationVH(@NonNull View itemView, int viewType) {
            super(itemView, viewType);
            view = itemView;
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_noti);
            imgType = itemView.findViewById(R.id.img_type_item_noti);
            tvContent = itemView.findViewById(R.id.tv_content_item_noti);
            tvTime = itemView.findViewById(R.id.tv_time_item_noti);
        }

        public void bind(Event event) {
            NotificationItem notificationItem = event.getNotificationItem(manager);
            tvContent.setText(Html.fromHtml(notificationItem.getContent()));
            tvTime.setText(notificationItem.getTime());
            if (notificationItem.getEventType() == Event.Type.USER_SOS_ADDED || notificationItem.getEventType() == Event.Type.USER_SOS_RESOLVED) {
                imgType.setImageResource(R.drawable.ic_sos_red_24dp);
            } else {
                imgType.setImageResource(R.drawable.ic_assistant_photo_black_24dp);
            }
            ImageHelper.loadImage(notificationItem.getAvatar(), imgAvatar);
        }
    }

    public class NotificationAdapter extends OneRecyclerView.Adapter<NotificationVH> {
        ArrayList<Event> events;

        public NotificationAdapter() {
            events = manager.getEventsManager().getData();
        }

        @NonNull
        @Override
        public NotificationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.fragment_tab_notification_item, parent, false);
            return new NotificationVH(v, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationVH holder, int position) {
            final Event event = events.get(position);
            holder.bind(event);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onNotificationClick(event.getType(), event.getId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }
    }
}
