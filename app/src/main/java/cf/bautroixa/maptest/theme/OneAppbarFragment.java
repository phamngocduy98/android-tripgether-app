package cf.bautroixa.maptest.theme;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.TransitionAdapter;
import androidx.fragment.app.Fragment;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.OnAppbarStateChanged;

public class OneAppbarFragment extends Fragment {
    protected MotionLayout motionLayout;
    protected ImageButton btnBack;
    Toolbar toolbar;
    TextView tvLargeTitle, tvSmallTitle, tvSubtitle;
    OnAppbarStateChanged onAppbarStateChanged;

    int appbarState = OnAppbarStateChanged.State.EXTENDED;

    public void setOnAppbarStateChanged(OnAppbarStateChanged onAppbarStateChanged) {
        this.onAppbarStateChanged = onAppbarStateChanged;
    }

    protected void setBackButtonOnClickListener(View.OnClickListener onClickListener) {
        this.btnBack.setOnClickListener(onClickListener);
    }

    public void setSubtitle(CharSequence title) {
        if (this.tvSubtitle != null) this.tvSubtitle.setText(title);
    }

    public void setTitle(CharSequence title) {
        if (this.tvLargeTitle != null) this.tvLargeTitle.setText(title);
        if (this.tvSmallTitle != null) this.tvSmallTitle.setText(title);
    }

    public void setToolbarMenu(@MenuRes int menuResId) {
        toolbar.inflateMenu(menuResId);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setAppbarState(int state) {
        appbarState = state;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        motionLayout = view.findViewById(R.id.appbar_root);
        toolbar = view.findViewById(R.id.appbar_scrollable_toolbar);
        tvLargeTitle = view.findViewById(R.id.appbar_tv_large_title);
        tvSubtitle = view.findViewById(R.id.appbar_tv_subtitle);
        tvSmallTitle = view.findViewById(R.id.appbar_tv_small_title);
        btnBack = view.findViewById(R.id.appbar_btn_back);
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
