package cf.bautroixa.maptest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import cf.bautroixa.maptest.firestore.User;

public class ProfileActivity extends AppCompatActivity {
    private LinearLayout mPersonalInformationLinear;
    private LinearLayout mLogoutLinear;
    private LinearLayout mChangePasswordLinear;

    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    DocumentReference docRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    GoogleSignInClient mGoogleSignInClient;
    private String googleClientId="703604566706-upp9g9rtcdh3adrflqcgddt4p712jh27.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mPersonalInformationLinear=findViewById(R.id.ln_personal_information);
        mLogoutLinear=findViewById(R.id.ln_logout);
        mChangePasswordLinear=findViewById(R.id.ln_change_password);
//        mAvatarImage=findViewById(R.id.iv_main_avatar);
        user= mAuth.getCurrentUser();
        assert user != null;
        String uid=user.getUid();
//        mAvatarImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                chooseImage(v.getContext());
//            }
//        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mPersonalInformationLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(ProfileActivity.this,DetailProfileActivity.class);
                startActivity(intent);
            }
        });
        mChangePasswordLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(ProfileActivity.this,ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
        mLogoutLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
//        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
//        String email=user.getEmail();
//        Toast.makeText(ProfileActivity.this,email,Toast.LENGTH_LONG).show();
        FirebaseAuth.getInstance().signOut();
//        FirebaseUser users=FirebaseAuth.getInstance().getCurrentUser();

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent=new Intent(ProfileActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }
                });

    }
}
