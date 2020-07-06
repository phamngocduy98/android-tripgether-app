package cf.bautroixa.maptest.ui.trip;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.presenter.CreateTripPresenter;
import cf.bautroixa.maptest.presenter.impl.CreateTripPresenterImpl;
import cf.bautroixa.maptest.ui.adapter.CreateTripCheckpointsAdapter;
import cf.bautroixa.maptest.ui.dialogs.CheckpointEditDialogFragment;
import cf.bautroixa.maptest.ui.theme.OneAppbarActivity;
import cf.bautroixa.maptest.ui.theme.ViewAnim;
import cf.bautroixa.maptest.ui.trip_invite.TripInvitationActivity;
import cf.bautroixa.maptest.utils.KeyboardHelper;

public class CreateTripActivity extends OneAppbarActivity implements CreateTripPresenter.View {

    private static final String TAG = "CreateTripActivity";
    CreateTripPresenterImpl createTripPresenter;
    RecyclerView rvCheckpoints;
    TextView tvTripNameHint;
    Button btnAddCheckpoint, btnCreateTrip;
    EditText editTripName;
    private CreateTripCheckpointsAdapter checkpointsAdapter;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);
        setTitle("Tạo chuyến đi");
        setSubtitle("");
        setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editTripName = findViewById(R.id.edit_name_activity_trip_create);
        tvTripNameHint = findViewById(R.id.tv_trip_name_hint_activity_create_trip);
        rvCheckpoints = findViewById(R.id.rv_checkpoint_trip_create);
        btnAddCheckpoint = findViewById(R.id.btn_add_checkpoint_activity_create_trip);
        btnCreateTrip = findViewById(R.id.btn_continue_screen2_activity_create_trip);

        createTripPresenter = new CreateTripPresenterImpl(this, this);
        checkpointsAdapter = new CreateTripCheckpointsAdapter(createTripPresenter, getSupportFragmentManager());
        createTripPresenter.initAdapter(checkpointsAdapter);
        setupAdapter();

        editTripName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    tvTripNameHint.setVisibility(View.VISIBLE);
                    btnCreateTrip.setEnabled(false);
                } else {
                    tvTripNameHint.setVisibility(View.INVISIBLE);
                    btnCreateTrip.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnAddCheckpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = CheckpointEditDialogFragment.newInstance(new CheckpointEditDialogFragment.OnCheckpointSetListener() {
                    @Override
                    public void onCheckpointSet(Checkpoint checkpoint) {
                        createTripPresenter.onAddCheckpoint(checkpoint);
                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "create trip checkpoint fragment");
                Log.d(TAG, "show dialog");
            }
        });
        btnCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTripPresenter.createTrip(editTripName.getText().toString());
            }
        });
    }

    @Override
    public void setupAdapter() {
        rvCheckpoints.setLayoutManager(new LinearLayoutManager(this));
        rvCheckpoints.setAdapter(checkpointsAdapter);
    }

    @Override
    public void onCreateTripLoading() {
        ViewAnim.toggleLoading(CreateTripActivity.this, btnCreateTrip, true, "Tạo chuyến đi");
        loadingDialog = ProgressDialog.show(CreateTripActivity.this, "", "Đang tạo chuyến đi", true, false);
        loadingDialog.setCustomTitle(new View(CreateTripActivity.this));
    }

    @Override
    public void onCreateTripDone() {
        loadingDialog.dismiss();
        KeyboardHelper.hideSoftKeyboard(CreateTripActivity.this);
        startActivity(new Intent(CreateTripActivity.this, TripInvitationActivity.class));
        finish();
    }
}
