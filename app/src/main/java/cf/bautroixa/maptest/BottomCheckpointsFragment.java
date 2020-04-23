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

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.Visit;
import cf.bautroixa.maptest.firestore.VisitsManager;
import cf.bautroixa.maptest.interfaces.HasOnGoToMainActivityState;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.interfaces.OnDrawRouteRequestWithPath;
import cf.bautroixa.maptest.interfaces.OnGoToMainActivityState;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.Formater;
import cf.bautroixa.maptest.utils.LatLngDistance;
import cf.bautroixa.maptest.utils.RecyclerViewOnScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomCheckpointsFragment extends Fragment implements HasOnGoToMainActivityState<Checkpoint> {
    private static final String TAG = "TripOverviewFragment";

    private MainAppManager manager;

    TextView tvLocation, tvTime, tvTimeLine;
    private DatasManager.OnItemInsertedListener<Checkpoint> onCheckpointInsertedListener;
    private DatasManager.OnItemChangedListener<Checkpoint> onCheckpointChangedListener;
    private DatasManager.OnItemRemovedListener<Checkpoint> onCheckpointRemovedListener;
    private OnDrawRouteRequestWithPath onDrawRouteRequest = null;
    private OnDataItemSelected<Checkpoint> onCheckpointItemSelected = null;

    private ArrayList<Checkpoint> checkpoints;
    private OnGoToMainActivityState<Checkpoint> onGoToMainActivityState = null;

    private Button btnCreateTrip, btnJoinTrip;
    private int activePos = 0;
    Button btnRoute;
    RecyclerView rv;
    Adapter adapter;
    SnapHelper snapHelper;


    public BottomCheckpointsFragment() {
        manager = MainAppManager.getInstance();
        checkpoints = manager.getCheckpoints();

        adapter = new Adapter();
        onCheckpointInsertedListener = new DatasManager.OnItemInsertedListener<Checkpoint>() {
            @Override
            public void onItemInserted(int position, Checkpoint data) {
                adapter.notifyItemInserted(position);
                Log.d(TAG, "insert" + position);
            }
        };
        onCheckpointChangedListener = new DatasManager.OnItemChangedListener<Checkpoint>() {
            @Override
            public void onItemChanged(int position, Checkpoint data) {
                Log.d(TAG, "changed" + position);
                adapter.notifyItemChanged(position);
            }
        };
        onCheckpointRemovedListener = new DatasManager.OnItemRemovedListener<Checkpoint>() {
            @Override
            public void onItemRemoved(int position, Checkpoint checkpoint) {
                adapter.notifyItemRemoved(position);
            }
        };
    }

    public void setOnDrawRouteRequestWithPathListener(OnDrawRouteRequestWithPath mListener) {
        this.onDrawRouteRequest = mListener;
    }

    public void setOnCheckpointChanged(OnDataItemSelected<Checkpoint> onCheckpointItemSelected) {
        this.onCheckpointItemSelected = onCheckpointItemSelected;
    }

    public void setOnGoToMainActivityState(OnGoToMainActivityState<Checkpoint> onGoToMainActivityState) {
        this.onGoToMainActivityState = onGoToMainActivityState;
    }

    private void setTimeLineString(int position) {
        if (checkpoints.size() == 0) {
            tvTimeLine.setVisibility(View.GONE);
            return;
        }
        tvTimeLine.setVisibility(View.VISIBLE);
        if (position == 0) {
            if (checkpoints.size() == 1) {
                tvTimeLine.setText(Html.fromHtml("<b>" + DateFormatter.format(checkpoints.get(position).getTime()) + "</b>"));
            } else {
                tvTimeLine.setText(Html.fromHtml(
                        "<b>" + DateFormatter.format(checkpoints.get(position).getTime()) + "</b> - "
                                + DateFormatter.format(checkpoints.get(position + 1).getTime())
                ));
            }
        } else if (position == checkpoints.size() - 1) {
            tvTimeLine.setText(Html.fromHtml(
                    DateFormatter.format(checkpoints.get(position - 1).getTime()) + " - <b>"
                            + DateFormatter.format(checkpoints.get(position).getTime()) + "</b>"
            ));
        } else {
            tvTimeLine.setText(Html.fromHtml(
                    DateFormatter.format(checkpoints.get(0).getTime()) + " - <b>"
                            + DateFormatter.format(checkpoints.get(position).getTime()) + "</b> - "
                            + DateFormatter.format(checkpoints.get(position + 1).getTime())));
        }
    }

    void selectCheckpoint(int position) {
        if (position >= 0 && position < checkpoints.size()) {
            rv.smoothScrollToPosition(position);
        }
    }

    public void selectCheckpoint(String checkpointId) {
        for (int i = 0; i < checkpoints.size(); i++) {
            if (checkpointId.equals(checkpoints.get(i).getId())) {
                activePos = i;
                if (isResumed()) {
                    scrollToSelectedCheckpoint();
                }
                return;
            }
        }
    }

    private void scrollToSelectedCheckpoint() {
        onCheckpointItemSelected.selectItem(checkpoints.get(activePos));
        adapter.notifyItemChanged(activePos, Payload.MAYBE_NEED_BTN_UPDATE);
        rv.smoothScrollToPosition(activePos);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        scrollToSelectedCheckpoint();
        manager.getCheckpointsManager().addOnItemChangedListener(onCheckpointChangedListener)
                .addOnItemInsertedListener(onCheckpointInsertedListener)
                .addOnItemRemovedListener(onCheckpointRemovedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getCheckpointsManager().removeOnItemChangedListener(onCheckpointChangedListener)
                .removeOnItemInsertedListener(onCheckpointInsertedListener)
                .removeOnItemRemovedListener(onCheckpointRemovedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_bottom_checkpoints, container, false);

        tvTimeLine = v.findViewById(R.id.tv_count_frag_trip_overview);

        // joined
        rv = v.findViewById(R.id.rv_checkpoints_frag_trip_overview);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rv);

        setTimeLineString(0);
        rv.addOnScrollListener(new RecyclerViewOnScrollListener(RecyclerViewOnScrollListener.ScrollMode.SCROLL_IDLE, snapHelper, new RecyclerViewOnScrollListener.OnNewPosition() {
            @Override
            public void onNewPosition(int position) {
                Log.d(TAG, "new pos = " + position + "/" + checkpoints.size() + " " + checkpoints.get(position).getId());
                setTimeLineString(position);
                adapter.notifyItemChanged(position, Payload.MAYBE_NEED_BTN_UPDATE);
                onCheckpointItemSelected.selectItem(checkpoints.get(position));
            }
        }));

        if (manager.getCurrentTripRef() == null) {
            rv.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.VISIBLE);
        }
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onDrawRouteRequest = null;
        onGoToMainActivityState = null;
        onCheckpointItemSelected = null;
    }

    enum ActionButton {
        BTN_ROLL_UP, BTN_ROUTE;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvTime, tvDate;
        Button btnRoute, btnStart, btnCheckIn;
        ActionButton activeBtn;

        // TODO: remove listener onPause
        DatasManager.OnItemInsertedListener<Visit> onItemInsertedListener;

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

        void setRollUpButton(final Button btnCheckIn, final Checkpoint checkpoint) {
            VisitsManager visitsManager = checkpoint.getVisitsManager();
            Visit visit = visitsManager.get(manager.getCurrentUser().getId());
            if (visit != null) {
                // đã điểm danh
                btnCheckIn.setText(String.format("Xem danh sách (%d/%d)", visitsManager.getData().size(), manager.getMembers().size()));
                btnCheckIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onGoToMainActivityState.newState(MainActivity.STATE_FRIEND_LIST_EXPANDED);
                    }
                });
            } else {
                btnCheckIn.setText(String.format("Điểm danh (%d/%d)", visitsManager.getData().size(), manager.getMembers().size()));
                btnCheckIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "diem danh");
                        ViewAnim.toggleLoading(btnCheckIn, true, "");
                        checkpoint.getVisitsManager().addVisit(manager.getCurrentUser()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "diem danh thanh cong!");
                                    ViewAnim.toggleLoading(btnCheckIn, false, "Đã điểm danh!");
                                    setRollUpButton(btnCheckIn, checkpoint);
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
            setRollUpButton(btnCheckIn, checkpoint);

            onItemInsertedListener = new DatasManager.OnItemInsertedListener<Visit>() {
                @Override
                public void onItemInserted(int position, Visit data) {
                    setRollUpButton(btnCheckIn, checkpoint);
                }
            };
            setRollUpButton(btnCheckIn, checkpoint);
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + checkpoint.getLatLng().latitude + "," + checkpoint.getLatLng().longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            });
            btnRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final GeoPoint from = manager.getCurrentUser().getCurrentCoord();
                    final GeoPoint to = checkpoint.getCoordinate();
                    NavigationRoute.builder(getContext())
                            .accessToken(getString(R.string.config_mapbox_map_api_key))
                            .origin(Point.fromLngLat(from.getLongitude(), from.getLatitude()))
                            .destination(Point.fromLngLat(to.getLongitude(), to.getLatitude()))
                            .build()
                            .getRoute(new Callback<DirectionsResponse>() {
                                @Override
                                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                                    if (response.isSuccessful() && response.body() != null)
                                        for (DirectionsRoute route : response.body().routes()) {
                                            List<Point> coords = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6).coordinates();
                                            ArrayList<LatLng> latLngs = new ArrayList<>();
                                            latLngs.add(new LatLng(from.getLatitude(), from.getLongitude()));
                                            for (Point coord : coords) {
                                                latLngs.add(new LatLng(coord.latitude(), coord.longitude()));
                                            }
                                            latLngs.add(new LatLng(to.getLatitude(), to.getLongitude()));
                                            btnRoute.setText(String.format("%s/%s", Formater.formatDistance(route.distance()), Formater.formatTime(route.duration())));
                                            onDrawRouteRequest.drawRoute(latLngs);
                                        }
                                }

                                @Override
                                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                                    Log.d(TAG, "get route failed");
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
                checkpoint.getVisitsManager().addOnItemInsertedListener(onItemInsertedListener);

            } else {
                btnRoute.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                btnCheckIn.setVisibility(View.GONE);
                checkpoint.getVisitsManager().removeOnItemInsertedListener(onItemInsertedListener);
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
                if (LatLngDistance.measureDistance(manager.getCurrentUser().getLatLng(), checkpoint.getLatLng()) < 50) {
                    holder.updateBtn(checkpoint, ActionButton.BTN_ROLL_UP);
                } else {
                    holder.updateBtn(checkpoint, ActionButton.BTN_ROUTE);
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

    interface Payload {
        int MAYBE_NEED_BTN_UPDATE = 1;
    }
}
