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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.FireStoreManager;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.LatLngDistance;
import cf.bautroixa.maptest.utils.RecyclerViewOnScrollListener;
import cf.bautroixa.maptest.utils.UrlParser;

public class TripOverviewFragment extends Fragment {
    private static final String TAG = "TripOverviewFragment";

    private FirebaseFirestore db;
    private SharedPreferences sharedPref;
    private FireStoreManager manager;
    private OnDrawRouteButtonClickedListener onDrawRouteButtonClickedListener = null;
    private OnActiveCheckpointChanged onCheckpointChanged = null;

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

    public void setOnCheckpointChanged(OnActiveCheckpointChanged onCheckpointChanged) {
        this.onCheckpointChanged = onCheckpointChanged;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getContext().getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        String userName = sharedPref.getString("userName", "notLoggedIn");
        manager = FireStoreManager.getInstance(userName);

        currentUserRef = manager.getCurrentUserRef();
        currentTripRef = manager.getCurrentTripRef();
        checkpoints = manager.getCheckpoints();
//        adapter.changeDataSet(checkpoints);

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
                    checkpoint.setId(documentSnapshot.getId());
                    String id = documentSnapshot.getId();
                    checkpoints.put(id, checkpoint);
                }
                adapter.changeDataSet(checkpoints);
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
        rv.addOnScrollListener(new RecyclerViewOnScrollListener(RecyclerViewOnScrollListener.ScrollMode.SCROLL_IDLE, snapHelper, new RecyclerViewOnScrollListener.OnNewPosition() {
            @Override
            public void onNewPosition(int position) {
                Log.d(TAG, "new pos = "+position + " "+ adapter.checkpoints.get(position).getId());
                onCheckpointChanged.onCheckpointChanged(adapter.checkpoints.get(position).getId());
            }
        }));

        if (currentTripRef == null) {
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
        Log.d(TAG, "select"+checkpoints.size());
        Iterator<Checkpoint> itr = checkpoints.values().iterator();
        int i=0;
        while (itr.hasNext()) {
            Checkpoint checkpoint = itr.next();
            Log.d(TAG, "compare"+checkpoint.getId());
            if (checkpoint.getId().equals(checkpointId)){
                Log.d(TAG, "scroll smooth = "+i);
                rv.smoothScrollToPosition(i);
                break;
            }
            i++;
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
//            if (false) {
            if (LatLngDistance.measureDistance(manager.getCurrentUser().getLatLng(), checkpoint.getLatLng()) < 50){
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

    public interface OnActiveCheckpointChanged {
        void onCheckpointChanged(String checkpointId);
    }
}
