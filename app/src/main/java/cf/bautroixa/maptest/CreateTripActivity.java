package cf.bautroixa.maptest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.KeyboardHelper;

public class CreateTripActivity extends AppCompatActivity {

    private static final String TAG = "CreateTripActivity";
    private FirebaseFirestore db;
    SharedPreferences sharedPref;
    String userName = "";
    ArrayList<View> screens;
    int activeScreen = 0;

    RecyclerView rvCheckpoints;
    ArrayList<Checkpoint> checkpoints;
    TextView tvStep, tvCode;
    ImageView imgQR;
    EditText editTripName;
    Button btnCancel, btnNext1, btnNext2, btnShare, btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        db = FirebaseFirestore.getInstance();
        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        userName = sharedPref.getString("userName", "notLoggedIn");
        checkpoints = new ArrayList<>();
        screens = new ArrayList<>();

        addScreen(findViewById(R.id.screen_1_activity_create_trip));
        addScreen(findViewById(R.id.screen_2_activity_create_trip));
        addScreen(findViewById(R.id.screen_3_activity_create_trip));

        tvStep = findViewById(R.id.tv_step_activity_create_trip);
        btnCancel = findViewById(R.id.btn_cancel_activity_create_trip);
        btnNext1 = findViewById(R.id.btn_continue_screen1_activity_create_trip);
        btnNext2 = findViewById(R.id.btn_continue_screen2_activity_create_trip);
        tvCode = findViewById(R.id.tv_code_activity_create_trip);
        imgQR = findViewById(R.id.img_qr_code_activity_create_trip);
        btnShare = findViewById(R.id.btn_share_activity_create_trip);
        btnFinish = findViewById(R.id.btn_finish_activity_create_trip);
        editTripName = findViewById(R.id.edit_name_trip_create);

        tvStep.setText(String.format(getString(R.string.tv_step_activity_create_trip), 1, 3));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvStep.setText(String.format(getString(R.string.tv_step_activity_create_trip), 2, 3));
                editTripName.clearFocus();
                KeyboardHelper.hideSoftKeyboard(CreateTripActivity.this);
                renderScreen(1);
            }
        });

        btnNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvStep.setText(String.format(getString(R.string.tv_step_activity_create_trip), 3, 3));
                final DocumentReference currentUserRef = db.collection(Collections.USERS).document(userName);
                db.collection(Collections.TRIPS).add(new Trip(editTripName.getText().toString(), currentUserRef)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            final DocumentReference tripRef = task.getResult();
                            currentUserRef.update(User.ACTIVE_TRIP, tripRef);
                            WriteBatch batch = db.batch();
                            for (Checkpoint checkpoint : checkpoints) {
                                batch.set(db.collection(Collections.checkpoints(tripRef.getId())).document(), checkpoint);
                            }
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Bitmap myBitmap = QRCode.from(String.format("http://%s/trips/%s", getString(R.string.server_host), tripRef.getId())).bitmap();
                                    tvCode.setText(tripRef.getId());
                                    imgQR.setImageBitmap(myBitmap);
                                    renderScreen(2);
                                }
                            });
                        }
                    }
                });
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "Mã tham gia nhóm Tripgether của tôi là: " + tvCode.getText().toString());
                startActivity(Intent.createChooser(share, "Chia sẻ mã tham gia"));
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvCheckpoints = findViewById(R.id.rv_checkpoint_trip_create);
        rvCheckpoints.setLayoutManager(new LinearLayoutManager(this));
        rvCheckpoints.setAdapter(new CheckPointAdapter(checkpoints));
    }

    private void addScreen(final View screen) {
        screens.add(screen);
        if (screens.size() > 1) {
            screen.post(new Runnable() {
                @Override
                public void run() {
                    ViewAnim.toggleHideShow(screen, false, ViewAnim.DIRECTION_RIGHT);
                    screen.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void renderScreen(int position) {
        if (position < 0 || position >= screens.size()) return;
        activeScreen = position;
        for (int i = 0; i < screens.size(); i++) {
            ViewAnim.toggleHideShow(screens.get(i), i == position, ViewAnim.DIRECTION_RIGHT);
        }
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

        public void bind(Checkpoint checkpoint) {
            tvName.setText(checkpoint.getName());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));
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

        public CheckPointAdapter(ArrayList<Checkpoint> checkpoints) {
            this.checkpoints = checkpoints;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_ADD_CHECKPOINT_BTN) {
                return new AddCheckPointViewHolder(getLayoutInflater().inflate(R.layout.activity_create_trip_item_btn_add, parent, false));
            } else {
                return new CheckPointViewHolder(getLayoutInflater().inflate(R.layout.activity_create_trip_item_checkpoint, parent, false));
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
                        DialogFragment dialogFragment = new CreateCheckpointDialogFragment(new CreateCheckpointDialogFragment.OnCheckpointSetListener() {
                            @Override
                            public void onCheckpointSet(Checkpoint checkpoint) {
                                checkpoints.add(checkpoint);
                                notifyItemInserted(checkpoints.size());
                            }
                        });
                        dialogFragment.show(getSupportFragmentManager(), "create trip checkpoint fragment");
                        Log.d(TAG, "show dialog");
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            Log.d(TAG, "getType" + position + "= " + this.checkpoints.size());
            if (position == this.checkpoints.size()) {
                return TYPE_ADD_CHECKPOINT_BTN;
            }
            return TYPE_CHECKPOINT_ITEM;
        }

        @Override
        public int getItemCount() {
            return this.checkpoints.size() + 1;
        }
    }
}
