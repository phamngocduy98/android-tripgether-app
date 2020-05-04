package cf.bautroixa.maptest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;

import cf.bautroixa.maptest.auth.LoginActivity;
import cf.bautroixa.maptest.data.RequestCodes;
import cf.bautroixa.maptest.data.SharedPrefs;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.theme.OneDialog;
import cf.bautroixa.maptest.utils.DarkModeHelper;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";
    private SharedPreferences sharedPref;
    private String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ArrayList<String> essentialPermissions = new ArrayList<>(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION));
    private ArrayList<String> neededPermissions = new ArrayList<>();

    public SplashScreenActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(DarkModeHelper.androidNightModes.get(sharedPref.getInt(SharedPrefs.DARK_MODE, DarkModeHelper.SYSTEM_MODE)));

        if (!checkAllPermissions()) {
            requestPermissions(permissions, RequestCodes.ALL_PERMISSIONS);
        } else {
            onPermissionGranted();
        }
    }

    private void onPermissionGranted() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            MainAppManager manager = MainAppManager.getInstance();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1000);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }
    }

    private boolean checkAllPermissions() {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestEssentialPermission() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                OneDialog permissionDialog = new OneDialog.Builder().buttonClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(neededPermissions.toArray(new String[0]), RequestCodes.ESSENTIAL_PERMISSION);
                        dialog.dismiss();
                    }
                }).build();
                permissionDialog.show(getSupportFragmentManager(), "request ESSENTIAL_PERMISSION");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.ESSENTIAL_PERMISSION) {
            // request only essential permission
            ArrayList<String> newNeededPermission = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    newNeededPermission.add(neededPermissions.get(i));
                }
            }
            if (newNeededPermission.size() > 0) {
                neededPermissions = newNeededPermission;
                requestEssentialPermission();
            } else {
                onPermissionGranted();
            }
        }
        if (requestCode == RequestCodes.ALL_PERMISSIONS) {
            // request all permission
            neededPermissions.clear();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED && essentialPermissions.contains(permissions[i])) {
                    neededPermissions.add(permissions[i]);
                }
            }
            if (neededPermissions.size() > 0) {
                requestEssentialPermission();
            } else {
                onPermissionGranted();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }
}
