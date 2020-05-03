package cf.bautroixa.maptest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import cf.bautroixa.maptest.auth.ChangePasswordActivity;
import cf.bautroixa.maptest.auth.DetailProfileActivity;
import cf.bautroixa.maptest.auth.LoginActivity;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.settings.SettingActivity;
import cf.bautroixa.maptest.theme.OneAppbarFragment;
import cf.bautroixa.maptest.theme.OneDialog;
import cf.bautroixa.maptest.utils.AlarmHelper;
import cf.bautroixa.maptest.utils.ImageHelper;

public class TabProfileFragment extends OneAppbarFragment {
    GoogleSignInClient mGoogleSignInClient;
    private MainAppManager manager;
    private SharedPreferences sharedPref;

    private TextView tvUserName;
    private ImageView imgAvatar;

    private Switch switchService;

    public TabProfileFragment() {
        manager = MainAppManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_profile, container, false);
        imgAvatar = view.findViewById(R.id.appbar_img_avatar);
        switchService = view.findViewById(R.id.switch_toggle_service);
        LinearLayout mPersonalInformationLinear = view.findViewById(R.id.ln_personal_information);
        LinearLayout mChangePasswordLinear = view.findViewById(R.id.ln_change_password);
        LinearLayout mLogoutLinear = view.findViewById(R.id.ln_logout);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
            }
        });
        mPersonalInformationLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DetailProfileActivity.class);
                startActivity(intent);
            }
        });
        mChangePasswordLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
        mLogoutLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OneDialog.Builder().title(R.string.dialog_title_confirm_logout).message(R.string.dialog_message_logout)
                        .enableNegativeButton(true)
                        .buttonClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    logout();
                                }
                                dialog.dismiss();
                            }
                        }).show(getChildFragmentManager(), "logout dialog");
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getContext() == null) return;
        sharedPref = getContext().getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        switchService.setChecked(sharedPref.getBoolean("SERVICE_ON", false));
        switchService.setText(switchService.isChecked() ? R.string.switch_toggle_service_on : R.string.switch_toggle_service_off);
        switchService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref.edit().putBoolean("SERVICE_ON", switchService.isChecked()).commit();
                if (switchService.isChecked()) {
                    AlarmHelper.turnOn(Objects.requireNonNull(getActivity()));
                    switchService.setText(R.string.switch_toggle_service_on);
                } else {
                    AlarmHelper.turnOff(Objects.requireNonNull(getActivity()));
                    switchService.setText(R.string.switch_toggle_service_off);
                }
            }
        });

        String googleClientId = "703604566706-upp9g9rtcdh3adrflqcgddt4p712jh27.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
    }

    @Override
    public void onResume() {
        super.onResume();
        String avatarUrl = "";
        if (manager.isLoggedIn()) {
            if (!avatarUrl.equals(manager.getCurrentUser().getAvatar())) {
                ImageHelper.loadImage(manager.getCurrentUser().getAvatar(), imgAvatar, 100, 100);
            }
            setTitle(manager.getCurrentUser().getName());
        }
    }

    private void logout() {
        manager.logout();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });

    }
}
