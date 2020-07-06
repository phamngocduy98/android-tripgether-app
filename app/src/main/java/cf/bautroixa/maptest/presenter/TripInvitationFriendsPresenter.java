package cf.bautroixa.maptest.presenter;

import cf.bautroixa.maptest.model.firestore.objects.User;

public interface TripInvitationFriendsPresenter {
    void inviteFriend(User user);

    interface View {
        void onInviting();

        void onInvited();

        void onInviteFailed();
    }
}
