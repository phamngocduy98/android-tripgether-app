package cf.bautroixa.maptest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.utils.BatteryHelper;

public class FriendFragment extends Fragment {
    private static final String TAG = "FriendFragment";
    public static final String ARG_USER_NAME = "user_name";

    private FirebaseFirestore db;
    private User activeUser;
    private OnDrawRouteButtonClickedListener mRouteBtnListener;

    TextView tvName, tvLocation, tvBattery, tvSpeed;
    Button btnCall, btnDirection, btnMessage;
    ImageView imgAvatar;

    int pos;

    public FriendFragment() {
    }

    public static FriendFragment newInstance(String userId) {
        Bundle args = new Bundle();
        args.putString(ARG_USER_NAME, userId);
        FriendFragment fragment = new FriendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDrawRouteButtonClickedListener(OnDrawRouteButtonClickedListener mRouteBtnListener) {
        this.mRouteBtnListener = mRouteBtnListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        db = FirebaseFirestore.getInstance();
        this.activeUser = new User();
        if (bundle != null) {
            String userName = bundle.getString(ARG_USER_NAME);
            db.collection(Collections.USERS).document(userName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        activeUser = documentSnapshot.toObject(User.class);
                        activeUser.setUserName(documentSnapshot.getId());
                        updateView();
                    }
                }
            });
            db.collection(Collections.USERS).document(userName).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    activeUser = documentSnapshot.toObject(User.class);
                    activeUser.setUserName(documentSnapshot.getId());
                    updateView();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friend, container, false);
        imgAvatar = v.findViewById(R.id.img_avatar_frag_friend);
        tvName = v.findViewById(R.id.tv_name_frag_friend);
        tvLocation = v.findViewById(R.id.tv_location_frag_friend);
        tvBattery = v.findViewById(R.id.tv_battery_frag_friend);
        tvSpeed = v.findViewById(R.id.tv_speed_frag_friend);
        btnCall = v.findViewById(R.id.btn_call_frag_friend);
        btnDirection = v.findViewById(R.id.btn_direction_frag_friend);
        btnMessage = v.findViewById(R.id.btn_message_frag_friend);

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + activeUser.getPhoneNumber()));
                startActivity(intent);
            }
        });

        btnDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRouteBtnListener.onClick(activeUser.getUserName(), activeUser.getLatLng());
                Log.d(TAG, "battery = " + BatteryHelper.getBatteryPercentage(getContext()));
            }
        });
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", activeUser.getPhoneNumber(), null)));
            }
        });
        this.updateView();
        return v;
    }

    void updateView() {
        if (getContext() == null && isDetached()) return; // getContext() may return null if fragment detached or not attached
        if (activeUser != null) {
            Picasso.get().load(activeUser.getAvatar()).placeholder(R.drawable.user).into(imgAvatar);
            tvName.setText(this.activeUser.getName());
            tvLocation.setText(this.activeUser.getCurrentLocation());
            tvBattery.setText(this.activeUser.getBattery() + "%");
            tvSpeed.setText(this.activeUser.getSpeed() + " m/s");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRouteBtnListener = null;
    }

    public interface OnDrawRouteButtonClickedListener {
        void onClick(String userName, LatLng latLng);
    }
}
