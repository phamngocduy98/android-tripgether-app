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
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ImageHelper;

public class InfoTabFragment extends Fragment {
    RecyclerView rvSos;
    NavNotiFragment.OnNotificationItemClickedListener mListener;
    DatasManager.OnItemInsertedListener<SosRequest> onItemInsertedListener;
    DatasManager.OnItemChangedListener<SosRequest> onItemChangedListener;
    MainAppManager manager;
    SosAdapter adapter;

    public InfoTabFragment() {
    }

    public void setListener(NavNotiFragment.OnNotificationItemClickedListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = MainAppManager.getInstance();
        adapter = new SosAdapter();
        onItemInsertedListener = new DatasManager.OnItemInsertedListener<SosRequest>() {
            @Override
            public void onItemInserted(int position, SosRequest data) {
                adapter.notifyItemChanged(position);
            }
        };
        onItemChangedListener = new DatasManager.OnItemChangedListener<SosRequest>() {
            @Override
            public void onItemChanged(int position, SosRequest sosRequest) {
                adapter.notifyItemChanged(position);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getSosRequestsManager().addOnItemInsertedListener(onItemInsertedListener);
        manager.getSosRequestsManager().addOnItemChangedListener(onItemChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getSosRequestsManager().removeOnItemInsertedListener(onItemInsertedListener);
        manager.getSosRequestsManager().removeOnItemChangedListener(onItemChangedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        rvSos = v.findViewById(R.id.rv_sos);
        rvSos.setAdapter(adapter);
        rvSos.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public class SosVH extends RecyclerView.ViewHolder {
        View view;
        RoundedImageView imgAvatar, imgType;
        TextView tvContent, tvTime;

        public SosVH(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_noti);
            imgType = itemView.findViewById(R.id.img_type_item_noti);
            tvContent = itemView.findViewById(R.id.tv_content_item_noti);
            tvTime = itemView.findViewById(R.id.tv_time_item_noti);
        }

        public void bind(SosRequest sosRequest) {
            NotificationItem notificationItem = sosRequest.getNotificationItem(manager);
            tvContent.setText(Html.fromHtml(notificationItem.getContent()));
            tvTime.setText(notificationItem.getTime());
            if (!sosRequest.isResolved()) {
                imgType.setImageResource(R.drawable.ic_sos_red_24dp);
            } else {
                imgType.setImageResource(R.drawable.ic_verified_user_black_24dp);
            }
            ImageHelper.loadImage(notificationItem.getAvatar(), imgAvatar);
        }
    }

    public class SosAdapter extends RecyclerView.Adapter<SosVH> {
        ArrayList<SosRequest> sosRequests;

        public SosAdapter() {
            sosRequests = manager.getSosRequestsManager().getData();
        }

        @NonNull
        @Override
        public SosVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_notification_frag_notification, parent, false);
            return new SosVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SosVH holder, int position) {
            final SosRequest sosRequest = sosRequests.get(position);
            holder.bind(sosRequest);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onNotificationClick(Event.Type.USER_SOS_ADDED, sosRequest.getId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return sosRequests.size();
        }
    }
}
