package cf.bautroixa.maptest.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.SosRequest;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.ui.theme.OneBottomSheetDialog;

public class SosRequestViewDialogFragment extends OneBottomSheetDialog {
    private static final String TAG = "SosRequestEditDialogFragment";
    private static final String ARG_ID = "id";
    User sosRequestUser;
    String sosRequestUserId;
    private ModelManager manager;
    private TextView tvUserName, tvUserLocation, tvLever, tvDesc;
    private String[] leverStrings;

    public SosRequestViewDialogFragment() {
        manager = ModelManager.getInstance();
    }

    public static SosRequestViewDialogFragment newInstance(String userId) {
        Bundle args = new Bundle();
        args.putString(ARG_ID, userId);
        SosRequestViewDialogFragment fragment = new SosRequestViewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sosRequestUserId = getArguments().getString(ARG_ID);
            manager.getBaseUsersManager().requestGet(sosRequestUserId).addOnCompleteListener(new OnCompleteListener<User>() {
                @Override
                public void onComplete(@NonNull Task<User> task) {
                    if (task.isSuccessful()) {
                        sosRequestUser = task.getResult();
                        updateView(sosRequestUser);
                    }
                }
            });
        }
        leverStrings = getResources().getStringArray(R.array.sos_lever);

    }

    void updateView(User user) {
        SosRequest sosRequest = user.getSosRequest();
        tvUserName.setText(user.getName());
        tvUserLocation.setText("Vị trí hiện tại: Gần " + user.getCurrentLocation());
        tvLever.setText("Cần hỗ trợ " + leverStrings[sosRequest.getLever()]);
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
        return view;
    }
}
