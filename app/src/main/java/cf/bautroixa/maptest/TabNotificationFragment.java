package cf.bautroixa.maptest;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Objects;

import cf.bautroixa.maptest.data.NotificationItem;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.Navigable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.theme.OneAppbarFragment;
import cf.bautroixa.maptest.theme.OneRecyclerView;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ImageHelper;

public class TabNotificationFragment extends OneAppbarFragment implements Navigable {
    private MainAppManager manager;
    ArrayList<Event> events;

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;

    private DatasManager.OnDatasChangedListener<Event> onEventsChangedListener;
    private NavigationInterfaces navigationInterfaces;

    public TabNotificationFragment() {
        manager = MainAppManager.getInstance();
        events = manager.getEventsManager().getData();
        adapter = new NotificationAdapter();
        onEventsChangedListener = new DatasManager.OnDatasChangedListener<Event>() {
            @Override
            public void onItemInserted(int position, Event data) {
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onItemChanged(int position, Event data) {

            }

            @Override
            public void onItemRemoved(int position, Event data) {

            }

            @Override
            public void onDataSetChanged(ArrayList<Event> datas) {
                events = datas;
                adapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_notification, container, false);
        rvNotifications = v.findViewById(R.id.rv_notifications);
        rvNotifications.setAdapter(adapter);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("Thông báo");
        setSubtitle(String.format("%d thông báo chưa đọc", manager.getEventsManager().getData().size()));
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getEventsManager().addOnDatasChangedListener(onEventsChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getEventsManager().removeOnDatasChangedListener(onEventsChangedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationInterfaces = null;
    }

    @Override
    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
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
            event.getNotificationItem(manager).addOnCompleteListener(new OnCompleteListener<NotificationItem>() {
                @Override
                public void onComplete(@NonNull Task<NotificationItem> task) {
                    if (task.isSuccessful()) {
                        NotificationItem notificationItem = task.getResult();
                        tvContent.setText(Html.fromHtml(notificationItem.getContent()));
                        tvTime.setText(notificationItem.getTime());
                        if (notificationItem.getEventType() == Event.Type.USER_SOS_ADDED || notificationItem.getEventType() == Event.Type.USER_SOS_RESOLVED) {
                            imgType.setImageResource(R.drawable.ic_sos_red_24dp);
                        } else {
                            imgType.setImageResource(R.drawable.ic_assistant_photo_black_24dp);
                        }
                        ImageHelper.loadCircleImage(notificationItem.getAvatar(), imgAvatar);
                    }
                }
            });
        }
    }

    public class NotificationAdapter extends OneRecyclerView.Adapter<NotificationVH> {

        public NotificationAdapter() {
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
                    switch (event.getType()) {
                        case Event.Type.CHECKPOINT_ADDED:
                        case Event.Type.CHECKPOINT_ROLL_UP_ADDED:
                            Checkpoint checkpoint = manager.getCheckpointsManager().get(Objects.requireNonNull(event.getCheckpointRef()).getId());
                            navigationInterfaces.navigate(MainActivity.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, checkpoint);
                            break;
                        case Event.Type.USER_ADDED:
                        case Event.Type.USER_SOS_ADDED:
                            User user = manager.getMembersManager().get(Objects.requireNonNull(event.getUserRef()).getId());
                            navigationInterfaces.navigate(MainActivity.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, user);
                            break;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }
    }
}



