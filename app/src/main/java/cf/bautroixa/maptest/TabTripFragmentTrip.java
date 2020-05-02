package cf.bautroixa.maptest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.dialogs.SosRequestEditDialogFragment;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.HasOnGoToMainActivityState;
import cf.bautroixa.maptest.interfaces.OnDrawRouteRequest;
import cf.bautroixa.maptest.interfaces.OnGoToMainActivityState;
import cf.bautroixa.maptest.theme.OneRecyclerView;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ImageHelper;

public class TabTripFragmentTrip extends Fragment implements HasOnGoToMainActivityState<SosRequest> {

    MainAppManager manager;
    OnDrawRouteRequest onDrawRouteRequest;
    DatasManager.OnItemInsertedListener<SosRequest> onItemInsertedListener;
    DatasManager.OnItemChangedListener<SosRequest> onItemChangedListener;
    DatasManager.OnItemRemovedListener<SosRequest> onItemRemovedListener;
    RecyclerView rvSos;
    Button btnAddEditSos;
    private OnGoToMainActivityState<SosRequest> onGoToMainActivityState = null;
    SosAdapter adapter;

    public TabTripFragmentTrip() {
    }

    public void setOnDrawRouteRequest(OnDrawRouteRequest onDrawRouteRequest) {
        this.onDrawRouteRequest = onDrawRouteRequest;
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
        onItemRemovedListener = new DatasManager.OnItemRemovedListener<SosRequest>() {
            @Override
            public void onItemRemoved(int position, SosRequest data) {
                adapter.notifyItemRemoved(position);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getSosRequestsManager().addOnItemInsertedListener(onItemInsertedListener);
        manager.getSosRequestsManager().addOnItemChangedListener(onItemChangedListener);
        manager.getSosRequestsManager().addOnItemRemovedListener(onItemRemovedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getSosRequestsManager().removeOnItemInsertedListener(onItemInsertedListener);
        manager.getSosRequestsManager().removeOnItemChangedListener(onItemChangedListener);
        manager.getSosRequestsManager().removeOnItemRemovedListener(onItemRemovedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_trip_subtab_trip, container, false);
        btnAddEditSos = v.findViewById(R.id.btn_add_edit_sos_frag_tab_trip_subtab_checkpoints);

        if (manager.getSosRequestsManager().get(manager.getCurrentUser().getId()) != null) {
            btnAddEditSos.setText("Cập nhật yêu cầu");
            btnAddEditSos.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            btnAddEditSos.setText("Tạo yêu cầu hỗ trợ");
            btnAddEditSos.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_white_24dp, 0, 0, 0);
        }
        btnAddEditSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SosRequestEditDialogFragment().show(getChildFragmentManager(), "add edit sos");
            }
        });


        rvSos = v.findViewById(R.id.rv_sos);
        rvSos.setAdapter(adapter);
        rvSos.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onDrawRouteRequest = null;
    }

    @Override
    public void setOnGoToMainActivityState(OnGoToMainActivityState<SosRequest> onGoToMainActivityState) {
        this.onGoToMainActivityState = onGoToMainActivityState;
    }

    public class SosVH extends OneRecyclerView.ViewHolder {
        View view;
        RoundedImageView imgAvatar;
        TextView tvName, tvDes, tvLever;

        public SosVH(@NonNull View itemView, int viewType) {
            super(itemView, viewType);
            view = itemView;
            imgAvatar = itemView.findViewById(R.id.img_user_avatar_item_sos);
            tvName = itemView.findViewById(R.id.tv_user_name_item_sos);
            tvDes = itemView.findViewById(R.id.tv_description_item_sos);
            tvLever = itemView.findViewById(R.id.tv_lever_item_sos);
        }

        public void bind(SosRequest sosRequest) {
            // sosId == userId
            User user = manager.getMembersManager().get(sosRequest.getId());
            if (sosRequest.isResolved()) {
                tvLever.setText("Đã giải quyết");
            } else {
                tvLever.setText("Mức độ " + sosRequest.getLever());
            }
            if (user != null) {
                ImageHelper.loadImage(user.getAvatar(), imgAvatar);
                tvName.setText(user.getName());
            }
            tvDes.setText(sosRequest.getDescription());
        }
    }

    public class SosAdapter extends OneRecyclerView.Adapter<SosVH> {
        ArrayList<SosRequest> sosRequests;

        public SosAdapter() {
            sosRequests = manager.getSosRequestsManager().getData();
        }

        @NonNull
        @Override
        public SosVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_sos_request, parent, false);
            return new SosVH(v, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SosVH holder, int position) {
            final SosRequest sosRequest = sosRequests.get(position);
            holder.bind(sosRequest);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGoToMainActivityState.newState(MainActivity.STATE_MEMBER_STATUS, sosRequest);
                }
            });
        }

        @Override
        public int getItemCount() {
            return sosRequests.size();
        }
    }
}
