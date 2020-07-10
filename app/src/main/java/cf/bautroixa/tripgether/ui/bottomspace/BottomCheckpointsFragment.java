package cf.bautroixa.tripgether.ui.bottomspace;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.MapBackgroundControllable;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomCheckpointsPresenter;
import cf.bautroixa.tripgether.presenter.bottomspace.MapPresenter;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomCheckpointsPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.BottomCheckpointsAdapter;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.tripgether.utils.ui_utils.RecyclerViewOnScrollListener;

public class BottomCheckpointsFragment extends Fragment implements BottomCheckpointsPresenter.View, NavigationInterfaceOwner, MapBackgroundControllable {
    private static final String TAG = "TripOverviewFragment";
    BottomCheckpointsPresenterImpl bottomCheckpointsPresenter;
    private int activePos;
    private NavigationInterface navigationInterface = null;
    private MapPresenter.CallableMask mapBackgroundInterfaces = null;
    private ConstraintLayout root;
    private TextView tvTopTimeLine;
    private RecyclerView rv;
    private BottomCheckpointsAdapter adapter;

    public BottomCheckpointsFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        bottomCheckpointsPresenter = new BottomCheckpointsPresenterImpl(requireContext(), this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_checkpoints, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view.findViewById(R.id.root_frag_bot_checkpoints);
        tvTopTimeLine = view.findViewById(R.id.tv_count_frag_trip_overview);
        rv = view.findViewById(R.id.rv_checkpoints_frag_trip_overview);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            activePos = savedInstanceState.getInt(SavedStateKeys.ACTIVE_POSITION, 0);
            rv.smoothScrollToPosition(activePos);
        }
        bottomCheckpointsPresenter.initAdapter(this, navigationInterface);
    }

    @Override
    public void setupAdapter(BottomCheckpointsAdapter adapter) {
        rv.setAdapter(adapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        rv.addOnScrollListener(new RecyclerViewOnScrollListener(RecyclerViewOnScrollListener.ScrollMode.SCROLL_IDLE, snapHelper, new RecyclerViewOnScrollListener.OnNewPosition() {
            @Override
            public void onNewPosition(int position) {
                activePos = position;
                bottomCheckpointsPresenter.onScrollNewPosition(position);
            }
        }));
    }

    @Override
    public void setUpTimeLineString(Checkpoint currentCheckpoint, @Nullable Checkpoint nextCheckpoint) {
        if (currentCheckpoint == null) {
            tvTopTimeLine.setVisibility(View.GONE);
            return;
        }
        tvTopTimeLine.setVisibility(View.VISIBLE);
        if (nextCheckpoint != null) {
            tvTopTimeLine.setText(Html.fromHtml("<b>" +
                    DateFormatter.format(currentCheckpoint.getTime()) + "</b> -"
                    + DateFormatter.format(nextCheckpoint.getTime())
            ));
        } else {
            tvTopTimeLine.setText(Html.fromHtml("<b>" +
                    DateFormatter.format(currentCheckpoint.getTime()) + "</b>"));
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SavedStateKeys.ACTIVE_POSITION, activePos);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationInterface = null;
        mapBackgroundInterfaces = null;
    }

    @Override
    public void scrollToPosition(int position) {
        activePos = position;
        if (isResumed() && rv != null) {
            rv.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onTargetCheckpoint(Checkpoint checkpoint) {
        if (mapBackgroundInterfaces != null) {
            mapBackgroundInterfaces.target(checkpoint);
        }
    }

    public BottomCheckpointsPresenterImpl getBottomCheckpointsPresenter() {
        return bottomCheckpointsPresenter;
    }

    public void setMapBackgroundInterface(MapPresenter.CallableMask mapBackgroundInterface) {
        this.mapBackgroundInterfaces = mapBackgroundInterface;
    }

    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }

    public interface SavedStateKeys {
        String ACTIVE_POSITION = "activePos";
    }
}
