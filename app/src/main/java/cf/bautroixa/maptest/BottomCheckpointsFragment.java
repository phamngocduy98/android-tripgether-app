package cf.bautroixa.maptest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.Visit;
import cf.bautroixa.maptest.firestore.VisitsManager;
import cf.bautroixa.maptest.interfaces.MapBackgroundControllable;
import cf.bautroixa.maptest.interfaces.MapBackgroundInterfaces;
import cf.bautroixa.maptest.interfaces.Navigable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.Formater;
import cf.bautroixa.maptest.utils.LatLngDistance;
import cf.bautroixa.maptest.utils.RecyclerViewOnScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomCheckpointsFragment extends Fragment implements Navigable, MapBackgroundControllable {
    private static final String TAG = "TripOverviewFragment";

    private MainAppManager manager;
    private ArrayList<Checkpoint> checkpoints;
    private int activePos = 0;

    private NavigationInterfaces navigationInterfaces = null;
    private MapBackgroundInterfaces mapBackgroundInterfaces = null;
    private DatasManager.OnDatasChangedListener<Checkpoint> onCheckpointsChangedListener;
    private DatasManager.OnDatasChangedListener<Visit> onVisitsChangedListener;

    private TextView tvTopTimeLine;
    private RecyclerView rv;
    private Adapter adapter;

    public BottomCheckpointsFragment() {
        manager = MainAppManager.getInstance();
        checkpoints = manager.getCheckpoints();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        onCheckpointsChangedListener = new DatasManager.OnDatasChangedListener<Checkpoint>() {
            @Override
            public void onItemInserted(int position, Checkpoint data) {
                adapter.notifyItemInserted(position);
            }

            @Override
            public void onItemChanged(int position, Checkpoint data) {
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onItemRemoved(int position, Checkpoint data) {
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onDataSetChanged(ArrayList<Checkpoint> datas) {
                adapter.notifyDataSetChanged();
            }
        };

        onVisitsChangedListener = new DatasManager.OnDatasChangedListener<Visit>() {
            @Override
            public void onItemInserted(int position, Visit data) {
                Checkpoint activeCheckpoint = manager.getActiveCheckpoint();
                if (activeCheckpoint != null) {
                    adapter.notifyItemChanged(manager.getCheckpointsManager().indexOf(activeCheckpoint), Payload.UPDATE_NOTI_COUNT);
                }
            }

            @Override
            public void onItemChanged(int position, Visit data) {

            }

            @Override
            public void onItemRemoved(int position, Visit data) {

            }

            @Override
            public void onDataSetChanged(ArrayList<Visit> datas) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_checkpoints, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTopTimeLine = view.findViewById(R.id.tv_count_frag_trip_overview);
        setTimeLineString(0);

        adapter = new Adapter();
        rv = view.findViewById(R.id.rv_checkpoints_frag_trip_overview);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        rv.addOnScrollListener(new RecyclerViewOnScrollListener(RecyclerViewOnScrollListener.ScrollMode.SCROLL_IDLE, snapHelper, new RecyclerViewOnScrollListener.OnNewPosition() {
            @Override
            public void onNewPosition(int position) {
                Log.d(TAG, "new pos = " + position + "/" + checkpoints.size() + " " + checkpoints.get(position).getId());
                setTimeLineString(position);
                adapter.notifyItemChanged(position, Payload.MAYBE_NEED_BTN_UPDATE);
                mapBackgroundInterfaces.cleanUpTempMarkerAndRoute();
                mapBackgroundInterfaces.target(checkpoints.get(position));
            }
        }));

        if (manager.getCurrentTripRef() == null) {
            rv.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        smoothScrollToPosition(activePos);
        manager.getCheckpointsManager().addOnDatasChangedListener(onCheckpointsChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getCheckpointsManager().removeOnDatasChangedListener(onCheckpointsChangedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationInterfaces = null;
        mapBackgroundInterfaces = null;
        onCheckpointsChangedListener = null;
        onVisitsChangedListener = null;
    }

    public void smoothScrollToPosition(int position) {
        if (activePos < checkpoints.size()) {
            rv.smoothScrollToPosition(position);
        } else {
            Log.e(TAG, "scroll to index out of bounds");
        }
    }

    private void setTimeLineString(int position) {
        if (checkpoints.size() == 0) {
            tvTopTimeLine.setVisibility(View.GONE);
            return;
        }
        tvTopTimeLine.setVisibility(View.VISIBLE);
        if (position == 0) {
            if (checkpoints.size() == 1) {
                tvTopTimeLine.setText(Html.fromHtml("<b>" + DateFormatter.format(checkpoints.get(position).getTime()) + "</b>"));
            } else {
                tvTopTimeLine.setText(Html.fromHtml(
                        "<b>" + DateFormatter.format(checkpoints.get(position).getTime()) + "</b> - "
                                + DateFormatter.format(checkpoints.get(position + 1).getTime())
                ));
            }
        } else if (position == checkpoints.size() - 1) {
            tvTopTimeLine.setText(Html.fromHtml(
                    DateFormatter.format(checkpoints.get(position - 1).getTime()) + " - <b>"
                            + DateFormatter.format(checkpoints.get(position).getTime()) + "</b>"
            ));
        } else {
            tvTopTimeLine.setText(Html.fromHtml(
                    DateFormatter.format(checkpoints.get(0).getTime()) + " - <b>"
                            + DateFormatter.format(checkpoints.get(position).getTime()) + "</b> - "
                            + DateFormatter.format(checkpoints.get(position + 1).getTime())));
        }
    }

    public void selectCheckpoint(String checkpointId) {
        for (int i = 0; i < checkpoints.size(); i++) {
            if (checkpointId.equals(checkpoints.get(i).getId())) {
                activePos = i;
                if (isResumed()) smoothScrollToPosition(activePos);
                return;
            }
        }
    }

    public void setMapBackgroundInterfaces(MapBackgroundInterfaces mapBackgroundInterfaces) {
        this.mapBackgroundInterfaces = mapBackgroundInterfaces;
    }

    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
    }


    enum ActionButton {
        BTN_ROLL_UP, BTN_ROUTE
    }

    interface Payload {
        int MAYBE_NEED_BTN_UPDATE = 1;
        int UPDATE_NOTI_COUNT = 2;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvTime, tvDate;
        Button btnRoute, btnStart, btnCheckIn;
        ActionButton activeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint_frag_trip_overview);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint_frag_trip_overview);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint_frag_trip_overview);
            tvDate = itemView.findViewById(R.id.tv_date_item_checkpoint_frag_trip_overview);
            btnRoute = itemView.findViewById(R.id.btn_draw_route_item_checkpoint_frag_trip_overview);
            btnCheckIn = itemView.findViewById(R.id.btn_check_in_item_checkpoint_frag_trip_overview);
            btnStart = itemView.findViewById(R.id.btn_start_direction_item_checkpoint_frag_trip_overview);
        }

        void setRollUpButton(final Checkpoint checkpoint) {
            VisitsManager visitsManager = checkpoint.getVisitsManager();
            Visit visit = visitsManager.get(manager.getCurrentUser().getId());
            if (visit != null) {
                // đã điểm danh
                btnCheckIn.setText(String.format("Xem danh sách (%d/%d)", visitsManager.getData().size(), manager.getMembers().size()));
                btnCheckIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigationInterfaces.navigate(MainActivity.TAB_MAP, TabMapFragment.STATE_FRIEND_LIST_EXPANDED, checkpoint);
                    }
                });
            } else {
                btnCheckIn.setText(String.format("Điểm danh (%d/%d)", visitsManager.getData().size(), manager.getMembers().size()));
                btnCheckIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "diem danh");
                        ViewAnim.toggleLoading(getContext(), btnCheckIn, true, "");
                        checkpoint.getVisitsManager().addVisit(manager.getCurrentUser()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "diem danh thanh cong!");
                                    ViewAnim.toggleLoading(getContext(), btnCheckIn, false, "Đã điểm danh!");
                                    setRollUpButton(checkpoint);
                                }
                            }
                        });
                    }
                });
            }
        }

        void bind(final Checkpoint checkpoint) {
            tvName.setText(checkpoint.getName());
            tvLocation.setText(checkpoint.getLocation());
            tvTime.setText(DateFormatter.formatTime(checkpoint.getTime()));
            tvDate.setText(DateFormatter.formatDate(checkpoint.getTime()));
            setRollUpButton(checkpoint);
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + checkpoint.getLatLng().latitude + "," + checkpoint.getLatLng().longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (getContext() != null && mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            });
            btnRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final GeoPoint from = manager.getCurrentUser().getCurrentCoord();
                    final GeoPoint to = checkpoint.getCoordinate();
                    ViewAnim.toggleLoading(getContext(), btnRoute, true, getString(R.string.btn_route));
                    NavigationRoute.builder(getContext())
                            .accessToken(getString(R.string.config_mapbox_map_api_key))
                            .origin(Point.fromLngLat(from.getLongitude(), from.getLatitude()))
                            .destination(Point.fromLngLat(to.getLongitude(), to.getLatitude()))
                            .build()
                            .getRoute(new Callback<DirectionsResponse>() {
                                @Override
                                public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                                    ViewAnim.toggleLoading(getContext(), btnRoute, false, getString(R.string.btn_route));
                                    if (response.isSuccessful() && response.body() != null)
                                        for (DirectionsRoute route : response.body().routes()) {
                                            String routeGeometry = route.geometry();
                                            Double routeDistance = route.distance();
                                            Double routeDuration = route.duration();
                                            if (routeGeometry == null || routeDistance == null || routeDuration == null)
                                                continue;
                                            List<Point> coords = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6).coordinates();
                                            ArrayList<LatLng> latLngs = new ArrayList<>();
                                            latLngs.add(new LatLng(from.getLatitude(), from.getLongitude()));
                                            for (Point coord : coords) {
                                                latLngs.add(new LatLng(coord.latitude(), coord.longitude()));
                                            }
                                            latLngs.add(new LatLng(to.getLatitude(), to.getLongitude()));
                                            btnRoute.setText(String.format("%s/%s", Formater.formatDistance(routeDistance), Formater.formatTime(routeDuration)));
                                            mapBackgroundInterfaces.drawLine(latLngs);
                                        }
                                }

                                @Override
                                public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable t) {
                                    Log.d(TAG, "get route failed");
                                    ViewAnim.toggleLoading(getContext(), btnRoute, false, getString(R.string.btn_route));
                                }
                            });
                }
            });
            updateBtn(checkpoint, LatLngDistance.measureDistance(manager.getCurrentUser().getLatLng(), checkpoint.getLatLng()) < 50 ? ActionButton.BTN_ROLL_UP : ActionButton.BTN_ROUTE);
        }

        void updateBtn(final Checkpoint checkpoint, ActionButton activeBtn) {
            if (activeBtn == ActionButton.BTN_ROLL_UP) {
                btnRoute.setVisibility(View.GONE);
                btnStart.setVisibility(View.GONE);
                btnCheckIn.setVisibility(View.VISIBLE);
                checkpoint.getVisitsManager().addOnDatasChangedListener(onVisitsChangedListener);

            } else {
                btnRoute.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                btnCheckIn.setVisibility(View.GONE);
                checkpoint.getVisitsManager().removeOnDatasChangedListener(onVisitsChangedListener);
            }
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bottom_checkpoints_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(checkpoints.get(position));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.size() > 0) {
                Checkpoint checkpoint = checkpoints.get(position);
                if ((int) payloads.get(0) == Payload.MAYBE_NEED_BTN_UPDATE) {
                    if (LatLngDistance.measureDistance(manager.getCurrentUser().getLatLng(), checkpoint.getLatLng()) < 50) {
                        holder.updateBtn(checkpoint, ActionButton.BTN_ROLL_UP);
                    } else {
                        holder.updateBtn(checkpoint, ActionButton.BTN_ROUTE);
                    }
                } else {
                    holder.setRollUpButton(checkpoint);
                }
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }
        }

        @Override
        public int getItemCount() {
            return checkpoints.size();
        }
    }
}
