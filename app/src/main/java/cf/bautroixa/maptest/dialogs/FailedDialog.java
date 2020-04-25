package cf.bautroixa.maptest.dialogs;

import android.content.DialogInterface;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.theme.OneDialog;

public class FailedDialog extends OneDialog {
    public FailedDialog() {
        setMessageRes(R.string.dialog_message_no_internet);
        setButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
}
