package cf.bautroixa.maptest.ui.theme;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.ui.dialogs.LoadingDialogHelper;

/**
 * One Dialog
 * Design by Pham Ngoc Duy
 * github.com/phamngocduy98
 */
public class OneDialog extends OneDialogBase {
    DialogInterface.OnClickListener btnClickListener;
    @StringRes
    int titleRes = R.string.dialog_title, messageRes = R.string.dialog_messsage, posBtnRes = R.string.btn_ok, negBtnRes = R.string.btn_cancel;
    @DrawableRes
    int iconTitleRes, iconBodyRes;
    boolean enableNegativeButton = false, isProcessing = false, enableTitleIcon = false, enableBodyIcon = false;

    TextView tvTitle, tvMessage;
    LinearLayout containerBody, containerMessageAndEditText;
    View customBody;
    Button btnPos, btnNeg;
    ImageView icTitle, icBody;
    private ProgressDialog loadingDialog;

    public OneDialog() {
        btnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public void toggleProgressBar(boolean onOff) {
        isProcessing = onOff;
        if (onOff) {
            // ON
            loadingDialog = LoadingDialogHelper.create(requireContext(), "Vui lòng đợi");
            btnNeg.setVisibility(View.GONE);
            btnPos.setText("Loading...");
            btnPos.setEnabled(false);
        } else {
            // OFF
            if (loadingDialog != null) loadingDialog.dismiss();
            if (enableNegativeButton) {
                btnNeg.setVisibility(View.VISIBLE);
            }
            btnPos.setText(posBtnRes);
            btnPos.setEnabled(true);
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
        containerMessageAndEditText = view.findViewById(R.id.container_message_and_edit_text);
        tvMessage = view.findViewById(R.id.tv_message_one_dialog);
        icBody = view.findViewById(R.id.ic_body_one_dialog);
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
            icTitle.setImageResource(iconTitleRes);
            icTitle.setVisibility(View.VISIBLE);
            tvTitle.setGravity(Gravity.CENTER);
            tvMessage.setGravity(Gravity.CENTER);
        }
        if (enableBodyIcon) {
//            tvTitle.setGravity(Gravity.CENTER);
            icBody.setImageResource(iconBodyRes);
            icBody.setVisibility(View.VISIBLE);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            int margin = (int) PixelDPConverter.convertDpToPixel(16, requireContext());
//            layoutParams.setMargins(margin, 0, margin, 0);
//            containerBody.setLayoutParams(layoutParams);
        }
        if (customBody != null) {
            containerBody.removeAllViews();
            containerBody.addView(customBody, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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

    public void setIconTitleRes(int iconTitleRes) {
        this.iconTitleRes = iconTitleRes;
        this.enableTitleIcon = true;
    }

    public void setIconBodyRes(int iconBodyRes) {
        this.iconBodyRes = iconBodyRes;
        this.enableBodyIcon = true;
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

    public void setCustomBody(View customBody) {
        this.customBody = customBody;
    }

    public void setButtonClickListener(DialogInterface.OnClickListener btnClickListener) {
        this.btnClickListener = btnClickListener;
    }

    public static class Builder {
        OneDialog instance;

        public Builder() {
            instance = new OneDialog();
        }

        public Builder iconTitle(@DrawableRes int iconRes) {
            instance.setIconTitleRes(iconRes);
            return this;
        }

        public Builder iconBody(@DrawableRes int iconRes) {
            instance.setIconBodyRes(iconRes);
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

}