package cf.bautroixa.tripgether.ui.theme;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class OneLiteAppbarActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView tvSmallTitle, tvSubtitle;
    ImageView imgAvatar;
    ImageButton btnBack;

    void findAppbarView() {
        toolbar = findViewById(R.id.appbar_lite_toolbar);
        imgAvatar = findViewById(R.id.appbar_img_avatar);
        tvSmallTitle = findViewById(R.id.appbar_tv_small_title);
        tvSubtitle = findViewById(R.id.appbar_tv_subtitle);

        btnBack = findViewById(R.id.appbar_btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        findAppbarView();
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
        this.btnBack.setOnClickListener(onClickListener);
    }

    public void setToolbarMenu(@MenuRes int menuResId) {
        toolbar.inflateMenu(menuResId);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }


    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        this.tvSmallTitle.setText(title);
    }
}
