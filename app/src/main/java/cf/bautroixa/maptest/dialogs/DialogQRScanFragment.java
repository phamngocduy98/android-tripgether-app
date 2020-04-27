package cf.bautroixa.maptest.dialogs;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.Result;

import cf.bautroixa.maptest.data.RequestCodes;
import cf.bautroixa.maptest.theme.FullScreenDialogFragment;
import cf.bautroixa.maptest.theme.OneDialog;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class DialogQRScanFragment extends FullScreenDialogFragment implements ZXingScannerView.ResultHandler {
    private static final String TAG = "QRScanDialogFragment";
    private ZXingScannerView mScannerView;
    private OnQrResultListener onResult;

    public interface OnQrResultListener {
        void onResult(String result);
    }

    public DialogQRScanFragment(OnQrResultListener onResult) {
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
                OneDialog permissionDialog = new OneDialog.Builder().enableNegativeButton(true).buttonClickListener(new DialogInterface.OnClickListener() {
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
        onResult.onResult(rawResult.getText());
        dismiss();
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
}
