package cf.bautroixa.maptest.ui.dialogs;

import android.content.DialogInterface;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.ui.theme.OneDialog;

public class FailedDialogFragment extends OneDialog {
    public FailedDialogFragment() {
        setMessageRes(R.string.dialog_message_no_internet);
        setButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
}
