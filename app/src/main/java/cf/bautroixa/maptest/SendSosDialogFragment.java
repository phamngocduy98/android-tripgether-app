package cf.bautroixa.maptest;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;

public class SendSosDialogFragment extends BottomSheetDialogFragment {

    private static final String TAG = "SendSosDialogFragment";
    private View view;
    private Button btnOK, btnCancel;
    private EditText editDesc;
    private Spinner spinnerLever;

    private int selectedLever;

    private FirebaseAuth mAuth;
    private MainAppManager manager;

    public SendSosDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = MainAppManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        SosRequest sosRequest = manager.getSosRequestsManager().get(mAuth.getUid());
        if (sosRequest != null) {
            editDesc.setText(sosRequest.getDescription());
            spinnerLever.setSelection(sosRequest.getLever());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dialog_send_sos, container, false);

        editDesc = view.findViewById(R.id.edit_desc_frag_send_sos);
        btnOK = view.findViewById(R.id.btn_send_frag_send_sos);
        btnCancel = view.findViewById(R.id.btn_cancel_frag_send_sos);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.getSosRequestsManager().addSosRequest(new SosRequest(selectedLever, editDesc.getText().toString(), false)).addOnCompleteListener(new OnCompleteListener<Void>() {
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

        spinnerLever = view.findViewById(R.id.spinner_alert_lever_frag_send_sos);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.alert_levers_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLever.setAdapter(adapter);

        spinnerLever.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "selected value = "+getResources().getStringArray(R.array.alert_levers_array)[position]);
                selectedLever = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
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
