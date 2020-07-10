package cf.bautroixa.tripgether.ui.bottomspace;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.MapBackgroundControllable;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomMembersPresent;
import cf.bautroixa.tripgether.presenter.bottomspace.MapPresenter;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomMembersPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.BottomMembersAdapter;
import cf.bautroixa.tripgether.utils.ui_utils.RecyclerViewOnScrollListener;

public class BottomMembersFragment extends Fragment implements BottomMembersPresent.View, NavigationInterfaceOwner, MapBackgroundControllable {
    private static final String TAG = "FriendFragment";
    public static final String ARG_POS = "pos";
    // Data and state
    BottomMembersPresenterImpl bottomMembersPresenter;
    int currentPosition = 0;

    // Listener
    private MapPresenter.CallableMask mapBackgroundInterfaces;
    private NavigationInterface navigationInterface;

    // View
    private RecyclerView rv;
    private TextView tvCount;

    public BottomMembersFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        bottomMembersPresenter = new BottomMembersPresenterImpl(this, context, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCount = view.findViewById(R.id.tv_count_frag_friend);
        rv = view.findViewById(R.id.rv_friends);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(ARG_POS, 0);
        }
        rv.smoothScrollToPosition(currentPosition);
        bottomMembersPresenter.initAdapter(this, navigationInterface, getChildFragmentManager());
    }

    @Override
    public void setupAdapter(BottomMembersAdapter adapter) {
        rv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rv.setLayoutManager(linearLayoutManager);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        rv.addOnScrollListener(new RecyclerViewOnScrollListener(RecyclerViewOnScrollListener.ScrollMode.SCROLL_IDLE, snapHelper, new RecyclerViewOnScrollListener.OnNewPosition() {
            @Override
            public void onNewPosition(int position) {
                bottomMembersPresenter.onScrollNewPosition(position);
            }
        }));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mapBackgroundInterfaces = null;
        navigationInterface = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_POS, currentPosition);
    }

    public void setMapBackgroundInterface(MapPresenter.CallableMask mapBackgroundInterface) {
        this.mapBackgroundInterfaces = mapBackgroundInterface;
    }

    @Override
    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }

    public BottomMembersPresenterImpl getBottomMembersPresenter() {
        return bottomMembersPresenter;
    }

    @Override
    public void setUpNoString(int i, int size) {
        tvCount.setText(String.format("%d/%d", i, size));
    }

    @Override
    public void scrollToPosition(int position) {
        currentPosition = position;
        if (isResumed() && rv != null) {
            rv.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onTargetUser(User user) {
        if (mapBackgroundInterfaces != null) {
            mapBackgroundInterfaces.cleanUpTempMarkerAndRoute();
            mapBackgroundInterfaces.target(user);
        }
    }
}
