package cf.bautroixa.tripgether.ui.trip;

import android.content.Context;
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
import androidx.recyclerview.widget.SortedList;

import java.util.ArrayList;
import java.util.Objects;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.objects.SosRequest;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.dialogs.SosRequestEditDialogFragment;
import cf.bautroixa.tripgether.ui.map.TabMapFragment;
import cf.bautroixa.tripgether.ui.sortedlist.UserSortedListAdapterCallback;
import cf.bautroixa.tripgether.ui.theme.OneRecyclerView;
import cf.bautroixa.tripgether.ui.theme.RoundedImageView;
import cf.bautroixa.tripgether.ui.trip_invite.TripInvitationActivity;
import cf.bautroixa.tripgether.utils.IntentHelper;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class TabTripFragmentTrip extends Fragment implements NavigationInterfaceOwner {
    SortedList<User> sosRequestMembers;

    ModelManager manager;
    RecyclerView rvSos;
    private String[] leverStrings = new String[3];
    private NavigationInterface navigationInterface;
    private DocumentsManager.OnListChangedListener<User> onSosChangedListener;
    /**
     * VIEWS
     */
    // SOS list
    private TextView tvHeaderSos;
    private SosAdapter adapter;
    private Button btnAddEditSos;
    // Widget Share Trip
    private TextView tvTripCode;
    private Button btnSendTripCode, btnQRTripCode;

    public TabTripFragmentTrip() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sosRequestMembers = new SortedList<>(User.class, new UserSortedListAdapterCallback(adapter));
        leverStrings = getResources().getStringArray(R.array.sos_lever);

        adapter = new SosAdapter();
        onSosChangedListener = new DocumentsManager.OnListChangedListener<User>() {
            @Override
            public void onItemInserted(int position, User data) {
                adapter.notifyItemChanged(position);
                if (sosRequestMembers.size() > 0 && tvHeaderSos.getVisibility() == View.GONE)
                    tvHeaderSos.setVisibility(View.VISIBLE);
            }

            @Override
            public void onItemChanged(int position, User data) {
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onItemRemoved(int position, User data) {
                adapter.notifyItemRemoved(position);
                if (sosRequestMembers.size() == 0 && tvHeaderSos.getVisibility() == View.VISIBLE)
                    tvHeaderSos.setVisibility(View.GONE);
            }

            @Override
            public void onDataSetChanged(ArrayList<User> datas) {
                adapter.notifyDataSetChanged();
                if (sosRequestMembers.size() > 0 && tvHeaderSos.getVisibility() == View.GONE)
                    tvHeaderSos.setVisibility(View.VISIBLE);
                if (sosRequestMembers.size() == 0 && tvHeaderSos.getVisibility() == View.VISIBLE)
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
        if (sosRequestMembers.size() == 0) tvHeaderSos.setVisibility(View.GONE);
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
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof NavigationInterfaceOwner) {
            ((NavigationInterfaceOwner) childFragment).setNavigationInterface(navigationInterface);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: improve performance
        if (manager.getCurrentTripRef() != null) {
            tvTripCode.setText(manager.getCurrentTripRef().getId());
        }
        if (manager.getCurrentUser().getSosRequest() != null) {
            btnAddEditSos.setText("Cập nhật yêu cầu hỗ trợ");
            btnAddEditSos.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            btnAddEditSos.setText("Tạo yêu cầu hỗ trợ");
            btnAddEditSos.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_white_24dp, 0, 0, 0);
        }
        if (manager.getCurrentTrip().isAvailable()) {
            manager.getCurrentTrip().getMembersManager().addOnListChangedListener(onSosChangedListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (manager.getCurrentTrip().isAvailable()) {
            manager.getCurrentTrip().getMembersManager().removeOnListChangedListener(onSosChangedListener);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationInterface = null;
        onSosChangedListener = null;
    }

    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
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

        public void bind(User sosRequestUser) {
            SosRequest sosRequest = sosRequestUser.getSosRequest();
            if (sosRequest.isResolved()) {
                tvLever.setText("Đã giải quyết");
            } else {
                tvLever.setText(String.format("%s", leverStrings[sosRequest.getLever()]));
            }
            ImageHelper.loadCircleImage(sosRequestUser.getAvatar(), imgAvatar);
            tvName.setText(sosRequestUser.getName());
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
            final User sosRequestUser = sosRequestMembers.get(position);
            holder.bind(sosRequestUser);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, sosRequestUser);
                }
            });
        }

        @Override
        public int getItemCount() {
            return sosRequestMembers.size();
        }
    }
}
