package cf.bautroixa.maptest;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import cf.bautroixa.maptest.dialogs.SosRequestEditDialogFragment;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.NavigableToMainTab;
import cf.bautroixa.maptest.interfaces.OnNavigationToMainTab;
import cf.bautroixa.maptest.theme.OneRecyclerView;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ImageHelper;
import cf.bautroixa.maptest.utils.IntentHelper;

public class TabTripFragmentTrip extends Fragment implements NavigableToMainTab {
    ArrayList<SosRequest> sosRequests;

    MainAppManager manager;
    private String[] leverStrings = new String[3];
    private OnNavigationToMainTab onNavigationToMainTab;
    private DatasManager.OnItemInsertedListener<SosRequest> onItemInsertedListener;
    private DatasManager.OnItemChangedListener<SosRequest> onItemChangedListener;
    private DatasManager.OnItemRemovedListener<SosRequest> onItemRemovedListener;
    private DatasManager.OnDataSetChangedListener<SosRequest> onDataSetChangedListener;

    /**
     * VIEWS
     */
    // SOS list
    private TextView tvHeaderSos;
    RecyclerView rvSos;
    private SosAdapter adapter;
    private Button btnAddEditSos;
    // Widget Share Trip
    private TextView tvTripCode;
    private Button btnSendTripCode, btnQRTripCode;

    public TabTripFragmentTrip() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = MainAppManager.getInstance();
        sosRequests = manager.getSosRequestsManager().getData();

        leverStrings = getResources().getStringArray(R.array.alert_levers_array);

        adapter = new SosAdapter();
        onItemInsertedListener = new DatasManager.OnItemInsertedListener<SosRequest>() {
            @Override
            public void onItemInserted(int position, SosRequest data) {
                adapter.notifyItemChanged(position);
                if (sosRequests.size() > 0 && tvHeaderSos.getVisibility() == View.GONE)
                    tvHeaderSos.setVisibility(View.VISIBLE);
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
                if (sosRequests.size() == 0 && tvHeaderSos.getVisibility() == View.VISIBLE)
                    tvHeaderSos.setVisibility(View.GONE);
            }
        };
        onDataSetChangedListener = new DatasManager.OnDataSetChangedListener<SosRequest>() {
            @Override
            public void onDataSetChanged(ArrayList<SosRequest> datas) {
                adapter.notifyDataSetChanged();
                if (sosRequests.size() > 0 && tvHeaderSos.getVisibility() == View.GONE)
                    tvHeaderSos.setVisibility(View.VISIBLE);
                if (sosRequests.size() == 0 && tvHeaderSos.getVisibility() == View.VISIBLE)
                    tvHeaderSos.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_trip_subtab_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHeaderSos = view.findViewById(R.id.tv_header_sos_request_list);
        rvSos = view.findViewById(R.id.rv_sos);
        rvSos.setAdapter(adapter);
        rvSos.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        // Widget Share Trip
        tvTripCode = view.findViewById(R.id.tv_trip_code_widget_share_trip);
        btnSendTripCode = view.findViewById(R.id.btn_send_widget_share_trip);
        btnQRTripCode = view.findViewById(R.id.btn_qr_widget_share_trip);
        btnSendTripCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentHelper.sendTripCodeIntent(requireContext(), Objects.requireNonNull(manager.getCurrentTripRef()).getId());
            }
        });
        btnQRTripCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), TripInvitationActivity.class));
            }
        });
        // Add/Edit Sos request
        btnAddEditSos = view.findViewById(R.id.btn_add_edit_sos_frag_tab_trip_subtab_checkpoints);
        btnAddEditSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SosRequestEditDialogFragment().show(getChildFragmentManager(), "add edit sos");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: improve performance
        tvTripCode.setText(Objects.requireNonNull(manager.getCurrentTripRef()).getId());
        if (manager.getMySosRequest() != null) {
            btnAddEditSos.setText("Cập nhật yêu cầu hỗ trợ");
            btnAddEditSos.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            btnAddEditSos.setText("Tạo yêu cầu hỗ trợ");
            btnAddEditSos.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_white_24dp, 0, 0, 0);
        }
        manager.getSosRequestsManager().addOnItemInsertedListener(onItemInsertedListener)
                .addOnItemChangedListener(onItemChangedListener)
                .addOnItemRemovedListener(onItemRemovedListener)
                .addOnDataSetChangedListener(onDataSetChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getSosRequestsManager().removeOnItemInsertedListener(onItemInsertedListener)
                .removeOnItemChangedListener(onItemChangedListener)
                .removeOnItemRemovedListener(onItemRemovedListener)
                .removeOnDataSetChangedListener(onDataSetChangedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onNavigationToMainTab = null;
        onItemInsertedListener = null;
        onItemChangedListener = null;
        onItemRemovedListener = null;
        onDataSetChangedListener = null;
    }

    public void setOnNavigationToMainTab(OnNavigationToMainTab onNavigationToMainTab) {
        this.onNavigationToMainTab = onNavigationToMainTab;
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
                tvLever.setText(String.format("Mức độ: %s", leverStrings[sosRequest.getLever()]));
            }
            if (user != null) {
                ImageHelper.loadImage(user.getAvatar(), imgAvatar);
                tvName.setText(user.getName());
            }
            tvDes.setText(sosRequest.getDescription());
        }
    }

    public class SosAdapter extends OneRecyclerView.Adapter<SosVH> {
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
                    onNavigationToMainTab.navigate(MainActivity.TAB_MAP, TabMainFragment.STATE_MEMBER_STATUS, sosRequest);
                }
            });
        }

        @Override
        public int getItemCount() {
            return sosRequests.size();
        }
    }
}
