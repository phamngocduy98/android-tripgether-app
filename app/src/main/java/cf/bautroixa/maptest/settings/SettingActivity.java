package cf.bautroixa.maptest.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import cf.bautroixa.maptest.R;

public class SettingActivity extends AppCompatActivity {


    LinearLayout linearDarkMode, linearShowNoti, linearVibrate, linearUnit, linearMapStyle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        linearDarkMode = findViewById(R.id.linear_dark_mode_activity_setting);
        linearShowNoti = findViewById(R.id.linear_show_noti_activity_setting);
        linearVibrate = findViewById(R.id.linear_vibrate_activity_setting);
        linearUnit = findViewById(R.id.linear_unit_activity_setting);
        linearMapStyle = findViewById(R.id.linear_map_style_activity_setting);

        linearDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        linearShowNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        linearVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        linearUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        linearMapStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void replaceSubSetting(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.linear_subsetting_space, fragment);
        ft.commit();
    }
}
