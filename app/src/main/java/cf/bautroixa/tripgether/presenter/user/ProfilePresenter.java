package cf.bautroixa.tripgether.presenter.user;

import cf.bautroixa.tripgether.model.repo.objects.UserPublic;

public interface ProfilePresenter {
    void init(UserPublic userPublic);

    void init(String userId);

    void requestAddFriend(UserPublic user);

    void cancelAddFriendRequest(UserPublic user);

    void removeFriend(UserPublic user);

    void acceptAddFriend(UserPublic user);

    void rejectAddFriend(UserPublic user);

    interface View {
        void setUpView(UserPublic user, int friendStatus);

        void onLoading();

        void onFailed(String reason, boolean finished);

        void onAddFriendSent();

        void onRemoveAddFriendSent();

        void onRemoveFriendSent();

        void onAcceptAddFriendSent();

        void onRejectAddFriendSent();
    }
}
