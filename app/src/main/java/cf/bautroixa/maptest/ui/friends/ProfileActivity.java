package cf.bautroixa.maptest.ui.friends;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.types.UserPublicData;
import cf.bautroixa.maptest.presenter.ProfilePresenter;
import cf.bautroixa.maptest.presenter.impl.ProfilePresenterImpl;
import cf.bautroixa.maptest.ui.MainActivity;
import cf.bautroixa.maptest.ui.theme.OneAppbarActivity;
import cf.bautroixa.maptest.ui.theme.OneDialog;
import cf.bautroixa.maptest.utils.ImageHelper;

public class ProfileActivity extends OneAppbarActivity implements ProfilePresenter.View {
    public static final String ARG_USER_ID = "userId";
    public static final String ARG_USER_PUBLIC_DATA = "userPublicData";
    ProfilePresenterImpl profilePresenter;
    UserPublicData user;

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

        Bundle args = getIntent().getExtras();
        if (args != null) {
            UserPublicData userPublicData = (UserPublicData) args.getSerializable(ARG_USER_PUBLIC_DATA);
            String userId = args.getString(ARG_USER_ID, User.NO_USER);
            if (userPublicData != null) {
                profilePresenter.init(userPublicData);
            } else if (!userId.equals(User.NO_USER)) {
                profilePresenter.init(userId);
            } else {
                Toast.makeText(this, "Lỗi: null intent", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void setUpView(UserPublicData user, int status) {
        this.user = user;
        loadingDialog.dismiss();
        setTitle(user.getName());
        ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar, 100, 100);
        linearChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: intent to MessagingActivity
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        setupAddFriendButton(user, status);
    }

    public void setupAddFriendButton(final UserPublicData user, final int status) {
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
                        profilePresenter.OnRequestAddFriend(user);
                        break;
                    case User.FriendStatus.BE_FRIEND:
                        profilePresenter.OnRemoveFriend(user);
                        break;
                    case User.FriendStatus.SENT:
                        profilePresenter.OnCancelAddFriendRequest(user);
                        break;
                    case User.FriendStatus.RECEIVED:
                        new OneDialog.Builder().posBtnText(R.string.btn_accept).negBtnText(R.string.btn_reject)
                                .title(R.string.dialog_title_confirm_add_friend)
                                .message(R.string.dialog_message_confirm_add_friend)
                                .buttonClickListener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            profilePresenter.OnAcceptAddFriend(user);
                                        } else {
                                            profilePresenter.OnRejectAddFriend(user);
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
    public void onAddFriendSending() {
        linearAddFriend.setEnabled(false);
        loadingDialog = ProgressDialog.show(ProfileActivity.this, "", "Vui lòng đợi", true, false);
        loadingDialog.setCustomTitle(new View(ProfileActivity.this));
    }

    @Override
    public void onAddFriendFailed(String reason) {
        linearAddFriend.setEnabled(true);
        loadingDialog.dismiss();
        Toast.makeText(ProfileActivity.this, reason, Toast.LENGTH_LONG).show();
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
