package cf.bautroixa.maptest.ui.map.bottomspace;

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

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.MapBackgroundControllable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.presenter.BottomMembersPresent;
import cf.bautroixa.maptest.presenter.MapPresenter;
import cf.bautroixa.maptest.presenter.impl.BottomMembersPresenterImpl;
import cf.bautroixa.maptest.ui.adapter.BottomMembersAdapter;
import cf.bautroixa.maptest.utils.RecyclerViewOnScrollListener;

public class BottomMembersFragment extends Fragment implements BottomMembersPresent.View, NavigationInterfaceOwner, MapBackgroundControllable {
    private static final String TAG = "FriendFragment";
    // Data and state
    BottomMembersPresenterImpl bottomMembersPresenter;

    // Listener
    private MapPresenter.CallableMask mapBackgroundInterfaces;
    private NavigationInterfaces navigationInterfaces;

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

//        btnCall = view.findViewById(R.id.btn_call_frag_friend);
//        btnDirection = view.findViewById(R.id.btn_direction_frag_friend);
//        btnMessage = view.findViewById(R.id.btn_message_frag_friend);
//        btnShowSos = view.findViewById(R.id.btn_view_sos_frag_friend);

        bottomMembersPresenter.initAdapter(this, navigationInterfaces, getChildFragmentManager());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mapBackgroundInterfaces = null;
        navigationInterfaces = null;
    }

    @Override
    public void updateView(final User selectedUser) {
//        btnCall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + selectedUser.getPhoneNumber()));
//                startActivity(intent);
//            }
//        });
//        btnDirection.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mapBackgroundInterfaces.drawRoute(null, selectedUser.getLatLng());
//            }
//        });
//        btnMessage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: when chat one to one ready, use navigationInterfaces to navigate to chat
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", selectedUser.getPhoneNumber(), null)));
//            }
//        });
//        if (selectedUser.getSosRequest() != null && !selectedUser.getSosRequest().isResolved()){
//            btnShowSos.setVisibility(View.VISIBLE);
//        } else {
//            btnShowSos.setVisibility(View.GONE);
//        }
//
//        btnShowSos.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SosRequestViewDialogFragment.newInstance(mapBackgroundInterfaces, selectedUser.getId()).show(getChildFragmentManager(), "sos viewer");
//            }
//        });
    }

    public void setMapBackgroundInterfaces(MapPresenter.CallableMask mapBackgroundInterfaces) {
        this.mapBackgroundInterfaces = mapBackgroundInterfaces;
    }

    @Override
    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
    }

    public BottomMembersPresenterImpl getBottomMembersPresenter() {
        return bottomMembersPresenter;
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
    public void setUpNoString(int i, int size) {
        tvCount.setText(String.format("%d/%d", i, size));
    }

    @Override
    public void onNoActiveTrip() {

    }

    @Override
    public void onInTrip() {

    }

    @Override
    public void scrollToPosition(int position) {
        if (isResumed()) {
            rv.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onTargetUser(User user) {
        mapBackgroundInterfaces.cleanUpTempMarkerAndRoute();
        mapBackgroundInterfaces.target(user);
    }
}
