package cf.bautroixa.maptest.presenter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.ui.adapter.BottomCheckpointsAdapter;

public interface BottomCheckpointsPresenter {
    void initAdapter(LifecycleOwner lifecycleOwner, NavigationInterfaces navigationInterfaces);

    boolean isTripLeader();

    boolean isActiveCheckpoint(Checkpoint checkpoint);

    boolean isReadyToCheckIn(Checkpoint checkpoint);

    @Nullable
    BottomCheckpointsAdapter.UpdateVisitCountPayload getUpdateVisitCountPayload(Checkpoint checkpoint);

    Task<Void> sendCheckIn();

    Task<Void> setActiveCheckpoint(Context context, DocumentReference checkpointRef);

    void onScrollNewPosition(int position);

    interface View {
        void setupAdapter(BottomCheckpointsAdapter adapter);

        void setUpTimeLineString(Checkpoint currentCheckpoint, Checkpoint nextCheckpoint);

        void scrollToPosition(int position);

        void onNoActiveTrip();

        void onInTrip();

        void onTargetCheckpoint(Checkpoint checkpoint);
    }

    interface CallableMask {
        void selectCheckpoint(String checkpointId);
    }

    interface SavedStateKeys {
        String SAVED_ACTIVE_POS = "SAVED_ACTIVE_POS";
    }
}
