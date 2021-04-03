package cf.bautroixa.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.TransitionAdapter;
import androidx.fragment.app.Fragment;

import cf.bautroixa.ui.interfaces.OnAppbarStateChanged;

public abstract class OneAppbarFragment extends Fragment {
    protected MotionLayout motionLayout;
    protected ImageButton btnBack;
    Toolbar toolbar;
    TextView tvLargeTitle, tvSmallTitle, tvSubtitle;
    OnAppbarStateChanged onAppbarStateChanged;

    int appbarState = OnAppbarStateChanged.State.EXTENDED;

    public abstract MotionLayout findMotionLayout(View view);

    public void setSubtitle(CharSequence title) {
        if (this.tvSubtitle != null) {
            this.tvSubtitle.setVisibility(View.VISIBLE);
            this.tvSubtitle.setText(title);
        }
    }

    public void setTitle(CharSequence title) {
        if (this.tvLargeTitle != null) this.tvLargeTitle.setText(title);
        if (this.tvSmallTitle != null) this.tvSmallTitle.setText(title);
    }

    public void setToolbarMenu(@MenuRes int menuResId) {
        toolbar.getMenu().clear();
        toolbar.inflateMenu(menuResId);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setAppbarState(int state) {
        appbarState = state;
    }

    public void setOnAppbarStateChanged(OnAppbarStateChanged onAppbarStateChanged) {
        this.onAppbarStateChanged = onAppbarStateChanged;
    }

    protected void setBackButtonOnClickListener(View.OnClickListener onClickListener) {
        this.btnBack.setVisibility(View.VISIBLE);
        this.btnBack.setOnClickListener(onClickListener);
    }

    protected void setBackButtonIcon(@DrawableRes int resId) {
        this.btnBack.setVisibility(View.VISIBLE);
        this.btnBack.setImageResource(resId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        motionLayout = findMotionLayout(view);
        toolbar = view.findViewById(R.id.appbar_scrollable_toolbar);
        tvLargeTitle = view.findViewById(R.id.appbar_tv_large_title);
        tvSubtitle = view.findViewById(R.id.appbar_tv_subtitle);
        tvSmallTitle = view.findViewById(R.id.appbar_tv_small_title);
        btnBack = view.findViewById(R.id.appbar_btn_back);
        btnBack.setVisibility(View.INVISIBLE);
        motionLayout.setTransitionListener(new TransitionAdapter() {
            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                if (onAppbarStateChanged == null) return;
                if (currentId == R.id.end)
                    onAppbarStateChanged.newState(OnAppbarStateChanged.State.COLLAPSED);
                else onAppbarStateChanged.newState(OnAppbarStateChanged.State.EXTENDED);
            }
        });
        if (appbarState == OnAppbarStateChanged.State.COLLAPSED) {
            motionLayout.setProgress(1f);
        } else {
            motionLayout.setProgress(0f);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onAppbarStateChanged = null;
    }
}
