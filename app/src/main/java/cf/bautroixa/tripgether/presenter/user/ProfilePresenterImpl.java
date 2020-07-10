package cf.bautroixa.tripgether.presenter.user;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import cf.bautroixa.tripgether.model.constant.Collections;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.http.HttpService;
import cf.bautroixa.tripgether.model.http.UserHttpService;
import cf.bautroixa.tripgether.model.repo.RepositoryManager;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.presenter.user.ProfilePresenter;

public class ProfilePresenterImpl implements ProfilePresenter {
    Context context;
    View view;
    ModelManager manager;
    RepositoryManager repositoryManager;

    public ProfilePresenterImpl(Context context, View view) {
        this.manager = ModelManager.getInstance(context);
        this.repositoryManager = RepositoryManager.getInstance(context);
        this.context = context;
        this.view = view;
    }

    @Override
    public void init(UserPublic userPublic) {
        view.onLoading();
        String userId = userPublic.getId();
        if (userId.equals(manager.getCurrentUser().getId())) {
            view.onFailed("Đây là bạn!", true);
            return;
        }
        DocumentReference friendRef = manager.getDb().collection(Collections.USERS).document(userId);
        if (manager.getCurrentUser().getFriends().contains(friendRef)) {
            view.setUpView(userPublic, User.FriendStatus.BE_FRIEND);
        } else if (manager.getCurrentUser().getFriendRequests().contains(friendRef)) {
            view.setUpView(userPublic, User.FriendStatus.RECEIVED);
        } else if (manager.getCurrentUser().getSentFriendRequests().contains(friendRef)) {
            view.setUpView(userPublic, User.FriendStatus.SENT);
        } else {
            view.setUpView(userPublic, User.FriendStatus.NONE);
        }
    }

    @Override
    public void init(final String userId) {
        if (userId.equals(manager.getCurrentUser().getId())) {
            view.onFailed("Đây là bạn!", true);
            return;
        }
        view.onLoading();
        DocumentReference friendRef = manager.getDb().collection(Collections.USERS).document(userId);
        if (manager.getCurrentUser().getFriends().contains(friendRef)) {
            initWithGettableUser(userId, User.FriendStatus.BE_FRIEND);
        } else if (manager.getCurrentUser().getFriendRequests().contains(friendRef)) {
            initWithNonGettableUser(userId, User.FriendStatus.RECEIVED);
        } else if (manager.getCurrentUser().getSentFriendRequests().contains(friendRef)) {
            initWithNonGettableUser(userId, User.FriendStatus.SENT);
        } else {
            initWithNonGettableUser(userId, User.FriendStatus.NONE);
        }
    }

    private void initWithGettableUser(String userId, final int status) {
        manager.getBaseUsersManager().requestGet(userId).addOnCompleteListener(new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User friend = task.getResult();
                    view.setUpView(new UserPublic(friend), status);
                } else {
                    view.onFailed(task.getException().getMessage(), true);
                }
            }
        });
    }

    private void initWithNonGettableUser(String userId, final int status) {
        repositoryManager.getUserRepository().requestGet(userId).addOnCompleteListener(new OnCompleteListener<UserPublic>() {
            @Override
            public void onComplete(@NonNull Task<UserPublic> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    UserPublic friend = task.getResult();
                    view.setUpView(friend, status);
                } else {
                    view.onFailed(task.getException().getMessage(), true);
                }
            }
        });
    }

    @Override
    public void requestAddFriend(UserPublic user) {
        view.onLoading();
        UserHttpService.sendAddFriend(user.getId(), UserHttpService.AddFriendActions.REQUEST).addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onAddFriendSent();
                } else {
                    view.onFailed(task.getException().getMessage(), false);
                }
            }
        });
    }

    @Override
    public void cancelAddFriendRequest(final UserPublic user) {
        view.onLoading();
        UserHttpService.sendAddFriend(user.getId(), UserHttpService.AddFriendActions.CANCEL).addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onRemoveAddFriendSent();
                } else {
                    view.onFailed(task.getException().getMessage(), false);
                }
            }
        });
    }

    @Override
    public void removeFriend(UserPublic user) {
        view.onLoading();
        UserHttpService.sendAddFriend(user.getId(), UserHttpService.AddFriendActions.REMOVE).addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onRemoveFriendSent();
                } else {
                    view.onFailed(task.getException().getMessage(), false);
                }
            }
        });
    }

    @Override
    public void acceptAddFriend(UserPublic user) {
        view.onLoading();
        UserHttpService.sendAddFriend(user.getId(), UserHttpService.AddFriendActions.ACCEPT).addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onAcceptAddFriendSent();
                } else {
                    view.onFailed(task.getException().getMessage(), false);
                }
            }
        });
    }

    @Override
    public void rejectAddFriend(UserPublic user) {
        view.onLoading();
        UserHttpService.sendAddFriend(user.getId(), UserHttpService.AddFriendActions.REJECT).addOnCompleteListener(new OnCompleteListener<HttpService.APIResponse>() {
            @Override
            public void onComplete(@NonNull Task<HttpService.APIResponse> task) {
                if (task.isSuccessful()) {
                    view.onRejectAddFriendSent();
                } else {
                    view.onFailed(task.getException().getMessage(), false);
                }
            }
        });
    }

}
