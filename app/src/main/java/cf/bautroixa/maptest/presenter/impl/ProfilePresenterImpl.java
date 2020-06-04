package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import cf.bautroixa.maptest.model.firestore.Collections;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.http.HttpRequest;
import cf.bautroixa.maptest.model.http.UserHttpService;
import cf.bautroixa.maptest.model.types.UserPublicData;
import cf.bautroixa.maptest.presenter.ProfilePresenter;

public class ProfilePresenterImpl implements ProfilePresenter {
    Context context;
    View view;
    ModelManager manager;

    public ProfilePresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance();
        this.context = context;
        this.view = view;
    }

    @Override
    public void OnRequestAddFriend(UserPublicData user) {
        view.onAddFriendSending();
        UserHttpService.sendAddFriend(manager.getCurrentUser(), user.getId(), UserHttpService.AddFriendActions.REQUEST).addOnCompleteListener(new OnCompleteListener<HttpRequest.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpRequest.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onAddFriendSent();
                } else {
                    view.onAddFriendFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void OnCancelAddFriendRequest(final UserPublicData user) {
        view.onAddFriendSending();
        UserHttpService.sendAddFriend(manager.getCurrentUser(), user.getId(), UserHttpService.AddFriendActions.CANCEL).addOnCompleteListener(new OnCompleteListener<HttpRequest.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpRequest.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onRemoveAddFriendSent();
                } else {
                    view.onAddFriendFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void OnRemoveFriend(UserPublicData user) {
        view.onAddFriendSending();
        UserHttpService.sendAddFriend(manager.getCurrentUser(), user.getId(), UserHttpService.AddFriendActions.REMOVE).addOnCompleteListener(new OnCompleteListener<HttpRequest.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpRequest.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onRemoveFriendSent();
                } else {
                    view.onAddFriendFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void OnAcceptAddFriend(UserPublicData user) {
        view.onAddFriendSending();
        UserHttpService.sendAddFriend(manager.getCurrentUser(), user.getId(), UserHttpService.AddFriendActions.ACCEPT).addOnCompleteListener(new OnCompleteListener<HttpRequest.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpRequest.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onAcceptAddFriendSent();
                } else {
                    view.onAddFriendFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void OnRejectAddFriend(UserPublicData user) {
        view.onAddFriendSending();
        UserHttpService.sendAddFriend(manager.getCurrentUser(), user.getId(), UserHttpService.AddFriendActions.REJECT).addOnCompleteListener(new OnCompleteListener<HttpRequest.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpRequest.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onRejectAddFriendSent();
                } else {
                    view.onAddFriendFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void init(UserPublicData userPublicData) {
        view.onAddFriendSending();
        String userId = userPublicData.getId();
        DocumentReference friendRef = manager.getDb().collection(Collections.USERS).document(userId);
        if (manager.getCurrentUser().getFriends().contains(friendRef)) {
            view.setUpView(userPublicData, User.FriendStatus.BE_FRIEND);
        } else if (manager.getCurrentUser().getFriendRequests().contains(friendRef)) {
            view.setUpView(userPublicData, User.FriendStatus.RECEIVED);
        } else if (manager.getCurrentUser().getSentFriendRequests().contains(friendRef)) {
            view.setUpView(userPublicData, User.FriendStatus.SENT);
        } else {
            view.setUpView(userPublicData, User.FriendStatus.NONE);
        }
    }

    @Override
    public void init(final String userId) {
        view.onAddFriendSending();
        DocumentReference friendRef = manager.getDb().collection(Collections.USERS).document(userId);
        if (manager.getCurrentUser().getFriends().contains(friendRef)) {
            initWithGettableUser(userId, User.FriendStatus.BE_FRIEND);
        } else if (manager.getCurrentUser().getFriendRequests().contains(friendRef)) {
            initWithGettableUser(userId, User.FriendStatus.RECEIVED);
        } else if (manager.getCurrentUser().getSentFriendRequests().contains(friendRef)) {
            initWithNonGettableUser(userId, User.FriendStatus.SENT);
        } else {
            initWithNonGettableUser(userId, User.FriendStatus.NONE);
        }
    }

    void initWithGettableUser(String userId, final int status) {
        manager.getBaseUsersManager().requestGet(userId).addOnCompleteListener(new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User friend = task.getResult();
                    view.setUpView(new UserPublicData(friend), status);
                }
            }
        });
    }

    void initWithNonGettableUser(String userId, final int status) {
        UserHttpService.getUserPublicData(userId).addOnCompleteListener(new OnCompleteListener<UserPublicData>() {
            @Override
            public void onComplete(@NonNull Task<UserPublicData> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    UserPublicData friend = task.getResult();
                    view.setUpView(friend, status);
                }
            }
        });
    }
}
