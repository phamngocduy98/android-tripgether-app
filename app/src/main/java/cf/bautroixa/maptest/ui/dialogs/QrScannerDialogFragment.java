package cf.bautroixa.maptest.ui.dialogs;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.Result;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.constant.RequestCodes;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.http.HttpRequest;
import cf.bautroixa.maptest.ui.theme.FullScreenDialogFragment;
import cf.bautroixa.maptest.ui.theme.LoadingDialogFragment;
import cf.bautroixa.maptest.ui.theme.OneDialog;
import cf.bautroixa.maptest.utils.UrlParser;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrScannerDialogFragment extends FullScreenDialogFragment implements ZXingScannerView.ResultHandler {
    private static final String TAG = "QRScanDialogFragment";

    ModelManager manager;
    private ZXingScannerView mScannerView;
    private OnQrResultListener onResult;

    public QrScannerDialogFragment(OnQrResultListener onResult) {
        manager = ModelManager.getInstance();
        this.onResult = onResult;
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
        onResult = null;
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v(TAG, "result = " + rawResult.getText() + ", TYPE = " + rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String[] codes = UrlParser.parseTripCode(requireContext(), rawResult.getText());
        String tripCode = codes[0], joinCode = codes[1];
        final LoadingDialogFragment loadingDialog = new LoadingDialogFragment();
        loadingDialog.show(getChildFragmentManager(), "loading...");
        manager.sendJoinTrip(tripCode, joinCode).addOnCompleteListener(requireActivity(), new OnCompleteListener<HttpRequest.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpRequest.APIResponse> task) {
                if (task.isSuccessful()) {
                    HttpRequest.APIResponse apiResponse = task.getResult();
                    if (apiResponse != null && apiResponse.success) {
                        Toast.makeText(getContext(), "Đã tham gia chuyến đi thành công!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Chưa thể tham gia chuyến đi", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Chưa thể tham gia chuyến đi", Toast.LENGTH_LONG).show();
                }
                loadingDialog.dismiss();
                dismiss();
            }
        });

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

    public interface OnQrResultListener {
        void onResult(String result);
    }
}
