package cf.bautroixa.tripgether.model.ui_item;

import android.content.res.TypedArray;
import android.view.View;

import androidx.lifecycle.LifecycleOwner;

import java.util.List;

public class StateToolItem extends ToolItem {
    private final OnChangeStateListener mOnChangeStateListener;
    private final OnStateToolItemClickedListener mOnToolItemClicked;
    List<String> stateTexts;
    TypedArray iconRes;
    int mState;


    public StateToolItem(int id, LifecycleOwner lifecycleOwner, List<String> stateTexts, TypedArray iconRes, final OnStateToolItemClickedListener onToolItemClicked, final int initState, final OnChangeStateListener onChangeStateListener) {
        this.id = id;
        this.stateTexts = stateTexts;
        this.iconRes = iconRes;
        this.mState = initState;
        activated = mState > 0 ? Activated.ACTIVATED : Activated.DEACTIVATED;
        this.mOnToolItemClicked = onToolItemClicked;
        this.mOnChangeStateListener = onChangeStateListener;
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    @Override
    public String getText() {
        return stateTexts.get(mState);
    }

    @Override
    public int getIcon() {
        return iconRes.getResourceId(mState, -1);
    }

    @Override
    public void onClick(View v) {
        mState = mOnChangeStateListener.newState(mState);
        mOnToolItemClicked.onClick(v, mState);
        activated = mState > 0 ? Activated.ACTIVATED : Activated.DEACTIVATED;
    }

    public interface OnStateToolItemClickedListener {
        void onClick(View v, int state);
    }

    public interface OnChangeStateListener {
        int newState(int oldState);
    }
}
