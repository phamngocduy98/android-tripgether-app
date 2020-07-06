package cf.bautroixa.maptest.ui.bottomspace;

import android.content.Intent;
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
import cf.bautroixa.maptest.interfaces.NavigationInterface;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.model.constant.RequestCodes;
import cf.bautroixa.maptest.ui.adapter.BottomToolsAdapter;
import cf.bautroixa.maptest.utils.NavigableHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class BottomToolsFragment extends Fragment implements NavigationInterfaceOwner {

    RecyclerView rvTools;
    private NavigationInterface navigationInterface;

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
        rvTools.setLayoutManager(new GridLayoutManager(requireContext(), 4));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rvTools.setAdapter(new BottomToolsAdapter(requireContext(), this, navigationInterface));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.TOOL_NOTIFICATION && data != null) {
            NavigableHelper.handleNavigation(data, navigationInterface);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationInterface = null;
    }

    @Override
    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }
}
