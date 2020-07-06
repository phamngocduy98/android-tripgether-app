package cf.bautroixa.maptest.ui.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import cf.bautroixa.maptest.ui.theme.LoadingDialogFragment;

public class LoadingDialogHelper {
    public static ProgressDialog create(Context context, String loadingText) {
        ProgressDialog loadingDialog = ProgressDialog.show(context, "", loadingText, true, false);
        loadingDialog.setCustomTitle(new View(context));
        return loadingDialog;
    }

    public static LoadingDialogFragment create(FragmentManager fragmentManager) {
        LoadingDialogFragment loadingFragment = new LoadingDialogFragment();
        loadingFragment.show(fragmentManager, "LoadingDialogFragment");
        return loadingFragment;
    }
}
