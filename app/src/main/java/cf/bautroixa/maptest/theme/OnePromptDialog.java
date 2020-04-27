package cf.bautroixa.maptest.theme;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import cf.bautroixa.maptest.R;

/**
 * One Prompt Dialog
 * Design by Pham Ngoc Duy
 * github.com/phamngocduy98
 */
public class OnePromptDialog extends OneDialog {

    public void setOnDialogResultListener(OnDialogResult onDialogResult) {
        this.onDialogResult = onDialogResult;
    }

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
                if (onDialogResult != null)
                    onDialogResult.onDialogResult(finalThis, which == DialogInterface.BUTTON_NEGATIVE, editPrompt.getText().toString());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            editPrompt = v.findViewById(R.id.edit_prompt_one_dialog);
            editPrompt.setVisibility(View.VISIBLE);
        }
        tvMessage.setVisibility(View.GONE);
        return v;
    }

    public interface OnDialogResult {
        void onDialogResult(OnePromptDialog dialog, boolean isCanceled, String value);
    }

    public static class Builder {
        OnePromptDialog instance;

        public Builder() {
            instance = new OnePromptDialog();
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