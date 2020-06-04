package cf.bautroixa.maptest.ui.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;

public class LoadingDialogHelper {
    public static ProgressDialog create(Context context, String loadingText) {
        ProgressDialog loadingDialog = ProgressDialog.show(context, "", loadingText, true, false);
        loadingDialog.setCustomTitle(new View(context));
        return loadingDialog;
    }
}
