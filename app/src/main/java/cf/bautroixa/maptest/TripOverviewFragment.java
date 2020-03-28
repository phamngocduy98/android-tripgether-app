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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.UrlParser;

public class TripOverviewFragment extends Fragment {
    private static final String TAG = "TripOverviewFragment";

    private FirebaseFirestore db;
    private SharedPreferences sharedPref;
    private OnDrawRouteButtonClickedListener onDrawRouteButtonClickedListener = null;
    private OnCheckpointChanged onCheckpointChanged = null;

    private HashMap<String, Checkpoint> checkpoints;
    private DocumentReference currentUserRef, currentTripRef;
    private int activePosition = 0;

    private Button btnCreateTrip, btnJoinTrip;
    private View noTripLayout;

    TextView tvLocation, tvTime;
    Button btnRoute;
    RecyclerView rv;
    Adapter adapter;
    SnapHelper snapHelper;

    public TripOverviewFragment() {
        // Required empty public constructor
        checkpoints = new HashMap<>();
    }

    public void setOnDrawRouteButtonClickedListener(OnDrawRouteButtonClickedListener mListener) {
        this.onDrawRouteButtonClickedListener = mListener;
    }

    public void setOnCheckpointChanged(OnCheckpointChanged onCheckpointChanged) {
        this.onCheckpointChanged = onCheckpointChanged;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getContext().getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        currentUserRef = db.collection(Collections.USERS).document(sharedPref.getString("userName", "notLoggedIn"));
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    User user = documentSnapshot.toObject(User.class);
                    currentTripRef = user.getActiveTrip();
                    if (currentTripRef == null) {
                        noTripLayout.setVisibility(View.VISIBLE);
                    } else {
                        noTripLayout.setVisibility(View.INVISIBLE);
                        currentTripRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Trip trip = task.getResult().toObject(Trip.class);
                                }
                            }
                        });

                        db.collection(Collections.checkpoints(currentTripRef.getId())).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Checkpoint checkpoint = document.toObject(Checkpoint.class);
                                        checkpoints.put(document.getId(), checkpoint);
                                    }
                                    adapter.changeDataSet(checkpoints);
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
                        // listen checkpoint added/changed
                        db.collection(Collections.checkpoints(currentTripRef.getId())).addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen failed.", e);
                                    return;
                                }
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    Checkpoint checkpoint = documentSnapshot.toObject(Checkpoint.class);
                                    String id = documentSnapshot.getId();
                                    checkpoints.put(id, checkpoint);
                                }
                                adapter.changeDataSet(checkpoints);
                            }
                        });
                    }
                }
            }
        });
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
                            currentTripRef = db.collection(Collections.TRIPS).document(tripCode);
                            currentUserRef.update(User.ACTIVE_TRIP, currentTripRef);
                            currentTripRef.update(Trip.MEMBERS, FieldValue.arrayUnion(currentUserRef));
                        }
                    }
                });
                qrDialog.show(getChildFragmentManager(), "qr scanner");
            }
        });

        // joined
        rv = v.findViewById(R.id.rv_checkpoints_frag_trip_overview);
        adapter = new Adapter(checkpoints);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        return v;
    }

    int getSnapPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) return RecyclerView.NO_POSITION;
        View snapView = snapHelper.findSnapView(layoutManager);
        if (snapView == null) return RecyclerView.NO_POSITION;
        return layoutManager.getPosition(snapView);
    }

    void selectCheckpoint(int position) {
        if (position >= 0 && position < checkpoints.size()) {
            rv.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onDrawRouteButtonClickedListener = null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvTime;
        Button btnRoute, btnStart, btnRollUp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint_frag_trip_overview);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint_frag_trip_overview);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint_frag_trip_overview);
            btnRoute = itemView.findViewById(R.id.btn_draw_route_item_checkpoint_frag_trip_overview);
            btnRollUp = itemView.findViewById(R.id.btn_roll_up_item_checkpoint_frag_trip_overview);
            btnStart = itemView.findViewById(R.id.btn_start_direction_item_checkpoint_frag_trip_overview);
        }

        void bind(final Checkpoint checkpoint) {
            tvName.setText(checkpoint.getName());
            tvLocation.setText(checkpoint.getLocation());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));
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
            if (false) {
//            if (LatLngDistance.measureDistance(currentUserStatus.status.getLatLng(), checkpoint.getLatLng()) < 50){
                btnRoute.setVisibility(View.GONE);
                btnRollUp.setVisibility(View.VISIBLE);
                btnRollUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Diem danh ...");
                    }
                });
            } else {
                btnRoute.setVisibility(View.VISIBLE);
                btnRollUp.setVisibility(View.GONE);
                btnRoute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDrawRouteButtonClickedListener.onClick(checkpoint.getLatLng());
                    }
                });
            }
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        ArrayList<Checkpoint> checkpoints;

        public Adapter(HashMap<String, Checkpoint> checkpointHashMap) {
            this.checkpoints = new ArrayList<>(checkpointHashMap.values());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_trip_overview_item_checkpoint, parent, false);
            return new ViewHolder(view);
        }

        void changeDataSet(HashMap<String, Checkpoint> checkpointHashMap) {
            checkpoints = new ArrayList<>(checkpointHashMap.values());
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(checkpoints.get(position));
        }

        @Override
        public int getItemCount() {
            return checkpoints.size();
        }
    }

    public interface OnDrawRouteButtonClickedListener {
        void onClick(LatLng latLng);
    }

    public interface OnCheckpointChanged {
        void onChanged(int newPosition);
    }
}
