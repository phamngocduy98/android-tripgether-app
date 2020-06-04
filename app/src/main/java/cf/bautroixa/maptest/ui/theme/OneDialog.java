package cf.bautroixa.maptest.ui.theme;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import cf.bautroixa.maptest.R;

/**
 * One Dialog
 * Design by Pham Ngoc Duy
 * github.com/phamngocduy98
 */
public class OneDialog extends DialogFragment {
    DialogInterface.OnClickListener btnClickListener;
    @StringRes
    int titleRes = R.string.dialog_title, messageRes = R.string.dialog_messsage, posBtnRes = R.string.btn_ok, negBtnRes = R.string.btn_cancel;
    @DrawableRes
    int iconRes;
    boolean enableNegativeButton = false, isProcessing = false, enableTitleIcon = false;

    TextView tvTitle, tvMessage;
    LinearLayout containerBody;
    View customBody;
    Button btnPos, btnNeg;
    ImageView icTitle;

    public void toggleProgressBar(boolean onOff) {
        isProcessing = onOff;
        if (onOff) {
            // ON
            btnNeg.setVisibility(View.GONE);
            btnPos.setText("Loading...");
            btnPos.setEnabled(false);
        } else {
            // OFF
            if (enableNegativeButton) {
                btnNeg.setVisibility(View.VISIBLE);
            }
            btnPos.setText(posBtnRes);
            btnPos.setEnabled(true);
        }

    }

    public OneDialog() {
        btnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.OneUI_Dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(null);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.theme_one_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        icTitle = view.findViewById(R.id.ic_title_one_dialog);
        tvTitle = view.findViewById(R.id.tv_title_one_dialog);
        containerBody = view.findViewById(R.id.container_body_one_dialog);
        tvMessage = view.findViewById(R.id.tv_message_one_dialog);
        btnPos = view.findViewById(R.id.btn_positive_one_dialog);
        btnNeg = view.findViewById(R.id.btn_negative_one_dialog);

        tvTitle.setText(titleRes);
        btnPos.setText(posBtnRes);
        btnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnClickListener != null && !isProcessing)
                    btnClickListener.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
            }
        });
        if (enableTitleIcon) {
            icTitle.setImageResource(iconRes);
            icTitle.setVisibility(View.VISIBLE);
            tvTitle.setGravity(Gravity.CENTER);
            tvMessage.setGravity(Gravity.CENTER);
        }
        if (customBody != null) {
            containerBody.removeAllViews();
            containerBody.addView(customBody);
        } else {
            tvMessage.setText(messageRes);
        }
        if (enableNegativeButton) {
            btnNeg.setText(negBtnRes);
            btnNeg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnClickListener != null && !isProcessing)
                        btnClickListener.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
                }
            });
        } else {
            btnNeg.setVisibility(View.GONE);
        }
    }

    public void setEnableNegativeButton(boolean enableNegativeButton) {
        this.enableNegativeButton = enableNegativeButton;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
        this.enableTitleIcon = true;
    }

    public void setTitleRes(@StringRes int titleRes) {
        this.titleRes = titleRes;
    }

    public void setMessageRes(@StringRes int messageRes) {
        this.messageRes = messageRes;
    }

    public void setPosBtnRes(@StringRes int posBtnRes) {
        this.posBtnRes = posBtnRes;
    }

    public void setNegBtnRes(@StringRes int negBtnRes) {
        this.negBtnRes = negBtnRes;
        this.enableNegativeButton = true;
    }

    public static class Builder {
        OneDialog instance;

        public Builder() {
            instance = new OneDialog();
        }

        public Builder iconTitle(@DrawableRes int iconRes) {
            instance.setIconRes(iconRes);
            return this;
        }

        public Builder title(@StringRes int titleRes) {
            instance.setTitleRes(titleRes);
            return this;
        }

        public Builder message(@StringRes int messageRes) {
            instance.setMessageRes(messageRes);
            return this;
        }

        public Builder posBtnText(@StringRes int posBtnRes) {
            instance.setPosBtnRes(posBtnRes);
            return this;
        }

        public Builder negBtnText(@StringRes int negBtnRes) {
            instance.setNegBtnRes(negBtnRes);
            instance.setEnableNegativeButton(true);
            return this;
        }

        public Builder body(View body) {
            instance.setCustomBody(body);
            return this;
        }

        public Builder enableNegativeButton(boolean enable) {
            instance.setEnableNegativeButton(enable);
            return this;
        }

        public Builder buttonClickListener(DialogInterface.OnClickListener buttonClickListener) {
            instance.setButtonClickListener(buttonClickListener);
            return this;
        }

        public OneDialog build() {
            return instance;
        }

        public void show(FragmentManager manager, String tag) {
            instance.show(manager, tag);
        }
    }

    public void setCustomBody(View customBody) {
        this.customBody = customBody;
    }

    public void setButtonClickListener(DialogInterface.OnClickListener btnClickListener) {
        this.btnClickListener = btnClickListener;
    }

}