package cf.bautroixa.maptest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.firestore.Checkpoint;

public class CreateTripActivity extends AppCompatActivity {

    private static final String TAG = "CreateTripActivity";
    RecyclerView rvCheckpoints;
    Button btnCancel, btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        btnCancel = findViewById(R.id.btn_cancel_trip_create);
        btnOk = findViewById(R.id.btn_ok_trip_create);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateTripActivity.this, CreateTripInvitationActivity.class);
                startActivity(intent);
            }
        });

        rvCheckpoints = findViewById(R.id.rv_checkpoint_trip_create);
        rvCheckpoints.setLayoutManager(new LinearLayoutManager(this));
        rvCheckpoints.setAdapter(new CheckPointAdapter());
    }

    public class CheckPointViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvLocation;
        View view;
        public CheckPointViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
        }
        public void bind(Checkpoint checkpoint){
            tvName.setText(checkpoint.getName());
            tvTime.setText(checkpoint.getTime().toString());
            tvLocation.setText(checkpoint.getLocation());
        }
    }

    public class AddCheckPointViewHolder extends RecyclerView.ViewHolder {
        View view;
        public AddCheckPointViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
    }

    public class CheckPointAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int TYPE_CHECKPOINT_ITEM = 0;
        private final int TYPE_ADD_CHECKPOINT_BTN = 1;
        ArrayList<Checkpoint> checkpoints;

        public CheckPointAdapter() {
            checkpoints = new ArrayList<>();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_ADD_CHECKPOINT_BTN){
                return new AddCheckPointViewHolder(getLayoutInflater().inflate(R.layout.item_checkpoint_btn_add, parent, false));
            } else {
                return new CheckPointViewHolder(getLayoutInflater().inflate(R.layout.item_checkpoint_activity_add_trip, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_CHECKPOINT_ITEM) {
                Checkpoint checkpoint = this.checkpoints.get(position);
                if (holder instanceof CheckPointViewHolder) {
                    ((CheckPointViewHolder) holder).bind(checkpoint);
                }
            } else {
                ((AddCheckPointViewHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment dialogFragment = new CreateTripCheckpointDialogFragment(new CreateTripCheckpointDialogFragment.OnCheckpointSetListener() {
                            @Override
                            public void onCheckpointSet(Checkpoint checkpoint) {
                                checkpoints.add(checkpoint);
                                notifyItemInserted(checkpoints.size()-1);
                                Log.d(TAG, "insert"+(checkpoints.size()-1));
                            }
                        });
                        dialogFragment.show(getSupportFragmentManager(),"create trip checkpoint fragment");
                        Log.d(TAG, "show dialog");
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            Log.d(TAG, "getType"+position+"= "+this.checkpoints.size());
            if (position == this.checkpoints.size()){
                return TYPE_ADD_CHECKPOINT_BTN;
            }
            return TYPE_CHECKPOINT_ITEM;
        }

        @Override
        public int getItemCount() {
            return this.checkpoints.size()+1;
        }
    }
}
