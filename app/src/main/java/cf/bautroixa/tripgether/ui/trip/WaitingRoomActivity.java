package cf.bautroixa.tripgether.ui.trip;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.http.TripHttpService;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.ui.adapter.TripWaitingRoomAdapter;
import cf.bautroixa.tripgether.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.tripgether.ui.theme.OneAppbarActivity;

public class WaitingRoomActivity extends OneAppbarActivity {
    RecyclerView rvTripMember;
    TripWaitingRoomAdapter adapter;
    ModelManager manager;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);
        setTitle("Yêu cầu tham gia");
        setSubtitle("Cho phép tham gia chuyến đi của bạn");

        manager = ModelManager.getInstance(this);
        if (!manager.getCurrentTrip().isAvailable() || !manager.isTripLeader()) {
            Toast.makeText(this, "Chỉ trưởng nhóm mới có thể truy cập", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        rvTripMember = findViewById(R.id.rv_waiting_user);
        rvTripMember.setLayoutManager(new LinearLayoutManager(this));
        loadingDialog = LoadingDialogHelper.create(this, "Vui lòng đợi");

        TripHttpService.getTripWaitingRoom(manager.getCurrentTrip().getId()).addOnCompleteListener(new OnCompleteListener<ArrayList<UserPublic>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<UserPublic>> task) {
                loadingDialog.dismiss();
                if (task.isSuccessful()) {
                    ArrayList<UserPublic> waitingRoom = task.getResult();
                    adapter = new TripWaitingRoomAdapter(WaitingRoomActivity.this, waitingRoom);
                    rvTripMember.setAdapter(adapter);
                } else {
                    Toast.makeText(WaitingRoomActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
