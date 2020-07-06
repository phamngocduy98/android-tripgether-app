package cf.bautroixa.maptest.ui.dialogs;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;

import java.util.Collections;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.objects.Message;
import cf.bautroixa.maptest.model.firestore.objects.SosRequest;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.ui.theme.OneBottomSheetDialog;
import cf.bautroixa.maptest.utils.ui_utils.ImageHelper;

public class SosRequestViewDialogFragment extends OneBottomSheetDialog {
    private static final String TAG = "SosRequestEditDialogFragment";
    private static final String ARG_ID = "id";
    User sosRequestUser;
    String sosRequestUserId;
    private ModelManager manager;
    private TextView tvUserName, tvLever, tvDesc;
    //    TextView tvUserLocation;
    private ImageView imgLever, imgAvatar, imgImage;
    Button btnClose, btnIamComing;
    private String[] leverStrings;

    public SosRequestViewDialogFragment() {
    }

    public static SosRequestViewDialogFragment newInstance(String userId) {
        Bundle args = new Bundle();
        args.putString(ARG_ID, userId);
        SosRequestViewDialogFragment fragment = new SosRequestViewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sosRequestUserId = getArguments().getString(ARG_ID);
            manager.getBaseUsersManager().requestGet(sosRequestUserId).addOnCompleteListener(new OnCompleteListener<User>() {
                @Override
                public void onComplete(@NonNull Task<User> task) {
                    if (task.isSuccessful()) {
                        sosRequestUser = task.getResult();
                        updateView(sosRequestUser);
                    }
                }
            });
        }
        leverStrings = getResources().getStringArray(R.array.sos_lever);

    }

    void updateView(User user) {
        SosRequest sosRequest = user.getSosRequest();
        ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
        tvUserName.setText(user.getName());
//        tvUserLocation.setText("Vị trí hiện tại: Gần " + user.getCurrentLocation());
        tvLever.setText(leverStrings[sosRequest.getLever()]);
        tvDesc.setText(sosRequest.getDescription());

        if (sosRequest.getImageUrl().length() == 0) {
            staticMap(user.getCurrentCoord());
        } else {
            imgImage.post(new Runnable() {
                @Override
                public void run() {
                    ImageHelper.loadImage(sosRequest.getImageUrl(), imgImage, imgImage.getMeasuredWidth(), imgImage.getMeasuredHeight());
                }
            });
        }

        btnIamComing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tripDiscussionId = manager.getCurrentTrip().getDiscussionRef().getId();
                manager.getDiscussionsManagers().get(tripDiscussionId).getMessagesManager().create(new Message(manager.getCurrentUserRef(), "Tôi đang tới giúp " + sosRequestUser.getName()));

            }
        });
    }

    public void staticMap(GeoPoint userLocation) {
        Point myLocationPoint = Point.fromLngLat(userLocation.getLongitude(), userLocation.getLatitude());
        StaticMarkerAnnotation myLocationMarker = StaticMarkerAnnotation.builder().lnglat(myLocationPoint).name(StaticMapCriteria.SMALL_PIN).color(255, 0, 0).build();

        int nightModeFlags = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        imgImage.post(new Runnable() {
            @Override
            public void run() {
                MapboxStaticMap staticImage = MapboxStaticMap.builder()
                        .accessToken(getString(R.string.config_mapbox_map_api_key))
                        .styleId((nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ? StaticMapCriteria.DARK_STYLE : StaticMapCriteria.LIGHT_STYLE)
                        .cameraPoint(myLocationPoint)
                        .cameraZoom(16)
                        .width(imgImage.getMeasuredWidth() / 2)
                        .height(imgImage.getMeasuredHeight() / 2)
                        .staticMarkerAnnotations(Collections.singletonList(myLocationMarker))
                        .build();

                String url = staticImage.url().toString();
                Picasso.get().load(url).placeholder(R.drawable.ic_photo).into(imgImage);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_sos_request_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgImage = view.findViewById(R.id.img_images_dialog_sos_request_view);
        imgAvatar = view.findViewById(R.id.img_avatar_dialog_sos_request_view);
        tvUserName = view.findViewById(R.id.tv_user_name_dialog_sos_request_view);
//        tvUserLocation = view.findViewById(R.id.tv_user_location_dialog_sos_request_view);
        imgLever = view.findViewById(R.id.img_lever_sos_request_view);
        tvLever = view.findViewById(R.id.tv_lever_dialog_sos_request_view);
        tvDesc = view.findViewById(R.id.tv_desc_dialog_sos_request_view);
        btnClose = view.findViewById(R.id.btn_close_dialog_sos_request_view);
        btnIamComing = view.findViewById(R.id.btn_i_am_coming_dialog_sos_request_view);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
