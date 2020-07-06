package cf.bautroixa.tripgether.presenter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.Task;

import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.ui.adapter.BottomCheckpointsAdapter;

public interface BottomCheckpointsPresenter {
    void initAdapter(LifecycleOwner lifecycleOwner, NavigationInterface navigationInterface);

    boolean isTripLeader();

    boolean isActiveCheckpoint(Checkpoint checkpoint);

    boolean isReadyToCheckIn(Checkpoint checkpoint);

    Task<Void> setActiveCheckpoint(Context context, @Nullable Checkpoint checkpoint);
    @Nullable
    BottomCheckpointsAdapter.UpdateVisitCountPayload getUpdateVisitCountPayload(Checkpoint checkpoint);
    Task<Void> sendCheckIn();

    void onScrollNewPosition(int position);

    interface View {
        void setupAdapter(BottomCheckpointsAdapter adapter);

        void setUpTimeLineString(Checkpoint currentCheckpoint, Checkpoint nextCheckpoint);

        void scrollToPosition(int position);

        void onTargetCheckpoint(Checkpoint checkpoint);
    }

    interface CallableMask {
        void selectCheckpoint(String checkpointId);
    }

    interface SavedStateKeys {
        String SAVED_ACTIVE_POS = "SAVED_ACTIVE_POS";
    }
}
