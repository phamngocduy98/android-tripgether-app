package cf.bautroixa.maptest.theme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cf.bautroixa.maptest.R;

/**
 * One Dialog
 * Design by Pham Ngoc Duy
 * github.com/phamngocduy98
 */
public class OneDialog extends DialogFragment {
    DialogInterface.OnClickListener positiveBtnClick, negativeBtnClick;

    public OneDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        TextView tvMessage = new TextView(getContext());
        tvMessage.setText(getMessageRes());
        tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        TypedValue typedValue = new TypedValue();
        int paddingHorizontal = getContext().getTheme().resolveAttribute(R.attr.dialogPreferredPadding,typedValue, true)?TypedValue.complexToDimensionPixelSize(typedValue.data, getContext().getResources().getDisplayMetrics()):0;
        tvMessage.setPadding(paddingHorizontal,32,paddingHorizontal,24);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getTitleRes())
                .setView(tvMessage)
                .setCancelable(false)
                .setPositiveButton(getPositiveButtonTextRes(), positiveBtnClick);
        if (isEnableNegativeButton()){
            builder.setNegativeButton(getNegativeButtonTextRes(), negativeBtnClick);
        }
        AlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_radius_full_white);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(Color.WHITE);
                positiveButton.setBackgroundResource(R.drawable.btn_raised);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
                positiveButton.setLayoutParams(params);
                positiveButton.invalidate();
            }
        });
        return dialog;
    }

    public int getTitleRes(){
        return R.string.dialog_title;
    }

    public int getMessageRes(){
        return R.string.dialog_messsage;
    }


    public int getPositiveButtonTextRes(){
        return R.string.btn_got_it;
    }

    public int getNegativeButtonTextRes(){
        return R.string.btn_cancel;
    }

    public boolean isEnableNegativeButton(){
        return false;
    }

    public void setNegativeBtnClick(DialogInterface.OnClickListener negativeBtnClick) {
        this.negativeBtnClick = negativeBtnClick;
    }

    public void setPositiveBtnClick(DialogInterface.OnClickListener positiveBtnClick) {
        this.positiveBtnClick = positiveBtnClick;
    }
}