package cf.bautroixa.maptest.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.ui.theme.OneDialog;
import cf.bautroixa.maptest.ui.theme.OnePromptDialog;
import cf.bautroixa.maptest.ui.trip_view.TripActivity;

public class JoinTripDialog extends OneDialog {
    ModelManager manager;
    View linearEnterTripId, linearScanQr;

    public JoinTripDialog() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View body = inflater.inflate(R.layout.dialog_body_join_trip, container, false);
        setCustomBody(body);
        setTitleRes(R.string.dialog_title_join_trip);
        setPosBtnRes(R.string.btn_cancel);
        setButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearEnterTripId = view.findViewById(R.id.linear_enter_trip_code);
        linearScanQr = view.findViewById(R.id.linear_qr_scan);

        linearScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QrScannerDialogFragment().show(getChildFragmentManager(), "qr scanner");
            }
        });

        linearEnterTripId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OnePromptDialog.Builder().title(R.string.dialog_title_enter_trip_code)
                        .iconBody(R.drawable.ic_validating_ticket)
                        .editHintText(R.string.dialog_title_enter_trip_code)
                        .onResult(new OnePromptDialog.OnDialogResult() {
                            @Override
                            public void onDialogResult(final OnePromptDialog onePromptDialog, boolean isCanceled, String value) {
                                if (isCanceled) {
                                    onePromptDialog.dismiss();
                                    return;
                                }
                                Intent intent = new Intent(requireContext(), TripActivity.class);
                                intent.putExtra(TripActivity.ARG_TRIP_ID, value);
//                                intent.putExtra(TripActivity.ARG_JOIN_CODE, joinCode);
                                requireContext().startActivity(intent);
//                                onePromptDialog.toggleProgressBar(true);
//                                TripHttpService.joinTrip(manager.getCurrentUser().getId(), value, "").addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
//                                        if (task.isSuccessful()) {
//                                            HttpService.APIResponse apiResponse = task.getResult();
//                                            if (apiResponse.success) {
//                                                Toast.makeText(requireContext(), "Thành công", Toast.LENGTH_LONG).show();
//                                            } else {
//                                                Toast.makeText(requireContext(), "Chưa thành công", Toast.LENGTH_LONG).show();
//                                            }
//                                        } else {
//                                            Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                                        }
//                                        onePromptDialog.dismiss();
//                                        dismiss();
//                                    }
//                                });
                            }
                        }).show(getChildFragmentManager(), "enter trip code");
            }
        });
    }
}
