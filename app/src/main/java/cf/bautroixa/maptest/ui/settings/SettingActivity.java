package cf.bautroixa.maptest.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.constant.SharedPrefs;
import cf.bautroixa.maptest.ui.theme.OneAppbarActivity;
import cf.bautroixa.maptest.utils.DarkModeHelper;

public class SettingActivity extends OneAppbarActivity {
    SharedPreferences sharedPref;
    LinearLayout linearDarkMode, linearShowNoti, linearVibrate, linearUnit, linearMapStyle;
    TextView tvDarkMode, tvShowNoti, tvVibrate, tvUnit, tvMapStyle;
    String[] settingDarkModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle(R.string.title_setting);
        setSubtitle("");

        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        settingDarkModes = getResources().getStringArray(R.array.setting_dark_mode);

        linearDarkMode = findViewById(R.id.linear_dark_mode_activity_setting);
        linearShowNoti = findViewById(R.id.linear_show_noti_activity_setting);
        linearVibrate = findViewById(R.id.linear_vibrate_activity_setting);
        linearUnit = findViewById(R.id.linear_unit_activity_setting);
        linearMapStyle = findViewById(R.id.linear_map_style_activity_setting);

        tvDarkMode = findViewById(R.id.tv_dark_mode_activity_setting);
        tvShowNoti = findViewById(R.id.tv_show_noti_activity_setting);
        tvVibrate = findViewById(R.id.tv_vibrate_activity_setting);
        tvUnit = findViewById(R.id.tv_unit_activity_setting);
        tvMapStyle = findViewById(R.id.tv_map_style_activity_setting);


        linearDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, DarkModeSettingActivity.class);
                startActivity(intent);
            }
        });
        linearShowNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, BooleanSettingActivity.class);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_NAME, SharedPrefs.SHOW_NOTI);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_TITLE, getString(R.string.title_show_noti_setting));
                startActivity(intent);
            }
        });
        linearVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, BooleanSettingActivity.class);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_NAME, SharedPrefs.VIBRATE);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_TITLE, getString(R.string.title_vibrate_setting));
                startActivity(intent);
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

    @Override
    protected void onResume() {
        super.onResume();
        int darkMode = sharedPref.getInt(SharedPrefs.DARK_MODE, DarkModeHelper.SYSTEM_MODE);
        tvDarkMode.setText(settingDarkModes[darkMode]);
        boolean showNoti = sharedPref.getBoolean(SharedPrefs.SHOW_NOTI, true);
        tvShowNoti.setText(showNoti ? getString(R.string.btn_on) : getString(R.string.btn_off));
        boolean vibrate = sharedPref.getBoolean(SharedPrefs.VIBRATE, true);
        tvVibrate.setText(vibrate ? getString(R.string.btn_on) : getString(R.string.btn_off));
    }
}
