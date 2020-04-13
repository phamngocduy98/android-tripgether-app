package cf.bautroixa.maptest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.KeyboardHelper;

public class CreateTripActivity extends AppCompatActivity {

    private static final String TAG = "CreateTripActivity";
    TextView tvStep, tvCode, tvTripNameHint;
    ArrayList<View> screens;
    int activeScreen = 0;

    RecyclerView rvCheckpoints;
    ArrayList<Checkpoint> checkpoints;
    Button btnCancel, btnNext1, btnAddCheckpoint, btnNext2, btnShare, btnFinish;
    ImageView imgQR;
    EditText editTripName;
    Group groupNoCheckpoint;
    CheckPointAdapter adapter;
    private MainAppManager manager;

    @Override
    public void onBackPressed() {
        if (activeScreen == 0) super.onBackPressed();
        else renderScreen(activeScreen - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        manager = MainAppManager.getInstance();
        checkpoints = new ArrayList<>();
        screens = new ArrayList<>();

        addScreen(findViewById(R.id.screen_1_activity_create_trip));
        addScreen(findViewById(R.id.screen_2_activity_create_trip));
        addScreen(findViewById(R.id.screen_3_activity_create_trip));

        // header
        tvStep = findViewById(R.id.tv_step_activity_create_trip);
        btnCancel = findViewById(R.id.btn_cancel_activity_create_trip);
        // screen1
        editTripName = findViewById(R.id.edit_name_activity_trip_create);
        tvTripNameHint = findViewById(R.id.tv_trip_name_hint_activity_create_trip);
        btnNext1 = findViewById(R.id.btn_continue_screen1_activity_create_trip);
        // screen 2
        btnNext2 = findViewById(R.id.btn_continue_screen2_activity_create_trip);
        groupNoCheckpoint = findViewById(R.id.group_no_checkpoint);
        rvCheckpoints = findViewById(R.id.rv_checkpoint_trip_create);
        rvCheckpoints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckPointAdapter(checkpoints);
        rvCheckpoints.setAdapter(adapter);
        btnAddCheckpoint = findViewById(R.id.btn_add_checkpoint_activity_create_trip);
        // screen 3
        tvCode = findViewById(R.id.tv_code_activity_create_trip);
        imgQR = findViewById(R.id.img_qr_code_activity_create_trip);
        btnShare = findViewById(R.id.btn_share_activity_create_trip);
        btnFinish = findViewById(R.id.btn_finish_activity_create_trip);

        screen2NoCheckpoint();

        tvStep.setText(String.format(getString(R.string.tv_step_activity_create_trip), 1, 3));
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        editTripName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) tvTripNameHint.setVisibility(View.VISIBLE);
                else tvTripNameHint.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvTripNameHint.getVisibility() == View.VISIBLE) return;
                editTripName.clearFocus();
                KeyboardHelper.hideSoftKeyboard(CreateTripActivity.this);
                renderScreen(1);
            }
        });
        btnAddCheckpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new CreateCheckpointDialogFragment(new CreateCheckpointDialogFragment.OnCheckpointSetListener() {
                    @Override
                    public void onCheckpointSet(Checkpoint checkpoint) {
                        checkpoints.add(checkpoint);
                        adapter.notifyItemInserted(checkpoints.size());
                        screen2NoCheckpoint();
                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "create trip checkpoint fragment");
                Log.d(TAG, "show dialog");
            }
        });
        btnNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference newTripRef = manager.sendCreateTrip(editTripName.getText().toString(), checkpoints, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            renderScreen(2);
                        }
                    }
                });
                Bitmap myBitmap = QRCode.from(String.format("http://%s/trips/%s", getString(R.string.server_host), newTripRef.getId())).bitmap();
                tvCode.setText(newTripRef.getId());
                imgQR.setImageBitmap(myBitmap);
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
        tvStep.setText(String.format(getString(R.string.tv_step_activity_create_trip), activeScreen + 1, 3));
        for (int i = 0; i < screens.size(); i++) {
            ViewAnim.toggleHideShow(screens.get(i), i == position, ViewAnim.DIRECTION_RIGHT);
        }
    }

    protected void screen2NoCheckpoint() {
        if (checkpoints.size() == 0) {
            groupNoCheckpoint.setVisibility(View.VISIBLE);
            rvCheckpoints.setVisibility(View.INVISIBLE);
        } else {
            groupNoCheckpoint.setVisibility(View.INVISIBLE);
            rvCheckpoints.setVisibility(View.VISIBLE);
        }
    }


    public class CheckPointViewHolder extends RecyclerView.ViewHolder {
        public static final int TYPE_TOP = 0;
        public static final int TYPE_FULL = 1;
        public static final int TYPE_NORMAL = 2;
        public static final int TYPE_BOT = 3;
        TextView tvName, tvTime, tvLocation;
        View view;

        public CheckPointViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            view = itemView;
            switch (viewType) {
                case TYPE_TOP:
                    view.setBackgroundResource(R.drawable.bg_radius_top_white_with_border);
                    break;
                case TYPE_NORMAL:
                    view.setBackgroundResource(R.drawable.bg_no_radius_white_with_border);
                    break;
                case TYPE_BOT:
                    view.setBackgroundResource(R.drawable.bg_radius_bot_white_with_border);
                    break;
                default:
                    view.setBackgroundResource(R.drawable.bg_radius_full_white_with_border);
            }
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

    public class CheckPointAdapter extends RecyclerView.Adapter<CheckPointViewHolder> {
        ArrayList<Checkpoint> checkpoints;

        public CheckPointAdapter(ArrayList<Checkpoint> checkpoints) {
            this.checkpoints = checkpoints;
        }

        @NonNull
        @Override
        public CheckPointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CheckPointViewHolder(getLayoutInflater().inflate(R.layout.activity_create_trip_item_checkpoint, parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull CheckPointViewHolder holder, int position) {
            Checkpoint checkpoint = this.checkpoints.get(position);
            holder.bind(checkpoint);
        }
        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                if (getItemCount() > 1) return CheckPointViewHolder.TYPE_TOP;
                else return CheckPointViewHolder.TYPE_FULL;
            } else if (position == getItemCount() - 1) {
                return CheckPointViewHolder.TYPE_BOT;
            }
            return CheckPointViewHolder.TYPE_NORMAL;
        }

        @Override
        public int getItemCount() {
            return this.checkpoints.size();
        }
    }
}
