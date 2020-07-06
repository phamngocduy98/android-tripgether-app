package cf.bautroixa.tripgether.ui.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.Result;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.constant.RequestCodes;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.ui.theme.FullScreenDialogFragment;
import cf.bautroixa.tripgether.ui.theme.OneDialog;
import cf.bautroixa.tripgether.ui.trip_view.TripActivity;
import cf.bautroixa.tripgether.utils.UrlParser;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrScannerDialogFragment extends FullScreenDialogFragment implements ZXingScannerView.ResultHandler {
    private static final String TAG = "QRScanDialogFragment";

    ModelManager manager;
    private ZXingScannerView mScannerView;
//    private OnQrResultListener onResult;

    public QrScannerDialogFragment() {

//        this.onResult = onResult;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(getContext());
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && getContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                OneDialog permissionDialog = new OneDialog.Builder()
                        .title(R.string.dialog_title_permission_camera)
                        .message(R.string.dialog_message_permission_camera)
                        .enableNegativeButton(true).buttonClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, RequestCodes.CAMERA_PERMISSION);
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            dismiss();
                        }
                    }
                }).build();
                permissionDialog.show(getChildFragmentManager(), "request CAMERA_PERMISSION");
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, RequestCodes.CAMERA_PERMISSION);
            }
        } else {
            mScannerView.startCamera();          // Start camera on resume
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mScannerView.startCamera();          // Start camera on resume
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        onResult = null;
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v(TAG, "result = " + rawResult.getText() + ", TYPE = " + rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String[] codes = UrlParser.parseTripCode(requireContext(), rawResult.getText());
        String tripId = codes[0], joinCode = codes[1];
        Intent intent = new Intent(requireContext(), TripActivity.class);
        intent.putExtra(TripActivity.ARG_TRIP_ID, tripId);
        intent.putExtra(TripActivity.ARG_JOIN_CODE, joinCode);
        requireActivity().startActivityForResult(intent, RequestCodes.QR_JOIN_TRIP);
        dismiss();
//        final LoadingDialogFragment loadingDialog = LoadingDialogHelper.create(getChildFragmentManager());

//        TripHttpService.joinTrip(manager.getCurrentUser().getId(), tripId, joinCode).addOnCompleteListener(requireActivity(), new OnCompleteListener<HttpService.APIResponse>() {
//            @Override
//            public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
//                if (task.isSuccessful()) {
//                    HttpService.APIResponse apiResponse = task.getResult();
//                    if (apiResponse != null && apiResponse.success) {
//                        Toast.makeText(getContext(), "Đã tham gia chuyến đi thành công!", Toast.LENGTH_LONG).show();
//                        loadingDialog.dismiss();
//                        dismiss();
//                    } else {
//                        Toast.makeText(getContext(), "Chưa thể tham gia chuyến đi", Toast.LENGTH_LONG).show();
//                    }
//                } else {
//                    Toast.makeText(getContext(), "Chưa thể tham gia chuyến đi", Toast.LENGTH_LONG).show();
//                }
//
//            }
//        });

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

//    public interface OnQrResultListener {
//        void onResult(String result);
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.QR_JOIN_TRIP) {
            if (resultCode == Activity.RESULT_OK) {
                dismiss();
            } else {
                mScannerView.resumeCameraPreview(this);
            }
        }
    }
}
