package cf.bautroixa.tripgether.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.motion.widget.MotionLayout;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.sharedpref.SPDarkMode;
import cf.bautroixa.tripgether.model.sharedpref.SPGetLost;
import cf.bautroixa.tripgether.model.sharedpref.SPMapStyle;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefHelper;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefKeys;
import cf.bautroixa.tripgether.utils.IntentHelper;
import cf.bautroixa.tripgether.utils.ui_utils.Formater;
import cf.bautroixa.ui.OneAppbarActivity;

public class SettingActivity extends OneAppbarActivity {
    SharedPreferences sharedPref;
    LinearLayout linearGotLost, linearCheckpointReminder, linearDarkMode, linearShowNoti, linearVibrate, linearUnit, linearMapStyle;
    TextView tvGotLost, tvCheckpointReminder, tvDarkMode, tvVibrate, tvUnit, tvMapStyle;
    String[] settingDarkModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle(R.string.title_setting);
        setSubtitle("");

        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        settingDarkModes = getResources().getStringArray(R.array.setting_dark_mode);

        linearGotLost = findViewById(R.id.linear_got_lost_activity_setting);
        linearCheckpointReminder = findViewById(R.id.linear_checkpoint_reminder_activity_setting);
        linearDarkMode = findViewById(R.id.linear_dark_mode_activity_setting);
        linearShowNoti = findViewById(R.id.linear_show_noti_activity_setting);
        linearVibrate = findViewById(R.id.linear_vibrate_activity_setting);
        linearUnit = findViewById(R.id.linear_unit_activity_setting);
        linearMapStyle = findViewById(R.id.linear_map_style_activity_setting);

        tvGotLost = findViewById(R.id.tv_got_lost_activity_setting);
        tvCheckpointReminder = findViewById(R.id.tv_checkpoint_reminder_activity_setting);
        tvDarkMode = findViewById(R.id.tv_dark_mode_activity_setting);
        tvVibrate = findViewById(R.id.tv_vibrate_activity_setting);
        tvUnit = findViewById(R.id.tv_unit_activity_setting);
        tvMapStyle = findViewById(R.id.tv_map_style_activity_setting);


        linearGotLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, GotLostSettingActivity.class));
            }
        });

        linearCheckpointReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, BooleanSettingActivity.class);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_NAME, SharedPrefKeys.SETTING_CHECKPOINT_REMINDER_ON);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_TITLE, getString(R.string.title_checkpoint_reminder_setting));
                startActivity(intent);
            }
        });

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
                IntentHelper.openNotificationSetting(SettingActivity.this);
//                Intent intent = new Intent(SettingActivity.this, BooleanSettingActivity.class);
//                intent.putExtra(BooleanSettingActivity.ARG_SETTING_NAME, SharedPrefKeys.SETTING_SHOW_NOTI_ON);
//                intent.putExtra(BooleanSettingActivity.ARG_SETTING_TITLE, getString(R.string.title_show_noti_setting));
//                startActivity(intent);
            }
        });
        linearVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, BooleanSettingActivity.class);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_NAME, SharedPrefKeys.SETTING_VIBRATE_ON);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_TITLE, getString(R.string.title_vibrate_setting));
                startActivity(intent);
            }
        });
        linearUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, BooleanSettingActivity.class);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_NAME, SharedPrefKeys.SETTING_UNIT_TYPE);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_TITLE, getString(R.string.title_unit_setting));
                intent.putExtra(BooleanSettingActivity.ARG_OPTION_1_TITLE, getString(R.string.setting_censius_meter));
                intent.putExtra(BooleanSettingActivity.ARG_OPTION_2_TITLE, getString(R.string.setting_fahrenheit_inch));
                intent.putExtra(BooleanSettingActivity.ARG_OPTION_1_VALUE, 0);
                intent.putExtra(BooleanSettingActivity.ARG_OPTION_2_VALUE, 1);
                startActivity(intent);
            }
        });
        linearMapStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, BooleanSettingActivity.class);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_NAME, SharedPrefKeys.SETTING_MAP_STYLE);
                intent.putExtra(BooleanSettingActivity.ARG_SETTING_TITLE, getString(R.string.title_map_style_setting));
                intent.putExtra(BooleanSettingActivity.ARG_OPTION_1_TITLE, getString(R.string.setting_street_map));
                intent.putExtra(BooleanSettingActivity.ARG_OPTION_2_TITLE, getString(R.string.setting_satellite_map));
                intent.putExtra(BooleanSettingActivity.ARG_OPTION_1_VALUE, 0);
                intent.putExtra(BooleanSettingActivity.ARG_OPTION_2_VALUE, 1);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean gotLostOn = SPGetLost.isGetLostDetectorOn(sharedPref);
        int safeDistance = SPGetLost.getSafeDistance(sharedPref);
        tvGotLost.setText(String.format("%s | %s", (gotLostOn ? "Bật" : "Tắt"), Formater.formatDistance(safeDistance)));

        boolean reminderOn = SharedPrefHelper.isCheckpointReminderOn(sharedPref);
        tvCheckpointReminder.setText(reminderOn ? getString(R.string.btn_on) : getString(R.string.btn_off));

        int darkMode = sharedPref.getInt(SharedPrefKeys.SETTING_DARK_MODE_TYPE, SPDarkMode.SYSTEM_MODE);
        tvDarkMode.setText(settingDarkModes[darkMode]);

        int unit = sharedPref.getInt(SharedPrefKeys.SETTING_UNIT_TYPE, 0);
        tvUnit.setText(unit == 0 ? getString(R.string.setting_censius_meter) : getString(R.string.setting_fahrenheit_inch));

        int mapStyle = SPMapStyle.getMapStyle(sharedPref);
        tvMapStyle.setText(mapStyle == SPMapStyle.MapStyle.STREET ? getString(R.string.setting_street_map) : getString(R.string.setting_satellite_map));

        boolean vibrate = sharedPref.getBoolean(SharedPrefKeys.SETTING_VIBRATE_ON, true);
        tvVibrate.setText(vibrate ? getString(R.string.btn_on) : getString(R.string.btn_off));
    }

    @Override
    public MotionLayout findMotionLayout() {
        return findViewById(R.id.appbar_root);
    }
}
