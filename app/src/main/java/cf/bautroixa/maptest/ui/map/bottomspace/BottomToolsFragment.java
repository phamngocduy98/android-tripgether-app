package cf.bautroixa.maptest.ui.map.bottomspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.ui.adapter.BottomToolsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class BottomToolsFragment extends Fragment implements NavigationInterfaceOwner {

    RecyclerView rvTools;
    private NavigationInterfaces navigationInterfaces;

    public BottomToolsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvTools = view.findViewById(R.id.rv_tools);
        rvTools.setAdapter(new BottomToolsAdapter(requireContext(), getChildFragmentManager(), this, navigationInterfaces));
        rvTools.setLayoutManager(new GridLayoutManager(requireContext(), 4));
    }

    @Override
    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
    }
}
