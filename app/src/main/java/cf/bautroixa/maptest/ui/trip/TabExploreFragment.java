package cf.bautroixa.maptest.ui.trip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import cf.bautroixa.maptest.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabExploreFragment extends Fragment {

    public TabExploreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_explore, container, false);
    }
}
