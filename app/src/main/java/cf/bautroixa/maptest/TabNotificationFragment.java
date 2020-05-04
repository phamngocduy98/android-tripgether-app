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

import java.util.ArrayList;

import cf.bautroixa.maptest.data.NotificationItem;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.interfaces.DataItemsSelectable;
import cf.bautroixa.maptest.interfaces.NavigableToState;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.interfaces.OnNavigationToState;
import cf.bautroixa.maptest.theme.OneAppbarFragment;
import cf.bautroixa.maptest.theme.OneRecyclerView;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ImageHelper;

public class TabNotificationFragment extends OneAppbarFragment implements NavigableToState, DataItemsSelectable<Event> {
    private MainAppManager manager;

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;

    private DatasManager.OnItemInsertedListener<Event> onItemInsertedListener;
    private DatasManager.OnDataSetChangedListener<Event> onDataSetChangedListener;
    private OnNavigationToState onNavigationToState;
    private OnDataItemSelected<Event> onEventItemSelected;

    public TabNotificationFragment() {
        manager = MainAppManager.getInstance();
        adapter = new NotificationAdapter();
        onItemInsertedListener = new DatasManager.OnItemInsertedListener<Event>() {
            @Override
            public void onItemInserted(int position, Event data) {
                adapter.notifyItemChanged(position);
            }
        };
        onDataSetChangedListener = new DatasManager.OnDataSetChangedListener<Event>() {
            @Override
            public void onDataSetChanged(ArrayList<Event> datas) {
                adapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public void setOnNavigationToState(OnNavigationToState onNavigationToState) {
        this.onNavigationToState = onNavigationToState;
    }

    @Override
    public void setOnDataItemSelected(OnDataItemSelected<Event> onEventItemSelected) {
        this.onEventItemSelected = onEventItemSelected;
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
        setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNavigationToState != null)
                    onNavigationToState.newState(MainActivity.STATE_TAB_TRIP);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getEventsManager().addOnItemInsertedListener(onItemInsertedListener).addOnDataSetChangedListener(onDataSetChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getEventsManager().removeOnItemInsertedListener(onItemInsertedListener).removeOnDataSetChangedListener(onDataSetChangedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onEventItemSelected = null;
        onNavigationToState = null;
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
                    if (onEventItemSelected != null)
                        onEventItemSelected.selectItem(event);
                }
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }
    }
}



