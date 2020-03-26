package cf.bautroixa.maptest;

import android.content.Context;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import cf.bautroixa.maptest.data.CurrentUserStatus;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;

public class TripOverviewFragment extends Fragment {
    private static final String TAG = "TripOverviewFragment";

    private FirebaseFirestore db;
    OnDrawRouteButtonClickedListener onDrawRouteButtonClickedListener = null;
    OnCheckpointChanged onCheckpointChanged = null;

    TextView tvLocation, tvTime;
    Button btnRoute;

    RecyclerView rv;
    SnapHelper snapHelper;

    HashMap<String, Checkpoint> checkpoints;

    CurrentUserStatus currentUserStatus;

    int activePosition = 0;

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
        if (getArguments() != null) {
        }
        if (currentUserStatus == null) currentUserStatus = CurrentUserStatus.getInstance(getContext());
        db = FirebaseFirestore.getInstance();
        db.collection(Collections.CHECKPOINTS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Checkpoint checkpoint = document.toObject(Checkpoint.class);
                        checkpoints.put(document.getId(), checkpoint);
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
        // listen checkpoint added/changed
        db.collection(Collections.CHECKPOINTS).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_trip_overview, container, false);
        rv = v.findViewById(R.id.rv_checkpoints_frag_trip_overview);
        rv.setAdapter(new Adapter());
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

    void selectCheckpoint(int position){
        if (position >= 0 && position < checkpoints.size()){
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
        void bind(final Checkpoint checkpoint){
//            tvName.setText("");
            tvLocation.setText(checkpoint.getLocation());
            tvTime.setText(checkpoint.getTime().toString());
            if (false){
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

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_frag_trip_overview, parent, false);
            return new ViewHolder(view);
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
