package cf.bautroixa.tripgether.presenter;

import cf.bautroixa.tripgether.model.firestore.objects.User;

public interface TripInvitationFriendsPresenter {
    void inviteFriend(User user);

    interface View {
        void onInviting();

        void onInvited();

        void onInviteFailed();
    }
}
