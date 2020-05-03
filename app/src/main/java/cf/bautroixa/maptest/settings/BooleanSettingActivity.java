package cf.bautroixa.maptest.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.theme.OneAppbarActivity;

public class BooleanSettingActivity extends OneAppbarActivity {
    public static final String ARG_SETTING_NAME = "ARG_SETTING_NAME";
    public static final String ARG_SETTING_TITLE = "ARG_SETTING_TITLE";
    SharedPreferences sharedPref;

    private RadioGroup rg;
    private RadioButton radioOn, radioOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boolean_setting);
        setSubtitle("");
        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);

        rg = findViewById(R.id.rg_activity_boolean_setting);
        radioOn = findViewById(R.id.radio_on);
        radioOff = findViewById(R.id.radio_off);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras == null || extras.size() == 0) {
            finish();
            return;
        }
        final String settingName = extras.getString(ARG_SETTING_NAME);
        String settingTitle = extras.getString(ARG_SETTING_TITLE);
        setTitle(settingTitle);
        boolean onOff = sharedPref.getBoolean(settingName, true);
        if (onOff) {
            radioOn.toggle();
        } else {
            radioOff.toggle();
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                sharedPref.edit().putBoolean(settingName, checkedId == R.id.radio_on).commit();
                finish();
            }
        });
    }
}
