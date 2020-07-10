package cf.bautroixa.tripgether.presenter.trip;

import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.ui.adapter.CreateTripCheckpointsAdapter;

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
