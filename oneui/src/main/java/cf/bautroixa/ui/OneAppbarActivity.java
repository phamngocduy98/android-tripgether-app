package cf.bautroixa.ui;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;

public abstract class OneAppbarActivity extends AppCompatActivity {
    protected MotionLayout motionLayout;
    Toolbar toolbar;
    TextView tvLargeTitle, tvSmallTitle, tvSubtitle;
    ImageButton btnBack;

    public abstract MotionLayout findMotionLayout();

    void findAppbarView() {
        motionLayout = findMotionLayout();
        toolbar = findViewById(R.id.appbar_scrollable_toolbar);
        tvLargeTitle = findViewById(R.id.appbar_tv_large_title);
        tvSubtitle = findViewById(R.id.appbar_tv_subtitle);
        tvSmallTitle = findViewById(R.id.appbar_tv_small_title);
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
        this.tvLargeTitle.setText(title);
        this.tvSmallTitle.setText(title);
    }
}
