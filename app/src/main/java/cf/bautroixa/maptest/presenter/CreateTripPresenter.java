package cf.bautroixa.maptest.presenter;

import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.ui.adapter.CreateTripCheckpointsAdapter;

public interface CreateTripPresenter {
    void onAddCheckpoint(Checkpoint checkpoint);

    void createTrip(String tripName);

    void initAdapter(CreateTripCheckpointsAdapter checkpointsAdapter);

    interface View {
        void setupAdapter();

        void onCreateTripLoading();

        void onCreateTripDone();
    }
}
