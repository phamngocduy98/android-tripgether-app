package cf.bautroixa.maptest;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.network_io.AppRequest;
import cf.bautroixa.maptest.network_io.HttpRequest;
import cf.bautroixa.maptest.theme.FullScreenDialogFragment;
import cf.bautroixa.maptest.types.APILocation;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.NoFilterArrayAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateCheckpointDialogFragment extends FullScreenDialogFragment implements OnMapReadyCallback {
    private static final String TAG = "CreateCheckpointDialog";
    private GoogleMap mMap;
    private EditText editTime, editName;
    private AutoCompleteTextView editLocation;
    private ImageView btnSearchLocation;
    private Button btnCancel, btnOk;

    private LatLng selectedLatLng = new LatLng(21.0245, 105.84117);
    private Calendar selectedTime = Calendar.getInstance();

    private static View view;

    private OnCheckpointSetListener onCheckpointSetListener;

    public interface OnCheckpointSetListener {
        void onCheckpointSet(Checkpoint checkpoint);
    }

    public CreateCheckpointDialogFragment(OnCheckpointSetListener onCheckpointSetListener) {
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
            view = inflater.inflate(R.layout.fragment_dialog_create_checkpoint, container, false);
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

        editLocation.setThreshold(1);
        final ArrayList<Point> points = new ArrayList<>();
        final ArrayAdapter adapterLocations = new NoFilterArrayAdapter(getContext(), android.R.layout.simple_list_item_1, new ArrayList<String>());
        editLocation.setAdapter(adapterLocations);
        editLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Point selectedPoint = points.get(position);
                selectedLatLng = new LatLng(selectedPoint.latitude(), selectedPoint.longitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 18));
            }
        });
        editLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.config_mapbox_map_api_key))
                        .query(s.toString())
                        .build();
                mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
                    @Override
                    public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {
                            // Log the first results Point.
                            adapterLocations.clear();
                            points.clear();
                            for (CarmenFeature feature : results) {
                                adapterLocations.add(feature.placeName());
                                points.add(feature.center());
                                Log.d(TAG, "address = " + feature.placeName());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
