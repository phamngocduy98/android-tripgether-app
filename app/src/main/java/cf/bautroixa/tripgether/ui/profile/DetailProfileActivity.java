package cf.bautroixa.tripgether.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.MainActivity;
import cf.bautroixa.tripgether.ui.auth.PhoneVerificationActivity;
import cf.bautroixa.tripgether.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.tripgether.ui.dialogs.OneChooseImageDialog;
import cf.bautroixa.tripgether.utils.FirebaseStorageHelper;


public class DetailProfileActivity extends AppCompatActivity {
    ModelManager manager;
    ProgressDialog progress;
    private boolean firstTimeSetup = false;
    private ImageView mAvartar;
    private EditText mNameEditText;
    private EditText mPhoneNumberEditText;
    private TextView mEmailText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_profile);

        manager = ModelManager.getInstance(this);
        if (getIntent().getExtras() != null) {
            // first time user setup
            firstTimeSetup = true;
        }

        ImageButton mChooseImgButton = findViewById(R.id.btn_choose_image);
        mAvartar = findViewById(R.id.iv_avatar);
        Button mUpdateButton = findViewById(R.id.btn_update);
        mNameEditText = findViewById(R.id.et_name);
        mPhoneNumberEditText = findViewById(R.id.et_phone_number);
        mEmailText = findViewById(R.id.tv_email);


        mChooseImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OneChooseImageDialog oneChooseImageDialog = new OneChooseImageDialog();
                oneChooseImageDialog.setOnImagePickedListener(new OneChooseImageDialog.OnImagePickedListener() {
                    @Override
                    public void onPicked(@Nullable Uri uri, Bitmap bitmap) {
                        updateAvatar(uri, bitmap);
                    }
                });
                oneChooseImageDialog.show(getSupportFragmentManager(), "choose avatar");
            }
        });
        mPhoneNumberEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailProfileActivity.this, PhoneVerificationActivity.class));
            }
        });

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInformation();
            }
        });

        manager.getCurrentUser().attachListener(this, new Document.OnValueChangedListener<User>() {
            @Override
            public void onValueChanged(@NonNull User user) {
                if (user.isAvailable()) {
                    updateView(user);
                }
            }
        });
    }

    private void updateView(User user) {
        if (user.getName() != null) {
            mNameEditText.setText(user.getName());
        }
        if (user.getPhoneNumber() != null) {
            mPhoneNumberEditText.setText(user.getPhoneNumber());
        }
        if (user.getEmail() != null) {
            mEmailText.setText(user.getEmail());
        }
        if (user.getAvatar() != null) {
            Picasso.get().load(user.getAvatar()).into(mAvartar);
        }
    }

    public void onLoading(String loadingText) {
        if (progress == null) progress = LoadingDialogHelper.create(this, loadingText);
    }

    public void onDone(String message) {
        if (progress != null) progress.dismiss();
    }

    public void onFailed(String reason) {
        if (progress != null) progress.dismiss();
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
    }

    private void updateAvatar(@Nullable Uri uri, Bitmap bitmap) {
        onLoading("Đang tải lên...");
        StorageReference storageRef = FirebaseStorageHelper.getReference("avatar/" + manager.getCurrentUser().getId());
        Task<Uri> uploadTask;
        if (uri != null) {
            uploadTask = FirebaseStorageHelper.uploadImageForResult(storageRef, uri);
        } else {
            uploadTask = FirebaseStorageHelper.uploadImageForResult(storageRef, bitmap);
        }
        uploadTask.continueWithTask(new Continuation<Uri, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Uri> task) throws Exception {
                if (!task.isSuccessful()) throw task.getException();
                String url = task.getResult().toString();
                return manager.getCurrentUser().sendUpdate(null, User.AVATAR, url);
            }
        }).addOnCompleteListener(DetailProfileActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    onDone("Đã cập nhật ảnh đại diện thành công!");
                    mAvartar.setImageBitmap(bitmap);
                } else {
                    onFailed(task.getException().getMessage());
                }
            }
        });
    }

    private void updateInformation() {
        onLoading("Đang cập nhật...");
        final String name = mNameEditText.getText().toString();
        final String phoneNumber = mPhoneNumberEditText.getText().toString();
        manager.getCurrentUser().sendUpdate(null, User.NAME, name, User.PHONE, phoneNumber)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            onDone("Cập nhật thông tin cá nhân thành công");
                            if (firstTimeSetup) {
                                startActivity(new Intent(DetailProfileActivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            onFailed("Thất bại" + task.getException().getMessage());
                        }
                    }
                });

    }
}
