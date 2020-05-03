package cf.bautroixa.maptest.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Arrays;
import java.util.List;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.data.SharedPrefs;
import cf.bautroixa.maptest.theme.OneAppbarActivity;
import cf.bautroixa.maptest.utils.DarkModeHelper;

public class DarkModeSettingActivity extends OneAppbarActivity {
    public static final List<Integer> radioModeIds = Arrays.asList(R.id.radio_system_dark_mode, R.id.radio_light_mode, R.id.radio_dark_mode, R.id.radio_auto_dark_mode);

    SharedPreferences sharedPref;

    private RadioGroup rgDarkMode;
    private RadioButton radioSystem, radioLight, radioDark, radioAuto;
    private List<RadioButton> radioModeButtons;

    public DarkModeSettingActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dark_mode_setting);
        setTitle(R.string.title_dark_mode_setting);
        setSubtitle("");

        rgDarkMode = findViewById(R.id.rg_activity_dark_mode_setting);
        radioSystem = findViewById(R.id.radio_system_dark_mode);
        radioLight = findViewById(R.id.radio_light_mode);
        radioDark = findViewById(R.id.radio_dark_mode);
        radioAuto = findViewById(R.id.radio_auto_dark_mode);

        radioModeButtons = Arrays.asList(radioSystem, radioLight, radioDark, radioAuto);

        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        int mode = sharedPref.getInt(SharedPrefs.DARK_MODE, DarkModeHelper.SYSTEM_MODE);
        if (mode >= 0 && mode < radioModeButtons.size()) {
            radioModeButtons.get(mode).toggle();
        }
        rgDarkMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                int mode = radioModeIds.indexOf(checkedId);
                if (mode != -1){
                    sharedPref.edit().putInt(SharedPrefs.DARK_MODE, mode).commit();
                    AppCompatDelegate.setDefaultNightMode(DarkModeHelper.androidNightModes.get(mode));
                    finish();
                }
            }
        });
    }
}
