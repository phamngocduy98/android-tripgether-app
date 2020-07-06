package cf.bautroixa.maptest.presenter;

import cf.bautroixa.maptest.model.repo.objects.TripPublic;

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
