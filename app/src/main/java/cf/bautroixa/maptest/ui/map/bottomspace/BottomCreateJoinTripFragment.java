package cf.bautroixa.maptest.ui.map.bottomspace;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.http.HttpRequest;
import cf.bautroixa.maptest.ui.dialogs.QrScannerDialogFragment;
import cf.bautroixa.maptest.ui.theme.OneDialog;
import cf.bautroixa.maptest.ui.theme.OnePromptDialog;
import cf.bautroixa.maptest.ui.trip.CreateTripActivity;

public class BottomCreateJoinTripFragment extends Fragment {
    LinearLayout linearCreateTrip, linearJoinTrip;
    ModelManager manager;

    public BottomCreateJoinTripFragment() {
        manager = ModelManager.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_create_join_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearCreateTrip = view.findViewById(R.id.linear_create_trip);
        linearJoinTrip = view.findViewById(R.id.linear_join_trip);

        linearCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateTripActivity.class);
                startActivity(intent);
            }
        });

        linearJoinTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OneDialog.Builder().enableNegativeButton(true).title(R.string.dialog_title_join_trip)
                        .message(R.string.dialog_message_join_trip)
                        .negBtnText(R.string.btn_enter_trip_code)
                        .posBtnText(R.string.btn_qr_scan)
                        .buttonClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface joinTripDialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    joinTripDialog.dismiss();
                                    new QrScannerDialogFragment(new QrScannerDialogFragment.OnQrResultListener() {
                                        @Override
                                        public void onResult(String result) {
                                            if (getContext() != null) {
                                                // TODO: join trip here
                                            }
                                        }
                                    }).show(getChildFragmentManager(), "qr scanner");
                                } else {
                                    new OnePromptDialog.Builder().title(R.string.dialog_title_enter_trip_code).editHintText(R.string.dialog_title_enter_trip_code).onResult(new OnePromptDialog.OnDialogResult() {
                                        @Override
                                        public void onDialogResult(final OnePromptDialog onePromptDialog, boolean isCanceled, String value) {
                                            if (!isCanceled) {
                                                onePromptDialog.toggleProgressBar(true);
                                                manager.sendJoinTrip(value, "").addOnCompleteListener(new OnCompleteListener<HttpRequest.APIResponse>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<HttpRequest.APIResponse> task) {
                                                        if (task.isSuccessful()) {
                                                            HttpRequest.APIResponse apiResponse = task.getResult();
                                                            if (apiResponse.success) {
                                                                Toast.makeText(requireContext(), "Thành công", Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Toast.makeText(requireContext(), "Chưa thành công", Toast.LENGTH_LONG).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                        onePromptDialog.dismiss();
                                                        joinTripDialog.dismiss();
                                                    }
                                                });
                                            } else {
                                                onePromptDialog.dismiss();
                                            }
                                        }
                                    }).show(getChildFragmentManager(), "enter trip code");
                                }
                            }
                        }).show(getChildFragmentManager(), "join trip");
            }
        });
    }
}
