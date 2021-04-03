package cf.bautroixa.tripgether.ui.friends;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.http.UserHttpService;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.ui.OneAppbarActivity;
import cf.bautroixa.ui.OnePromptDialog;

public class AddFriendActivity extends OneAppbarActivity {
    LinearLayout linearPhone, linearMail, linearContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        setTitle("Thêm bạn bè");
        setSubtitle("Thêm bạn, thêm vui");

        linearPhone = findViewById(R.id.linear_add_phone_friend);
        linearMail = findViewById(R.id.linear_add_email_friend);
        linearContact = findViewById(R.id.linear_sync_contact);

        linearContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddFriendActivity.this, SyncContactActivity.class));
            }
        });

        linearPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnePromptDialog onePromptDialog = new OnePromptDialog.Builder()
                        .iconBody(R.drawable.ic_phone_app)
                        .title(R.string.dialog_title_enter_phone_num)
                        .editHintText(R.string.dialog_title_enter_phone_num)
                        .onResult(new OnePromptDialog.OnDialogResult() {
                            @Override
                            public void onDialogResult(final OnePromptDialog dialog, boolean isCanceled, final String value) {
                                if (!isCanceled) {
                                    UserHttpService.findUser(null, value).addOnCompleteListener(new OnCompleteListener<UserPublic>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UserPublic> task) {
                                            if (task.isSuccessful()) {
                                                dialog.toggleProgressBar(false);
                                                UserPublic userPublic = task.getResult();
                                                Intent intent = new Intent(AddFriendActivity.this, ProfileActivity.class);
                                                intent.putExtra(ProfileActivity.ARG_USER_PUBLIC_DATA, (Parcelable) userPublic);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(AddFriendActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }).build();
                onePromptDialog.show(getSupportFragmentManager(), "add friend");
            }
        });

        linearMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnePromptDialog onePromptDialog = new OnePromptDialog.Builder()
                        .iconBody(R.drawable.ic_mail_app)
                        .title(R.string.dialog_title_enter_user_name)
                        .editHintText(R.string.dialog_title_enter_user_name)
                        .onResult(new OnePromptDialog.OnDialogResult() {
                            @Override
                            public void onDialogResult(final OnePromptDialog dialog, boolean isCanceled, final String value) {
                                if (!isCanceled) {
                                    UserHttpService.findUser(value, null).addOnCompleteListener(new OnCompleteListener<UserPublic>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UserPublic> task) {
                                            if (task.isSuccessful()) {
                                                dialog.toggleProgressBar(false);
                                                UserPublic userPublic = task.getResult();
                                                Intent intent = new Intent(AddFriendActivity.this, ProfileActivity.class);
                                                intent.putExtra(ProfileActivity.ARG_USER_PUBLIC_DATA, (Parcelable) userPublic);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(AddFriendActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }).build();
                onePromptDialog.show(getSupportFragmentManager(), "add friend");
            }
        });
    }

    @Override
    public MotionLayout findMotionLayout() {
        return findViewById(R.id.appbar_root);
    }
}
