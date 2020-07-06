package cf.bautroixa.tripgether.ui.dialogs;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;

import java.util.Collections;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.SosRequest;
import cf.bautroixa.tripgether.presenter.SosPresenter;
import cf.bautroixa.tripgether.presenter.impl.SosPresenterImpl;
import cf.bautroixa.tripgether.ui.theme.FullScreenDialogFragment;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class SosRequestEditDialogFragment extends FullScreenDialogFragment implements SosPresenter.View {
    private static final String TAG = "SosRequestEditDialogFragment";
    SosPresenterImpl sosPresenter;
    Bitmap selectedImageBitmap;
    Uri selectedImageUri;
    private ImageView imgImage;
    private ImageButton btnBack;
    private Button btnOK, btnCancel;
    private EditText editDesc;
    private RadioGroup rgLever;
    private RadioButton radioHigh, radioMedium, radioLow;
    private ProgressDialog loadingDialog;
    private int selectedLever;
    private SosRequest sosRequest;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_sos_request_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgImage = view.findViewById(R.id.img_sos_frag_send_sos);
        btnBack = view.findViewById(R.id.btn_back_sos_edit);
        editDesc = view.findViewById(R.id.edit_desc_frag_send_sos);
        btnOK = view.findViewById(R.id.btn_send_frag_send_sos);
        btnCancel = view.findViewById(R.id.btn_cancel_frag_send_sos);
        rgLever = view.findViewById(R.id.rg_lever);
        radioHigh = view.findViewById(R.id.radio_high);
        radioMedium = view.findViewById(R.id.radio_medium);
        radioLow = view.findViewById(R.id.radio_low);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        imgImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OneChooseImageDialog chooseImageDialog = new OneChooseImageDialog();
                chooseImageDialog.setOnImagePickedListener(new OneChooseImageDialog.OnImagePickedListener() {
                    @Override
                    public void onPicked(@Nullable Uri uri, Bitmap bitmap) {
                        selectedImageBitmap = bitmap;
                        selectedImageUri = uri;
                        imgImage.setImageBitmap(selectedImageBitmap);
                    }
                });
                chooseImageDialog.show(getChildFragmentManager(), "select image");
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sosPresenter = new SosPresenterImpl(this, requireActivity());
    }

    int getLeverFromButtonId(int btnId) {
        switch (btnId) {
            case R.id.radio_high:
                return SosRequest.SosLever.HIGH;
            case R.id.radio_low:
                return SosRequest.SosLever.LOW;
            default:
                return SosRequest.SosLever.MEDIUM;
        }
    }

    void selectButtonFromLever(int lever) {
        switch (lever) {
            case SosRequest.SosLever.HIGH:
                radioHigh.toggle();
                break;
            case SosRequest.SosLever.LOW:
                radioLow.toggle();
                break;
            default:
                radioMedium.toggle();
        }
    }

    @Override
    public void updateView(SosRequest sosRequest) {
        if (sosRequest != null && !sosRequest.isResolved()) {
            imgImage.post(new Runnable() {
                @Override
                public void run() {
                    ImageHelper.loadImage(sosRequest.getImageUrl(), imgImage, imgImage.getMeasuredWidth(), imgImage.getMeasuredHeight());
                }
            });
            editDesc.setText(sosRequest.getDescription());
            selectButtonFromLever(sosRequest.getLever());
            btnOK.setText("Cập nhật");
            btnCancel.setText("Đã giải quyết (Xóa)");
            btnCancel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_black_24dp, 0, 0, 0);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sosPresenter.resolveSos();
                }
            });
        }

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLever = getLeverFromButtonId(rgLever.getCheckedRadioButtonId());
                sosPresenter.updateSos(sosRequest == null || sosRequest.isResolved(), selectedLever, editDesc.getText().toString(), selectedImageUri, selectedImageBitmap);

            }
        });
    }

    @Override
    public void staticMap(GeoPoint myLocation) {
        Point myLocationPoint = Point.fromLngLat(myLocation.getLongitude(), myLocation.getLatitude());
        StaticMarkerAnnotation myLocationMarker = StaticMarkerAnnotation.builder().lnglat(myLocationPoint).iconUrl("https://sites.google.com/site/masoibot/user/marker_my_location_50x50.png").build();

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

    @Override
    public void onSending(String text) {
        btnCancel.setEnabled(false);
        btnOK.setEnabled(false);
        loadingDialog = LoadingDialogHelper.create(requireContext(), "Đang cập nhật yêu cầu...");
    }

    @Override
    public void onSuccess() {
        btnCancel.setEnabled(true);
        btnOK.setEnabled(true);
        if (loadingDialog != null) loadingDialog.dismiss();
        dismiss();
    }

    @Override
    public void onFailed(String reason) {
        btnCancel.setEnabled(true);
        btnOK.setEnabled(true);
        Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG).show();
        if (loadingDialog != null) loadingDialog.dismiss();
        dismiss();
    }
}
