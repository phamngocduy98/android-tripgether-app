package cf.bautroixa.maptest.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.TabProfileFragment;
import cf.bautroixa.maptest.firestore.User;


public class DetailProfileActivity extends AppCompatActivity {
    private ImageView mAvartar;
    private EditText mNameEditText;
    private EditText mPhoneNumberEditText;
    private TextView mEmailText;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    User currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;
//    String userName = "notLoggedIn";
//    private SharedPreferences sharedPref;
    DocumentReference docRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String currentImageName;
    String uid;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_profile);
        ImageButton mChooseImgButton = findViewById(R.id.btn_choose_image);
        mAvartar = findViewById(R.id.iv_avatar);
        Button mUpdateButton = findViewById(R.id.btn_update);
        mNameEditText = findViewById(R.id.et_name);
        mPhoneNumberEditText = findViewById(R.id.et_phone_number);
        mEmailText = findViewById(R.id.tv_email);

        mChooseImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(v.getContext());
            }
        });
        user= mAuth.getCurrentUser();
        assert user != null;
        uid=user.getUid();
        docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document!=null){
                        User user = document.toObject(User.class);
                        onUpdateInfoView(user);
                    }


                }
            }
        });
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInformation();
            }
        });
    }

//    private void getUserInfo() {
//        user= mAuth.getCurrentUser();
//        assert user != null;
//        String uid=user.getUid();
//        docRef = db.collection("users").document(uid);
//        docRef.get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if(document!=null){
//                        User user = document.toObject(User.class);
//                        onUpdateInfoView(user);
//                    }
//
//
//                }
//            }
//        });
//    }

    private void onUpdateInfoView(User user) {
        if(user!=null){
            if (user.getName() != null) {
                mNameEditText.setText(user.getName());
            }
            if (user.getPhoneNumber() != null) {
                mPhoneNumberEditText.setText(user.getPhoneNumber());
            }
            if (user.getEmail() != null) {
                mEmailText.setText(user.getEmail());
            }
            if(user.getAvatar()!=null){
                Picasso.get().load(user.getAvatar()).into(mAvartar);
            }

        }
    }

    private void updateInformation() {
        final String imageName= UUID.randomUUID().toString();
        final StorageReference storageRef = storage.getReference("images/"+imageName);
        mAvartar.setDrawingCacheEnabled(true);
        mAvartar.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) mAvartar.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final String name=mNameEditText.getText().toString();
                final String phoneNumber=mPhoneNumberEditText.getText().toString();
                docRef
                        .update("name", name,
                                "phoneNumber",phoneNumber
                                )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(DetailProfileActivity.this,
                                        "Cập nhật thông tin cá nhân thành công",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(DetailProfileActivity.this, TabProfileFragment.class);
                                startActivity(intent);
                                Log.d("TAG","Success");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DetailProfileActivity.this,
                                        "Cập nhật thông tin cá nhân thất bại",Toast.LENGTH_SHORT).show();
                                Log.d("TAG","Failure");
                            }
                        });



            }
        });


    }

    private void updateAvatar() {
        user= mAuth.getCurrentUser();
        assert user != null;
        String uid=user.getUid();
        docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document!=null){
                        User user = document.toObject(User.class);
                        if(user!=null)
                            currentImageName=user.getImageName();
                        Log.d("Image",currentImageName);
                    }
                }
            }
        });

        final String imageName= UUID.randomUUID().toString();
        final StorageReference storageRef = storage.getReference("images/"+imageName);
        mAvartar.setDrawingCacheEnabled(true);
        mAvartar.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) mAvartar.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageRef.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl=uri.toString();
                        docRef
                                .update("imageName",imageName,
                                        "avatar",imageUrl
                                )
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        deleteImageInStorage();
                                        Toast.makeText(DetailProfileActivity.this,
                                                "Cập nhật avatar thành công",Toast.LENGTH_SHORT).show();
                                        Log.d("TAG","Success");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TAG","Failure");
                                        Toast.makeText(DetailProfileActivity.this,
                                                "Cập nhật avatar thất bại",Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });



            }
        });

    }

    private void deleteImageInStorage() {

        StorageReference storageRef = storage.getReference("images/"+currentImageName);
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    private void getCurrentImageName() {
        user= mAuth.getCurrentUser();
        assert user != null;
        String uid=user.getUid();
        docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document!=null){
                        User user = document.toObject(User.class);
                        if(user!=null)
                        currentImageName=user.getImageName();
                        Log.d("Image",currentImageName);
                        updateAvatar();
                    }
                }
            }
        });
    }


    private void chooseImage(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        mAvartar.setImageBitmap(selectedImage);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                mAvartar.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }
                        getCurrentImageName();
                    }
                    break;
            }
        }
    }


}
