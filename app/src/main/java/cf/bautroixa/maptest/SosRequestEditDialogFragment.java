package cf.bautroixa.maptest;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.theme.OneBottomSheetDialog;

public class SosRequestEditDialogFragment extends OneBottomSheetDialog {

    private static final String TAG = "SosRequestEditDialogFragment";
    private View view;
    private Button btnOK, btnCancel;
    private EditText editDesc;
    private RadioGroup rgLever;
    private RadioButton radioHigh, radioMedium, radioLow;

    private int selectedLever;

    private FirebaseAuth mAuth;
    private MainAppManager manager;

    public SosRequestEditDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = MainAppManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        SosRequest sosRequest = manager.getSosRequestsManager().get(manager.getCurrentUser().getId());
        if (sosRequest != null) {
            editDesc.setText(sosRequest.getDescription());
            selectButtonFromLever(sosRequest.getLever());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dialog_sos_request_edit, container, false);

        editDesc = view.findViewById(R.id.edit_desc_frag_send_sos);
        btnOK = view.findViewById(R.id.btn_send_frag_send_sos);
        btnCancel = view.findViewById(R.id.btn_cancel_frag_send_sos);

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
                manager.sendSosRequest(new SosRequest(selectedLever, editDesc.getText().toString(), false), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
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

        rgLever = view.findViewById(R.id.rg_lever);


        return view;
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
