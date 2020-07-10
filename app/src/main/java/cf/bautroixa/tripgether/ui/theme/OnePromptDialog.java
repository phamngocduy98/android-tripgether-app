package cf.bautroixa.tripgether.ui.theme;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import cf.bautroixa.tripgether.R;

/**
 * One Prompt Dialog
 * Design by Pham Ngoc Duy
 * github.com/phamngocduy98
 */
public class OnePromptDialog extends OneDialog {
    @StringRes
    int hintResId = R.string.dialog_edit_hint;
    EditText editPrompt;
    OnDialogResult onDialogResult;

    public OnePromptDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnableNegativeButton(true);
        final OnePromptDialog finalThis = this;
        setButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editPrompt.getText().length() == 0) {
                    editPrompt.setHintTextColor(Color.RED);
                } else if (onDialogResult != null) {
                    onDialogResult.onDialogResult(finalThis, which == DialogInterface.BUTTON_NEGATIVE, editPrompt.getText().toString());
                }
                if (which == DialogInterface.BUTTON_NEGATIVE) dismiss();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editPrompt = view.findViewById(R.id.edit_prompt_one_dialog);
        editPrompt.setVisibility(View.VISIBLE);
        editPrompt.setHint(hintResId);
        tvMessage.setVisibility(View.GONE);
    }

    public void setOnDialogResultListener(OnDialogResult onDialogResult) {
        this.onDialogResult = onDialogResult;
    }

    public void setHintResId(@StringRes int hintResId) {
        this.hintResId = hintResId;
    }

    public interface OnDialogResult {
        void onDialogResult(OnePromptDialog dialog, boolean isCanceled, String value);
    }

    public static class Builder {
        OnePromptDialog instance;

        public Builder() {
            instance = new OnePromptDialog();
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
            return this;
        }

        public Builder editHintText(@StringRes int hintText) {
            instance.setHintResId(hintText);
            return this;
        }

        public Builder body(View body) {
            instance.setCustomBody(body);
            return this;
        }

        public Builder onResult(OnDialogResult onDialogResult) {
            instance.setOnDialogResultListener(onDialogResult);
            return this;
        }

        public OnePromptDialog build() {
            return instance;
        }

        public void show(FragmentManager fm, String tag) {
            instance.show(fm, tag);
        }
    }
}