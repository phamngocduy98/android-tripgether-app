package cf.bautroixa.tripgether.ui.theme;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class OneLiteAppbarFragment extends Fragment {
    Toolbar toolbar;
    TextView tvSmallTitle, tvSubtitle;
    ImageView imgAvatar;
    ImageButton btnBack;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.appbar_lite_toolbar);
        imgAvatar = view.findViewById(R.id.appbar_img_avatar);
        tvSmallTitle = view.findViewById(R.id.appbar_tv_small_title);
        tvSubtitle = view.findViewById(R.id.appbar_tv_subtitle);

        btnBack = view.findViewById(R.id.appbar_btn_back);
        btnBack.setVisibility(View.INVISIBLE);
    }

    public void setSubtitle(CharSequence title) {
        this.tvSubtitle.setVisibility(View.VISIBLE);
        this.tvSubtitle.setText(title);
    }

    public void setAvatarImage(String avatarUrl) {
        imgAvatar.setVisibility(View.VISIBLE);
        ImageHelper.loadCircleImage(avatarUrl, imgAvatar);
    }

    public void setAvatarImage(@DrawableRes int resId) {
        imgAvatar.setVisibility(View.VISIBLE);
        imgAvatar.setImageResource(resId);
    }

    protected void setBackButtonOnClickListener(View.OnClickListener onClickListener) {
        this.btnBack.setVisibility(View.VISIBLE);
        this.btnBack.setOnClickListener(onClickListener);
    }

    public void setBackButtonIcon(@DrawableRes int resId) {
        btnBack.setImageResource(resId);
    }

    public void setToolbarMenu(@MenuRes int menuResId) {
        toolbar.inflateMenu(menuResId);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }


    public void setTitle(CharSequence title) {
        this.tvSmallTitle.setText(title);
    }
}
