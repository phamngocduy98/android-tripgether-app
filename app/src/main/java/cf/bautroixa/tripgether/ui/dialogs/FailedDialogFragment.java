package cf.bautroixa.tripgether.ui.dialogs;

import android.content.DialogInterface;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.ui.dialogs.OneDialog;

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
