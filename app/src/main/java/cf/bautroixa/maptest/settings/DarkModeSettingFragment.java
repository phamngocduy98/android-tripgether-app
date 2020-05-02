package cf.bautroixa.maptest.settings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cf.bautroixa.maptest.R;

public class DarkModeSettingFragment extends Fragment {

    public DarkModeSettingFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dark_mode_setting, container, false);
    }
}
