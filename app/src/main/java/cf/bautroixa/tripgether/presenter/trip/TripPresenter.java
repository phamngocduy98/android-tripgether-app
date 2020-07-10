package cf.bautroixa.tripgether.presenter.trip;

import cf.bautroixa.tripgether.model.repo.objects.TripPublic;

public interface TripPresenter {
    void init(String tripId, String joinCode);

    void joinTrip();

    interface View {
        void setupView(TripPublic tripPublic);

        void onLoading();

        void onJoinComplete();

        void onFailed(String message);
    }
}
