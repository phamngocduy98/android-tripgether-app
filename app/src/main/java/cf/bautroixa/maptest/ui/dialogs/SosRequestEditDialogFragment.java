package cf.bautroixa.maptest.ui.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.SosRequest;
import cf.bautroixa.maptest.ui.theme.FullScreenDialogFragment;
import cf.bautroixa.maptest.ui.theme.ViewAnim;
import cf.bautroixa.maptest.utils.ImageHelper;

public class SosRequestEditDialogFragment extends FullScreenDialogFragment {
    private static final String TAG = "SosRequestEditDialogFragment";
    private ImageView imgAvatar;
    private Button btnOK, btnCancel;
    private EditText editDesc;
    private RadioGroup rgLever;
    private RadioButton radioHigh, radioMedium, radioLow;

    private int selectedLever;
    private SosRequest sosRequest;

    private FirebaseAuth mAuth;
    private ModelManager manager;
    private ProgressDialog loadingDialog;

    public SosRequestEditDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = ModelManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        ImageHelper.loadUserAvatar(imgAvatar, new TextView(requireContext()), manager.getCurrentUser());
        sosRequest = manager.getCurrentUser().getSosRequest();
        if (sosRequest != null) {
            if (!sosRequest.isResolved()) {
                editDesc.setText(sosRequest.getDescription());
                selectButtonFromLever(sosRequest.getLever());
                btnOK.setText("Cập nhật");
                btnCancel.setText("Xóa (đã giải quyết)");
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewAnim.toggleLoading(getContext(), btnCancel, true, "");
                        Task<Void> updateTask = manager.getCurrentUser().sendUpdate(null, SosRequest.RESOLVED, true);
                        if (updateTask != null)
                            updateTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    ViewAnim.toggleLoading(getContext(), btnCancel, false, "Xóa");
                                    loadingDialog = ProgressDialog.show(requireContext(), "", "Đang xóa", true, false);
                                    loadingDialog.setCustomTitle(new View(requireContext()));
                                    if (task.isSuccessful()) {
                                        loadingDialog.dismiss();
                                        dismiss();
                                    }
                                }
                            });
                    }
                });
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_sos_request_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgAvatar = view.findViewById(R.id.img_sos_frag_send_sos);
        editDesc = view.findViewById(R.id.edit_desc_frag_send_sos);
        btnOK = view.findViewById(R.id.btn_send_frag_send_sos);
        btnCancel = view.findViewById(R.id.btn_cancel_frag_send_sos);
        rgLever = view.findViewById(R.id.rg_lever);
        radioHigh = view.findViewById(R.id.radio_high);
        radioMedium = view.findViewById(R.id.radio_medium);
        radioLow = view.findViewById(R.id.radio_low);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLever = getLeverFromButtonId(rgLever.getCheckedRadioButtonId());
                btnCancel.setVisibility(View.GONE);
                btnOK.setEnabled(false);
                btnOK.setText("Sending...");
                loadingDialog = ProgressDialog.show(requireContext(), "", "Đang cập nhật", true, false);
                loadingDialog.setCustomTitle(new View(requireContext()));
                manager.sendSosRequest(requireContext(), new SosRequest(selectedLever, editDesc.getText().toString(), false)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadingDialog.dismiss();
                        dismiss();
                    }
                });
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    int getLeverFromButtonId(int btnId) {
        switch (btnId) {
            case R.id.radio_high:
                return SosRequest.SosLever.HIGH;
            case R.id.radio_low:
                return SosRequest.SosLever.LOW;
            default:
                return SosRequest.SosLever.MEDIUM;
        }
    }

    void selectButtonFromLever(int lever) {
        switch (lever) {
            case SosRequest.SosLever.HIGH:
                radioHigh.toggle();
                break;
            case SosRequest.SosLever.LOW:
                radioLow.toggle();
                break;
            default:
                radioMedium.toggle();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss();
    }
}
