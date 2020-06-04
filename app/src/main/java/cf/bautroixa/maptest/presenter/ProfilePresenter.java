package cf.bautroixa.maptest.presenter;

import cf.bautroixa.maptest.model.types.UserPublicData;

public interface ProfilePresenter {
    void init(UserPublicData userPublicData);

    void init(String userId);

    void OnRequestAddFriend(UserPublicData user);

    void OnCancelAddFriendRequest(UserPublicData user);

    void OnRemoveFriend(UserPublicData user);

    void OnAcceptAddFriend(UserPublicData user);

    void OnRejectAddFriend(UserPublicData user);

    interface View {
        void setUpView(UserPublicData user, int friendStatus);

        void onAddFriendSending();

        void onAddFriendFailed(String reason);

        void onAddFriendSent();

        void onRemoveAddFriendSent();

        void onRemoveFriendSent();

        void onAcceptAddFriendSent();

        void onRejectAddFriendSent();
    }
}
