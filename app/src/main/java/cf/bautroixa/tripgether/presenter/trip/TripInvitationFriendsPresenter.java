package cf.bautroixa.tripgether.presenter.trip;

import cf.bautroixa.tripgether.model.firestore.objects.User;

public interface TripInvitationFriendsPresenter {
    void inviteFriend(User user);

    interface View {
        void onInviting();

        void onInvited();

        void onInviteFailed();
    }
}
