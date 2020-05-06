package cf.bautroixa.maptest.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.MapBackgroundInterfaces;
import cf.bautroixa.maptest.theme.OneBottomSheetDialog;

public class SosRequestViewDialogFragment extends OneBottomSheetDialog {
    private static final String TAG = "SosRequestEditDialogFragment";
    private static final String ARG_ID = "id";
    SosRequest sosRequest;
    String sosRequestId;
    Data.OnNewValueListener<SosRequest> onSosRequestNewValueListener;
    MapBackgroundInterfaces mapBackgroundInterfaces;
    private MainAppManager manager;
    private TextView tvUserName, tvUserLocation, tvLever, tvDesc;
    private String[] leverStrings;

    public SosRequestViewDialogFragment(MapBackgroundInterfaces mapBackgroundInterfaces) {
        manager = MainAppManager.getInstance();
        this.mapBackgroundInterfaces = mapBackgroundInterfaces;
        onSosRequestNewValueListener = new Data.OnNewValueListener<SosRequest>() {
            @Override
            public void onNewData(@Nullable SosRequest sosRequest) {
                if (sosRequest != null) {
                    updateView(sosRequest);
                }
            }
        };
    }

    public static SosRequestViewDialogFragment newInstance(MapBackgroundInterfaces mapBackgroundInterfaces, String sosRequestId) {
        Bundle args = new Bundle();
        args.putString(ARG_ID, sosRequestId);
        SosRequestViewDialogFragment fragment = new SosRequestViewDialogFragment(mapBackgroundInterfaces);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sosRequestId = getArguments().getString(ARG_ID);
            sosRequest = manager.getSosRequestsManager().get(sosRequestId);
        }
        leverStrings = getResources().getStringArray(R.array.sos_lever);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sosRequest != null) sosRequest.addOnNewValueListener(onSosRequestNewValueListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sosRequest != null) sosRequest.removeOnNewValueListener(onSosRequestNewValueListener);
    }

    void updateView(SosRequest sosRequest) {
        // sosId == userId
        final User user = manager.getMembersManager().get(sosRequest.getId());
        if (user != null) {
            tvUserName.setText(user.getName());
            tvUserLocation.setText("Vị trí hiện tại: Gần " + user.getCurrentLocation());
        }
        tvLever.setText("Mức độ nghiêm trọng: " + leverStrings[sosRequest.getLever()]);
        tvDesc.setText("Thông điệp: " + sosRequest.getDescription());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_sos_request_view, container, false);

        tvUserName = view.findViewById(R.id.tv_user_name_dialog_sos_request_view);
        tvUserLocation = view.findViewById(R.id.tv_user_location_dialog_sos_request_view);
        tvLever = view.findViewById(R.id.tv_lever_dialog_sos_request_view);
        tvDesc = view.findViewById(R.id.tv_desc_dialog_sos_request_view);
        Button btnClose = view.findViewById(R.id.btn_close_dialog_sos_request_view);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        updateView(sosRequest);
        return view;
    }
}
