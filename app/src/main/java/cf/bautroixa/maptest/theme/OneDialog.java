package cf.bautroixa.maptest.theme;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import cf.bautroixa.maptest.R;

/**
 * One Dialog
 * Design by Pham Ngoc Duy
 * github.com/phamngocduy98
 */
public class OneDialog extends DialogFragment {

    public static class Builder {
        OneDialog instance;
        public Builder() {
            instance = new OneDialog();
        }
        public Builder title(@StringRes int titleRes){
            instance.setTitleRes(titleRes);
            return this;
        }
        public Builder message(@StringRes int messageRes){
            instance.setMessageRes(messageRes);
            return this;
        }
        public Builder posBtnText(@StringRes int posBtnRes){
            instance.setPosBtnRes(posBtnRes);
            return this;
        }
        public Builder negBtnText(@StringRes int negBtnRes){
            instance.setNegBtnRes(negBtnRes);
            return this;
        }
        public Builder body(View body){
            instance.setCustomBody(body);
            return this;
        }
        public Builder enableNegativeButton(boolean enable){
            instance.setEnableNegativeButton(enable);
            return this;
        }
        public Builder buttonClickListener(DialogInterface.OnClickListener buttonClickListener) {
            instance.setButtonClickListener(buttonClickListener);
            return this;
        }
        public OneDialog build(){
            return instance;
        }
    }

    DialogInterface.OnClickListener btnClickListener;
    TextView tvTitle, tvMessage, tvBtnDivider;
    LinearLayout containerBody;
    View customBody;
    Button btnPos, btnNeg;
    boolean isEnableNegativeButton = false;
    int titleRes = R.string.dialog_title, messageRes = R.string.dialog_messsage, posBtnRes = R.string.btn_got_it, negBtnRes = R.string.btn_cancel;

    public OneDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_radius_full_white);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.theme_one_dialog, container, false);
        tvTitle = v.findViewById(R.id.tv_title_one_dialog);
        containerBody = v.findViewById(R.id.container_body_one_dialog);
        tvMessage = v.findViewById(R.id.tv_message_one_dialog);
        btnPos = v.findViewById(R.id.btn_positive_one_dialog);
        tvBtnDivider = v.findViewById(R.id.tv_buttions_divider);
        btnNeg = v.findViewById(R.id.btn_negative_one_dialog);

        tvTitle.setText(titleRes);
        btnPos.setText(posBtnRes);
        btnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnClickListener != null) btnClickListener.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
            }
        });
        if (customBody != null){
            containerBody.removeAllViews();
            containerBody.addView(customBody);
        } else {
            tvMessage.setText(messageRes);
        }
        if (isEnableNegativeButton){
            btnNeg.setText(negBtnRes);
            btnNeg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnClickListener != null) btnClickListener.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
                }
            });
        } else {
            btnNeg.setVisibility(View.GONE);
            tvBtnDivider.setVisibility(View.GONE);
        }
        return v;
    }

    public void setEnableNegativeButton(boolean enableNegativeButton) {
        isEnableNegativeButton = enableNegativeButton;
    }

    public void setTitleRes(int titleRes) {
        this.titleRes = titleRes;
    }

    public void setMessageRes(int messageRes) {
        this.messageRes = messageRes;
    }

    public void setPosBtnRes(int posBtnRes) {
        this.posBtnRes = posBtnRes;
    }

    public void setNegBtnRes(int negBtnRes) {
        this.negBtnRes = negBtnRes;
    }

    public void setCustomBody(View customBody) {
        this.customBody = customBody;
    }

    public void setButtonClickListener(DialogInterface.OnClickListener btnClickListener) {
        this.btnClickListener = btnClickListener;
    }

}