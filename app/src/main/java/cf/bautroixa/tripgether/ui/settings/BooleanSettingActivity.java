package cf.bautroixa.tripgether.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.constraintlayout.motion.widget.MotionLayout;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.ui.OneAppbarActivity;

public class BooleanSettingActivity extends OneAppbarActivity {
    public static final String ARG_SETTING_NAME = "ARG_SETTING_NAME";
    public static final String ARG_SETTING_TITLE = "ARG_SETTING_TITLE";

    public static final String ARG_OPTION_1_TITLE = "ARG_OPTION_1_TITLE";
    public static final String ARG_OPTION_2_TITLE = "ARG_OPTION_2_TITLE";

    public static final String ARG_OPTION_1_VALUE = "ARG_OPTION_1_VALUE";
    public static final String ARG_OPTION_2_VALUE = "ARG_OPTION_2_VALUE";

    SharedPreferences sharedPref;

    String settingName, settingTitle, option1Title, option2Title;
    int option1Value, option2Value;

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

        Bundle extras = getIntent().getExtras();
        if (extras == null || extras.size() == 0) {
            finish();
            return;
        }
        settingName = extras.getString(ARG_SETTING_NAME);
        settingTitle = extras.getString(ARG_SETTING_TITLE);
        setTitle(settingTitle);

        option1Title = extras.getString(ARG_OPTION_1_TITLE);
        option2Title = extras.getString(ARG_OPTION_2_TITLE);
        option1Value = extras.getInt(ARG_OPTION_1_VALUE, 0);
        option2Value = extras.getInt(ARG_OPTION_2_VALUE, 1);
    }

    private void booleanSettingInit() {
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

    private void twoOptionSettingInit() {
        radioOn.setText(option1Title);
        radioOff.setText(option2Title);
        int option = sharedPref.getInt(settingName, 0);
        if (option == option1Value) {
            radioOn.toggle();
        } else {
            radioOff.toggle();
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                int selectedOption = checkedId == R.id.radio_on ? option1Value : option2Value;
                sharedPref.edit().putInt(settingName, selectedOption).commit();
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (option1Title == null || option2Title == null) {
            // boolean setting
            booleanSettingInit();
        } else {
            // two option setting
            twoOptionSettingInit();
        }
    }

    @Override
    public MotionLayout findMotionLayout() {
        return findViewById(R.id.appbar_root);
    }
}
