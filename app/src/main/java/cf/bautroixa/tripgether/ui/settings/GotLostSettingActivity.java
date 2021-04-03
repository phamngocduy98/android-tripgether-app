package cf.bautroixa.tripgether.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.motion.widget.MotionLayout;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.sharedpref.SPGetLost;
import cf.bautroixa.tripgether.utils.ui_utils.Formater;
import cf.bautroixa.ui.OneAppbarActivity;

public class GotLostSettingActivity extends OneAppbarActivity {
    LinearLayout linearGotLostSwitch;
    Switch switchGotLostOn;
    SeekBar sbSafeDistance;
    TextView tvSwitchTitle, tvSafeDistance;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_got_lost_setting);

        setTitle(R.string.title_got_lost_setting);
        setSubtitle("");

        linearGotLostSwitch = findViewById(R.id.linear_got_lost_switch);
        tvSwitchTitle = findViewById(R.id.tv_got_lost_switch_title);
        switchGotLostOn = findViewById(R.id.switch_got_lost);
        tvSafeDistance = findViewById(R.id.tv_safe_distance);
        sbSafeDistance = findViewById(R.id.seekBar_safe_distance);

        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        switchGotLostOn.setChecked(SPGetLost.isGetLostDetectorOn(sharedPref));
        int safeDistance = SPGetLost.getSafeDistance(sharedPref);
        sbSafeDistance.setProgress(safeDistance);
        tvSafeDistance.setText(Formater.formatDistance(safeDistance));
        tvSwitchTitle.setText(switchGotLostOn.isChecked() ? R.string.switch_toggle_reminder_on : R.string.switch_toggle_reminder_off);
//        tvSwitchTitle.setTextColor(switchGotLostOn.isChecked() ? R.color.colorTextDarkBg : R.color.colorText);
        linearGotLostSwitch.setBackgroundResource(switchGotLostOn.isChecked() ? R.drawable.bg_radius_full_color : R.drawable.bg_radius_full_white);
        switchGotLostOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchGotLostOn.isChecked()) {
                    SPGetLost.turnOnOff(sharedPref, true);
                    tvSwitchTitle.setText(R.string.switch_toggle_reminder_on);
                    tvSwitchTitle.setTextColor(R.color.colorTextDarkBg);
                    linearGotLostSwitch.setBackgroundResource(R.drawable.bg_radius_full_color);
                } else {
                    SPGetLost.turnOnOff(sharedPref, false);
                    tvSwitchTitle.setText(R.string.switch_toggle_reminder_off);
                    tvSwitchTitle.setTextColor(R.color.colorText);
                    linearGotLostSwitch.setBackgroundResource(R.drawable.bg_radius_full_white);
                }
            }
        });

        sbSafeDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSafeDistance.setText(Formater.formatDistance(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                tvSafeDistance.setText(Formater.formatDistance(progress));
                SPGetLost.setSafeDistance(sharedPref, progress);
            }
        });
    }

    @Override
    public MotionLayout findMotionLayout() {
        return findViewById(R.id.appbar_root);
    }
}
