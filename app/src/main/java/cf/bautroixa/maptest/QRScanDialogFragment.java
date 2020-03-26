package cf.bautroixa.maptest;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.Result;

import cf.bautroixa.maptest.theme.FullScreenDialogFragment;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScanDialogFragment extends FullScreenDialogFragment implements ZXingScannerView.ResultHandler {
    private static final String TAG = "QRScanDialogFragment";
    private ZXingScannerView mScannerView;
    private OnQrResultListener onResult = null;

    public interface OnQrResultListener {
        void onResult(String result);
    }

    public QRScanDialogFragment(OnQrResultListener onResult) {
        this.onResult = onResult;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(getContext());
        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
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
