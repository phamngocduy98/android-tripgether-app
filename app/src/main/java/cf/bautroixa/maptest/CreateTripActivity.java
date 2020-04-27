package cf.bautroixa.maptest;

import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

import cf.bautroixa.maptest.dialogs.DialogCheckpointEditFragment;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.theme.OneAppbarActivity;
import cf.bautroixa.maptest.theme.OneDialog;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.KeyboardHelper;

public class CreateTripActivity extends OneAppbarActivity {

    private static final String TAG = "CreateTripActivity";
    TextView tvCode, tvTripNameHint;
    ArrayList<View> screens;
    int activeScreen = 0;

    RecyclerView rvCheckpoints;
    ArrayList<Checkpoint> checkpoints;
    Button btnAddCheckpoint, btnCreateTrip, btnShare, btnFinish;
    ImageView imgQR;
    EditText editTripName;
    Group groupNoCheckpoint;
    CheckpointsAdapter adapter;
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
        setTitle("Tạo chuyến đi");

        manager = MainAppManager.getInstance();
        checkpoints = new ArrayList<>();
        screens = new ArrayList<>();

        addScreen(findViewById(R.id.screen_1_activity_create_trip));
        addScreen(findViewById(R.id.screen_2_activity_create_trip));
        // screen1
        editTripName = findViewById(R.id.edit_name_activity_trip_create);
        tvTripNameHint = findViewById(R.id.tv_trip_name_hint_activity_create_trip);

        rvCheckpoints = findViewById(R.id.rv_checkpoint_trip_create);
        rvCheckpoints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckpointsAdapter();
        rvCheckpoints.setAdapter(adapter);
        btnAddCheckpoint = findViewById(R.id.btn_add_checkpoint_activity_create_trip);

        btnCreateTrip = findViewById(R.id.btn_continue_screen2_activity_create_trip);
        // screen 3
        tvCode = findViewById(R.id.tv_code_activity_create_trip);
        imgQR = findViewById(R.id.img_qr_code_activity_create_trip);
        btnShare = findViewById(R.id.btn_share_activity_create_trip);
        btnFinish = findViewById(R.id.btn_finish_activity_create_trip);

        setSubtitle(String.format(getString(R.string.tv_step_activity_create_trip), 1, 2));
        setBackButtonOnClickListener(new View.OnClickListener() {
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
        btnAddCheckpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = DialogCheckpointEditFragment.newInstance(new DialogCheckpointEditFragment.OnCheckpointSetListener() {
                    @Override
                    public void onCheckpointSet(Checkpoint checkpoint) {
                        checkpoints.add(checkpoint);
                        adapter.notifyItemInserted(checkpoints.size());
                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "create trip checkpoint fragment");
                Log.d(TAG, "show dialog");
            }
        });
        btnCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference newTripRef = manager.sendCreateTrip(editTripName.getText().toString(), checkpoints, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            renderScreen(1);
                            KeyboardHelper.hideSoftKeyboard(CreateTripActivity.this);
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
        setSubtitle(String.format(getString(R.string.tv_step_activity_create_trip), activeScreen + 1, 2));
        for (int i = 0; i < screens.size(); i++) {
            ViewAnim.toggleHideShow(screens.get(i), i == position, ViewAnim.DIRECTION_RIGHT);
        }
    }


    public interface Payload {
        int SET_ACTIVE_CHECKPOINT = 1;
        int UNSET_ACTIVE_CHECKPOINT = 2;
    }

    public class CheckpointVH extends RecyclerView.ViewHolder {
        public TimelineView mTimelineView;
        TextView tvName, tvTime, tvLocation;
        ImageButton btnEdit;
        View view;

        public CheckpointVH(@NonNull View itemView, int viewType) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
            btnEdit = itemView.findViewById(R.id.btn_edit_item_checkpoint);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
        }

        public void bind(final int index) {
            final Checkpoint checkpoint = checkpoints.get(index);
            tvName.setText(checkpoint.getName());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));
            tvLocation.setText(checkpoint.getLocation());

            mTimelineView.setMarker(getResources().getDrawable(R.drawable.bg_item_message_outcoming));

            mTimelineView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final OneDialog confirmationDialog = new OneDialog.Builder().title(R.string.dialog_title_confirm_set_active_checkpoint)
                            .message(R.string.dialog_message_confirm_set_active_checkpoint)
                            .enableNegativeButton(true).build();
                    confirmationDialog.setButtonClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                confirmationDialog.toggleProgressBar(true);
                                manager.sendAddCheckInLocation(null, checkpoint.getRef(), new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        confirmationDialog.toggleProgressBar(false);
                                        dialog.dismiss();
                                    }
                                });
                            }
                        }
                    });
                    confirmationDialog.show(getSupportFragmentManager(), "confirm set active trip");
                }
            });
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogCheckpointEditFragment.newInstance(checkpoint, new DialogCheckpointEditFragment.OnCheckpointSetListener() {
                        @Override
                        public void onCheckpointSet(Checkpoint newCheckpoint) {
                            checkpoints.set(index, newCheckpoint);
                            adapter.notifyItemChanged(index);
                        }
                    }, new DialogCheckpointEditFragment.OnDeleteCheckpointListener() {
                        @Override
                        public void onCheckpointDeleted() {
                            checkpoints.remove(index);
                            adapter.notifyItemRemoved(index);
                        }
                    }).show(getSupportFragmentManager(), "edit checkpoint");
                }
            });
        }
    }

    public class CheckpointsAdapter extends RecyclerView.Adapter<CheckpointVH> {

        @NonNull
        @Override
        public CheckpointVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CheckpointVH(getLayoutInflater().inflate(R.layout.item_checkpoint_with_timeline, parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull CheckpointVH holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemViewType(int position) {
            return TimelineView.getTimeLineViewType(position, getItemCount());
        }

        @Override
        public int getItemCount() {
            return checkpoints.size();
        }
    }
}
