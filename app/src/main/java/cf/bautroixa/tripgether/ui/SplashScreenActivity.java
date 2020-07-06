package cf.bautroixa.tripgether.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.judemanutd.autostarter.AutoStartPermissionHelper;

import java.util.ArrayList;
import java.util.Arrays;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.constant.RequestCodes;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.sharedpref.SPDarkMode;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefHelper;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefKeys;
import cf.bautroixa.tripgether.services.work.WorkManagerHelper;
import cf.bautroixa.tripgether.ui.auth.LoginActivity;
import cf.bautroixa.tripgether.ui.auth.PhoneVerificationActivity;
import cf.bautroixa.tripgether.ui.theme.OneDialog;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";
    private SharedPreferences sharedPref;
    private String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS};
    private ArrayList<String> essentialPermissions = new ArrayList<>(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION));
    private ArrayList<String> neededPermissions = new ArrayList<>();

    public SplashScreenActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        AutoStartPermissionHelper autoStart = AutoStartPermissionHelper.getInstance();
        sharedPref = SharedPrefHelper.getSharedPreferences(this);
//        SharedPrefHelper.setAutoStartGranted(sharedPref, false);
        if (autoStart.isAutoStartPermissionAvailable(this) && !SharedPrefHelper.isAutoStartGranted(sharedPref)) {
            OneDialog.builder().title(R.string.dialog_title_permission_required)
                    .message(R.string.dialog_message_auto_start_permission)
                    .enableNegativeButton(true).posBtnText(R.string.btn_allow).negBtnText(R.string.btn_never_show_again)
                    .buttonClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                // ok button
                                autoStart.getAutoStartPermission(SplashScreenActivity.this);
                            } else {
                                start();
                            }
                            SharedPrefHelper.setAutoStartGranted(sharedPref, true);
                            dialog.dismiss();
                        }
                    }).show(getSupportFragmentManager(), "auto start");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SharedPrefHelper.isAutoStartGranted(sharedPref)) start();
    }

    private void start() {
        AppCompatDelegate.setDefaultNightMode(SPDarkMode.androidNightModes.get(sharedPref.getInt(SharedPrefKeys.SETTING_DARK_MODE_TYPE, SPDarkMode.SYSTEM_MODE)));

        // CheckpointReminder
        if (SharedPrefHelper.isCheckpointReminderOn(sharedPref)) {
            WorkManagerHelper.startScheduleCheckpointReminder(this);
        }

        if (!checkAllPermissions()) {
            requestPermissions(permissions, RequestCodes.ALL_PERMISSIONS);
        } else {
            onPermissionGranted();
        }
    }

    private void next() {
        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            ModelManager manager = ModelManager.getInstance(SplashScreenActivity.this);
            manager.getCurrentUser().attachListener(this, new Document.OnValueChangedListener<User>() {
                @Override
                public void onValueChanged(@NonNull User user) {
                    if (user.isAvailable()) {
                        manager.getCurrentUser().removeOnNewValueListener(this);
                        if (user.getPhoneNumber() != null && user.getPhoneNumber().length() > 5) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1000);
                        } else {
                            Intent intent = new Intent(SplashScreenActivity.this, PhoneVerificationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            });
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

    private void onPermissionGranted() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder.addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                next();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(SplashScreenActivity.this, RequestCodes.LOCATION_REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.LOCATION_REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(SplashScreenActivity.this, "OK, Bạn đã sắn sàng sử dụng tripgether", Toast.LENGTH_LONG).show();
                next();
            } else {
                Toast.makeText(SplashScreenActivity.this, "Ứng dụng cần quyền truy cập GPS để theo dõi vị trí của bạn", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
