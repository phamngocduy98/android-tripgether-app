package cf.bautroixa.maptest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.firestore.Visit;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.Formater;
import cf.bautroixa.maptest.utils.LatLngDistance;
import cf.bautroixa.maptest.utils.RecyclerViewOnScrollListener;
import cf.bautroixa.maptest.utils.UrlParser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripOverviewFragment extends Fragment {
    private static final String TAG = "TripOverviewFragment";

    private FirebaseFirestore db;
    private SharedPreferences sharedPref;
    DatasManager.OnItemInsertedListener<Checkpoint> onCheckpointInsertedListener;
    private OnDrawRouteButtonClickedListener onDrawRouteButtonClickedListener = null;
    private OnActiveCheckpointChanged onCheckpointChanged = null;
    DatasManager.OnItemChangedListener<Checkpoint> onCheckpointChangedListener;
    private MainAppManager manager;
    DatasManager.OnItemRemovedListener<Checkpoint> onCheckpointRemovedListener;

    private ArrayList<Checkpoint> checkpoints;
    private int activePosition = 0;

    private Button btnCreateTrip, btnJoinTrip;
    private View noTripLayout;

    TextView tvLocation, tvTime;
    Button btnRoute;
    RecyclerView rv;
    Adapter adapter;
    SnapHelper snapHelper;
    private Data.OnNewValueListener<User> userOnNewValue;

    public TripOverviewFragment() {
        checkpoints = new ArrayList<>();
    }

    public void setOnDrawRouteButtonClickedListener(OnDrawRouteButtonClickedListener mListener) {
        this.onDrawRouteButtonClickedListener = mListener;
    }

    public void setOnCheckpointChanged(OnActiveCheckpointChanged onCheckpointChanged) {
        this.onCheckpointChanged = onCheckpointChanged;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        sharedPref = getContext().getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        manager = MainAppManager.getInstance();

        checkpoints = manager.getCheckpoints();
        adapter = new Adapter();

        userOnNewValue = new Data.OnNewValueListener<User>() {
            @Override
            public void onNewData(User user) {
                noTripLayout.setVisibility(user.getActiveTrip() == null?View.VISIBLE:View.GONE);
            }
        };
        onCheckpointInsertedListener = new DatasManager.OnItemInsertedListener<Checkpoint>() {
            @Override
            public void onItemInserted(int position, Checkpoint data) {
                adapter.notifyItemInserted(position);
                Log.d(TAG, "insert"+position);
            }
        };
        onCheckpointChangedListener = new DatasManager.OnItemChangedListener<Checkpoint>() {
            @Override
            public void onItemChanged(int position, Checkpoint data) {
                Log.d(TAG, "changed"+position);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (manager.isLoggedIn()) {
            userOnNewValue.onNewData(manager.getCurrentUser());
        }
        manager.getCurrentUser().addOnNewValueListener(userOnNewValue);
        manager.getCheckpointsManager().addOnItemChangedListener(onCheckpointChangedListener)
                .addOnItemInsertedListener(onCheckpointInsertedListener)
                .addOnItemRemovedListener(onCheckpointRemovedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getCurrentUser().removeOnNewValueListener(userOnNewValue);
        manager.getCheckpointsManager().removeOnItemChangedListener(onCheckpointChangedListener)
                .removeOnItemInsertedListener(onCheckpointInsertedListener)
                .removeOnItemRemovedListener(onCheckpointRemovedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_trip_overview, container, false);

        // no trip
        noTripLayout = v.findViewById(R.id.fragment_trip_overview_no_trip);
        btnCreateTrip = v.findViewById(R.id.btn_create_trip_dialog_no_trip);
        btnCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateTripActivity.class);
                startActivity(intent);
            }
        });
        btnJoinTrip = v.findViewById(R.id.btn_join_trip_dialog_no_trip);
        btnJoinTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRScanDialogFragment qrDialog = new QRScanDialogFragment(new QRScanDialogFragment.OnQrResultListener() {
                    @Override
                    public void onResult(String result) {
                        if (getContext() != null) {
                            String tripCode = UrlParser.parseTripCode(getContext(), result);
                            DocumentReference tripRef = db.collection(Collections.TRIPS).document(tripCode);
                            manager.sendJoinTrip(null, tripRef, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    }
                });
                qrDialog.show(getChildFragmentManager(), "qr scanner");
            }
        });

        // joined
        rv = v.findViewById(R.id.rv_checkpoints_frag_trip_overview);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        rv.addOnScrollListener(new RecyclerViewOnScrollListener(RecyclerViewOnScrollListener.ScrollMode.SCROLL_IDLE, snapHelper, new RecyclerViewOnScrollListener.OnNewPosition() {
            @Override
            public void onNewPosition(int position) {
                Log.d(TAG, "new pos = " + position+ "/" +checkpoints.size() + " " + checkpoints.get(position).getId());
                adapter.notifyItemChanged(position, Payload.MAYBE_NEED_BTN_UPDATE);
                onCheckpointChanged.onCheckpointChanged(checkpoints.get(position).getId());
            }
        }));

        if (manager.getCurrentTripRef() == null) {
            noTripLayout.setVisibility(View.VISIBLE);
        } else {
            noTripLayout.setVisibility(View.GONE);
        }
        return v;
    }

    void selectCheckpoint(int position) {
        if (position >= 0 && position < checkpoints.size()) {
            rv.smoothScrollToPosition(position);
        }
    }

    public void selectCheckpoint(String checkpointId) {
        onCheckpointChanged.onCheckpointChanged(checkpointId);
        for (int i = 0; i < checkpoints.size(); i++) {
            if (checkpointId.equals(checkpoints.get(i).getId())) {
                rv.smoothScrollToPosition(i);
                adapter.notifyItemChanged(i, Payload.MAYBE_NEED_BTN_UPDATE);
                return;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onDrawRouteButtonClickedListener = null;
    }

    enum ActionButton {
        BTN_ROLL_UP, BTN_ROUTE;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvTime;
        Button btnRoute, btnStart, btnRollUp, btnShowVisitors;
        ActionButton activeBtn;
        DatasManager.OnItemInsertedListener<Visit> onItemInsertedListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint_frag_trip_overview);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint_frag_trip_overview);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint_frag_trip_overview);
            btnRoute = itemView.findViewById(R.id.btn_draw_route_item_checkpoint_frag_trip_overview);
            btnRollUp = itemView.findViewById(R.id.btn_roll_up_item_checkpoint_frag_trip_overview);
            btnStart = itemView.findViewById(R.id.btn_start_direction_item_checkpoint_frag_trip_overview);
            btnShowVisitors = itemView.findViewById(R.id.btn_show_visitors_frag_trip_overview);
        }

        void bind(final Checkpoint checkpoint) {
            tvName.setText(checkpoint.getName());
            tvLocation.setText(checkpoint.getLocation());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));
            btnShowVisitors.setText(String.format("%d người có mặt", checkpoint.getVisitsManager().getData().size()));

            onItemInsertedListener = new DatasManager.OnItemInsertedListener<Visit>() {
                @Override
                public void onItemInserted(int position, Visit data) {
                    btnShowVisitors.setText(String.format("%d người có mặt", position + 1));
                }
            };

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
                                    for (DirectionsRoute route : response.body().routes()) {
                                        List<Point> coords = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6).coordinates();
                                        ArrayList<LatLng> latLngs = new ArrayList<>();
                                        latLngs.add(new LatLng(from.getLatitude(), from.getLongitude()));
                                        for (Point coord : coords) {
                                            latLngs.add(new LatLng(coord.latitude(), coord.longitude()));
                                        }
                                        latLngs.add(new LatLng(to.getLatitude(), to.getLongitude()));
                                        btnRoute.setText(String.format("%s/%s", Formater.formatDistance(route.distance()), Formater.formatTime(route.duration())));
                                        onDrawRouteButtonClickedListener.onClick(latLngs);
                                    }
                                }

                                @Override
                                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                                    Log.d(TAG, "get route failed");
                                }
                            });
                }
            });
            btnRollUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "diem danh");
                    checkpoint.getVisitsManager().addVisit(manager.getCurrentUser()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "diem danh thanh cong!");
                            }
                        }
                    });
                }
            });
            btnShowVisitors.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "show visitors");
                }
            });
            updateBtn(checkpoint, LatLngDistance.measureDistance(manager.getCurrentUser().getLatLng(), checkpoint.getLatLng()) < 50 ? ActionButton.BTN_ROLL_UP : ActionButton.BTN_ROUTE);
        }

        void updateBtn(final Checkpoint checkpoint, ActionButton activeBtn) {
            if (activeBtn == ActionButton.BTN_ROLL_UP) {
                btnRoute.setVisibility(View.GONE);
                btnStart.setVisibility(View.GONE);
                btnRollUp.setVisibility(View.VISIBLE);
                btnShowVisitors.setVisibility(View.VISIBLE);
                checkpoint.getVisitsManager().addOnItemInsertedListener(onItemInsertedListener);

            } else {
                btnRoute.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                btnRollUp.setVisibility(View.GONE);
                btnShowVisitors.setVisibility(View.GONE);
                checkpoint.getVisitsManager().removeOnItemInsertedListener(onItemInsertedListener);
            }
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_trip_overview_item_checkpoint, parent, false);
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

    public interface OnDrawRouteButtonClickedListener {
        void onClick(List<LatLng> latLng);
    }

    public interface OnActiveCheckpointChanged {
        void onCheckpointChanged(String checkpointId);
    }

    interface Payload {
        int MAYBE_NEED_BTN_UPDATE = 1;
    }
}
