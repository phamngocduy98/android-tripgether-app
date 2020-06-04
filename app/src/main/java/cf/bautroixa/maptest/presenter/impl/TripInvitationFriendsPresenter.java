package cf.bautroixa.maptest.presenter.impl;

import cf.bautroixa.maptest.model.firestore.User;

public interface TripInvitationFriendsPresenter {
    void inviteFriend(User user);

    interface View {
        void onInviting();

        void onInvited();

        void onInviteFailed();
    }
}
