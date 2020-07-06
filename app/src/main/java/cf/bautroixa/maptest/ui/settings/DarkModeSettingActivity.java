package cf.bautroixa.maptest.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Arrays;
import java.util.List;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.sharedpref.SPDarkMode;
import cf.bautroixa.maptest.model.sharedpref.SharedPrefKeys;
import cf.bautroixa.maptest.ui.theme.OneAppbarActivity;

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
        int mode = sharedPref.getInt(SharedPrefKeys.SETTING_DARK_MODE_TYPE, SPDarkMode.SYSTEM_MODE);
        if (mode >= 0 && mode < radioModeButtons.size()) {
            radioModeButtons.get(mode).toggle();
        }
        rgDarkMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                int mode = radioModeIds.indexOf(checkedId);
                if (mode != -1){
                    sharedPref.edit().putInt(SharedPrefKeys.SETTING_DARK_MODE_TYPE, mode).commit();
                    AppCompatDelegate.setDefaultNightMode(SPDarkMode.androidNightModes.get(mode));
                    finish();
                }
            }
        });
    }
}
