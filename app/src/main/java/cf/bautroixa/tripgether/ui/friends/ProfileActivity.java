package cf.bautroixa.tripgether.ui.friends;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.presenter.user.ProfilePresenter;
import cf.bautroixa.tripgether.presenter.user.ProfilePresenterImpl;
import cf.bautroixa.tripgether.ui.chat.ChatActivity;
import cf.bautroixa.tripgether.ui.theme.OneAppbarActivity;
import cf.bautroixa.tripgether.ui.theme.OneDialog;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class ProfileActivity extends OneAppbarActivity implements ProfilePresenter.View {
    public static final String ARG_USER_ID = "userId";
    public static final String ARG_USER_PUBLIC_DATA = "userPublicData";
    ProfilePresenterImpl profilePresenter;
    UserPublic user;

    LinearLayout linearAddFriend, linearChat;
    TextView tvAddFriend;
    ImageView imgAvatar, icAddFriend;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePresenter = new ProfilePresenterImpl(this, this);

        imgAvatar = findViewById(R.id.appbar_img_avatar);
        linearAddFriend = findViewById(R.id.ln_add_friend);
        icAddFriend = findViewById(R.id.ic_add_friend);
        tvAddFriend = findViewById(R.id.tv_add_friend);

        linearChat = findViewById(R.id.ln_chat);

        Uri data = getIntent().getData();
        if (data != null && data.getScheme() != null) {
            String scheme = data.getScheme(); // "http" or "tripgether"
            List<String> params = data.getPathSegments();
            if (scheme.equals("http") && params.size() >= 3 && params.get(1).equals("users")) {
                String userId = params.get(2);
                profilePresenter.init(userId);
                return;
            } else if (scheme.equals("tripgether") && params.size() > 0) {
                String userId = params.get(0);
                profilePresenter.init(userId);
                return;
            } else {
                Toast.makeText(this, "Lỗi: invalid web intent", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        Bundle args = getIntent().getExtras();
        if (args != null) {
            UserPublic userPublic = (UserPublic) args.getSerializable(ARG_USER_PUBLIC_DATA);
            String userId = args.getString(ARG_USER_ID, User.NO_USER);
            if (userPublic != null) {
                profilePresenter.init(userPublic);
            } else if (!userId.equals(User.NO_USER)) {
                profilePresenter.init(userId);
            } else {
                Toast.makeText(this, "Lỗi: invalid intent, uid or data must be specified", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void setUpView(final UserPublic user, int status) {
        this.user = user;
        loadingDialog.dismiss();
        setTitle(user.getName());
        ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar, 100, 100);
        linearChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.ARG_TO_USER_ID, user.getId());
                startActivity(intent);
            }
        });
        setupAddFriendButton(user, status);
    }

    public void setupAddFriendButton(final UserPublic user, final int status) {
        switch (status) {
            case User.FriendStatus.NONE:
                tvAddFriend.setText("Kết bạn");
                break;
            case User.FriendStatus.BE_FRIEND:
                tvAddFriend.setText("Hủy kết bạn");
                break;
            case User.FriendStatus.SENT:
                tvAddFriend.setText("Hủy yêu cầu kết bạn");
                break;
            case User.FriendStatus.RECEIVED:
                tvAddFriend.setText("Chấp nhận yêu cầu");
        }
        linearAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status) {
                    case User.FriendStatus.NONE:
                        profilePresenter.requestAddFriend(user);
                        break;
                    case User.FriendStatus.BE_FRIEND:
                        profilePresenter.removeFriend(user);
                        break;
                    case User.FriendStatus.SENT:
                        profilePresenter.cancelAddFriendRequest(user);
                        break;
                    case User.FriendStatus.RECEIVED:
                        new OneDialog.Builder().posBtnText(R.string.btn_accept).negBtnText(R.string.btn_reject)
                                .title(R.string.dialog_title_confirm_add_friend)
                                .message(R.string.dialog_message_confirm_add_friend)
                                .buttonClickListener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            profilePresenter.acceptAddFriend(user);
                                        } else {
                                            profilePresenter.rejectAddFriend(user);
                                        }
                                        dialog.dismiss();
                                    }
                                }).show(getSupportFragmentManager(), "confirm add friend");
                }
            }
        });
        linearAddFriend.setEnabled(true);
    }

    @Override
    public void onLoading() {
        linearAddFriend.setEnabled(false);
        loadingDialog = ProgressDialog.show(ProfileActivity.this, "", "Vui lòng đợi", true, false);
        loadingDialog.setCustomTitle(new View(ProfileActivity.this));
    }

    public void onSuccess() {
        loadingDialog.dismiss();
    }

    @Override
    public void onFailed(String reason, boolean finished) {
        linearAddFriend.setEnabled(true);
        if (loadingDialog != null) loadingDialog.dismiss();
        Toast.makeText(ProfileActivity.this, reason, Toast.LENGTH_LONG).show();
        if (finished) finish();
    }

    @Override
    public void onAddFriendSent() {
        linearAddFriend.setEnabled(true);
        loadingDialog.dismiss();
        setupAddFriendButton(this.user, User.FriendStatus.SENT);
    }

    @Override
    public void onRemoveAddFriendSent() {
        linearAddFriend.setEnabled(true);
        loadingDialog.dismiss();
        setupAddFriendButton(this.user, User.FriendStatus.NONE);
    }

    @Override
    public void onRemoveFriendSent() {
        linearAddFriend.setEnabled(true);
        loadingDialog.dismiss();
        setupAddFriendButton(this.user, User.FriendStatus.NONE);
    }

    @Override
    public void onAcceptAddFriendSent() {
        linearAddFriend.setEnabled(true);
        loadingDialog.dismiss();
        setupAddFriendButton(this.user, User.FriendStatus.BE_FRIEND);
    }

    @Override
    public void onRejectAddFriendSent() {
        linearAddFriend.setEnabled(true);
        loadingDialog.dismiss();
        setupAddFriendButton(this.user, User.FriendStatus.NONE);
    }
}
