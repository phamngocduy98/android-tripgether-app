package cf.bautroixa.maptest;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.network_io.AppRequest;
import cf.bautroixa.maptest.network_io.HttpRequest;
import cf.bautroixa.maptest.theme.FullScreenDialogFragment;
import cf.bautroixa.maptest.types.APILocation;
import cf.bautroixa.maptest.utils.DateFormatter;

public class CreateTripCheckpointDialogFragment extends FullScreenDialogFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText editLocation, editTime, editName;
    private ImageView btnSearchLocation;
    private Button btnCancel, btnOk;

    private LatLng selectedLatLng = new LatLng(21.0245, 105.84117);
    private Calendar selectedTime = Calendar.getInstance();

    private static View view;

    private OnCheckpointSetListener onCheckpointSetListener;

    public interface OnCheckpointSetListener {
        void onCheckpointSet(Checkpoint checkpoint);
    }

    public CreateTripCheckpointDialogFragment(OnCheckpointSetListener onCheckpointSetListener) {
        this.onCheckpointSetListener = onCheckpointSetListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_dialog_checkpoint_trip_create, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        final SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map_checkpoint_select);
        mapFragment.getMapAsync(this);

        editLocation = view.findViewById(R.id.edit_location_checkpoint);
        btnSearchLocation = view.findViewById(R.id.btn_location_checkpoint_search);
        editName = view.findViewById(R.id.edit_name_dialog_checkpoint_trip_create);
        editTime = view.findViewById(R.id.edit_time_checkpoint);
        btnCancel = view.findViewById(R.id.btn_cancel_checkpoint);
        btnOk = view.findViewById(R.id.btn_add_checkpoint);

        btnSearchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = editLocation.getText().toString();
                AppRequest.getGeocodingLatLng(getContext(), query, new HttpRequest.Callback<APILocation>() {
                    @Override
                    public void onResponse(final APILocation response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(response.getLatLng(), 14));
                                editLocation.setHint(response.getAddress());
                                editLocation.setText("");
                            }
                        });
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                });
            }
        });
        final int hour = selectedTime.get(Calendar.HOUR_OF_DAY);
        final int minute = selectedTime.get(Calendar.MINUTE);
        final int year = selectedTime.get(Calendar.YEAR);
        final int month = selectedTime.get(Calendar.MONTH);
        final int dayOfMonth = selectedTime.get(Calendar.DAY_OF_MONTH);

        editTime.setText(DateFormatter.format(selectedTime));
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                        TimePickerDialog mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                selectedTime.set(year, month, dayOfMonth, hourOfDay, minute);
                                editTime.setText(DateFormatter.format(selectedTime));
                            }
                        }, hour, minute, true);
                        mTimePicker.show();
                    }
                }, year, month, dayOfMonth).show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCheckpointSetListener != null) {
                    onCheckpointSetListener.onCheckpointSet(new Checkpoint(editName.getText().toString(), new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude), editLocation.getHint().toString(), new Timestamp(selectedTime.getTime())));
                }
                dismiss();
            }
        });

        return view;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        UiSettings uiSetting = mMap.getUiSettings();
        uiSetting.setMapToolbarEnabled(false);
//        uiSetting.setZoomControlsEnabled(false);
//        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 12));
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            }
        });
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng midLatLng = mMap.getCameraPosition().target;
                AppRequest.getGeocodingAddress(getContext(), midLatLng, new HttpRequest.Callback<String>() {
                    @Override
                    public void onResponse(final String response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editLocation.setHint(response);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                });
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.onCheckpointSetListener = null;
    }
}
