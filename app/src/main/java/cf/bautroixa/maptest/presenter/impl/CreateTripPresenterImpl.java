package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.presenter.CreateTripPresenter;
import cf.bautroixa.maptest.ui.adapter.CreateTripCheckpointsAdapter;

public class CreateTripPresenterImpl implements CreateTripPresenter {
    ModelManager manager;
    Context context;
    View view;
    ArrayList<Checkpoint> checkpoints;
    CreateTripCheckpointsAdapter checkpointsAdapter;

    public CreateTripPresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance(context);
        this.context = context;
        this.view = view;
        this.checkpoints = new ArrayList<>();
    }

    @Override
    public void onAddCheckpoint(Checkpoint checkpoint) {
        checkpoints.add(checkpoint);
        checkpointsAdapter.notifyItemInserted(checkpoints.size() - 1);
    }

    @Override
    public void createTrip(String tripName) {
        view.onCreateTripLoading();
        manager.sendCreateTrip(tripName, checkpoints).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    DocumentReference newTripRef = task.getResult();
                    view.onCreateTripDone();
                }
            }
        });
    }

    @Override
    public void initAdapter(CreateTripCheckpointsAdapter checkpointsAdapter) {
        this.checkpointsAdapter = checkpointsAdapter;
        checkpointsAdapter.setCheckpoints(checkpoints);
    }
}
