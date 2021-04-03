package cf.bautroixa.tripgether.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.http.HttpRequest;
import cf.bautroixa.tripgether.model.http.MapboxHttpService;
import cf.bautroixa.tripgether.model.types.APILocation;
import cf.bautroixa.tripgether.model.types.GeocodingResult;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.tripgether.utils.ui_utils.NoFilterArrayAdapter;
import cf.bautroixa.ui.dialogs.FullScreenDialogFragment;
import cf.bautroixa.ui.dialogs.OneDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckpointEditDialogFragment extends FullScreenDialogFragment implements OnMapReadyCallback {
    private static final String TAG = "CreateCheckpointDialog";
    private static final String ARG_CHECKPOINT_ID = "checkpointId";
    private static final String ARG_CHECKPOINT_NAME = "checkpointName";
    private static final String ARG_CHECKPOINT_LOCATION = "checkpointLocation";
    private static final String ARG_CHECKPOINT_TIME = "checkpointTime";
    private static final String ARG_CHECKPOINT_LATITUDE = "checkpointLatitude";
    private static final String ARG_CHECKPOINT_LONGITUDE = "checkpointLongitude";
    boolean isMapLoaded = false;
    private Checkpoint checkpoint;
    private LatLng selectedLatLng = new LatLng(21.0245, 105.84117);
    private final Calendar selectedTime = Calendar.getInstance();
    private OnCheckpointSetListener onCheckpointSetListener;
    private final OnDeleteCheckpointListener onDeleteCheckpointListener;

    private GoogleMap mMap;
    private EditText editTime, editName;
    private AutoCompleteTextView editLocation;
    private Button btnCancel, btnOk;
    private MapView mapView;
    private ImageButton btnClearLocation, btnBack, btnMyLocation;

    public interface OnCheckpointSetListener {
        void onCheckpointSet(Checkpoint checkpoint);
    }

    public CheckpointEditDialogFragment(OnCheckpointSetListener onCheckpointSetListener, @Nullable OnDeleteCheckpointListener onDeleteCheckpointListener) {
        this.onCheckpointSetListener = onCheckpointSetListener;
        this.onDeleteCheckpointListener = onDeleteCheckpointListener;
    }

    public static CheckpointEditDialogFragment newInstance(Checkpoint checkpoint, OnCheckpointSetListener onCheckpointSetListener, OnDeleteCheckpointListener onDeleteCheckpointListener) {
        Bundle args = new Bundle();
        if (checkpoint != null) {
            args.putString(ARG_CHECKPOINT_ID, checkpoint.getId());
            args.putString(ARG_CHECKPOINT_NAME, checkpoint.getName());
            args.putString(ARG_CHECKPOINT_LOCATION, checkpoint.getLocation());
            args.putParcelable(ARG_CHECKPOINT_TIME, checkpoint.getTime());
            args.putDouble(ARG_CHECKPOINT_LATITUDE, checkpoint.getCoordinate().getLatitude());
            args.putDouble(ARG_CHECKPOINT_LONGITUDE, checkpoint.getCoordinate().getLongitude());
        }
        CheckpointEditDialogFragment fragment = new CheckpointEditDialogFragment(onCheckpointSetListener, onDeleteCheckpointListener);
        fragment.setArguments(args);
        return fragment;
    }

    public static CheckpointEditDialogFragment newInstance(OnCheckpointSetListener onCheckpointSetListener) {
        Bundle args = new Bundle();
        CheckpointEditDialogFragment fragment = new CheckpointEditDialogFragment(onCheckpointSetListener, null);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(getContext());
                locationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (!task.isSuccessful()) return;
                        Location location = task.getResult();
                        if (location == null) return;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18));
                    }
                });
            }
        });

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
        editLocation.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String query = editLocation.getText().toString();
                MapboxHttpService.getGeocodingLatLng(getContext(), query, new HttpRequest.Callback<APILocation>() {
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
                return true;
            }
        });
        editLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1) {
                    btnClearLocation.setVisibility(View.GONE);
                    return;
                }
                btnClearLocation.setVisibility(View.VISIBLE);
                MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.config_mapbox_map_api_key))
                        .query(s.toString())
                        .build();
                mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<GeocodingResponse> call, @NotNull Response<GeocodingResponse> response) {
                        if (response.body() == null) return;
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
                    public void onFailure(@NotNull Call<GeocodingResponse> call, @NotNull Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnClearLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLocation.setText("");
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
                    onCheckpointSetListener.onCheckpointSet(new Checkpoint(editName.getText().toString(), selectedLatLng.latitude, selectedLatLng.longitude, editLocation.getHint().toString(), new Timestamp(selectedTime.getTime())));
                }
                dismiss();
            }
        });
        if (getArguments() != null) {
            String checkpointId = getArguments().getString(ARG_CHECKPOINT_ID, "");
            if (checkpointId.length() > 0) {
                btnOk.setText(R.string.btn_update);
                btnCancel.setText(R.string.btn_delete);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new OneDialog.Builder().title(R.string.dialog_title_confirm_delete_checkpoint)
                                .message(R.string.dialog_message_confirm_delete_checkpoint)
                                .enableNegativeButton(true)
                                .buttonClickListener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            onDeleteCheckpointListener.onCheckpointDeleted();
                                        }
                                        dialog.dismiss();
                                        dismiss();
                                    }
                                }).show(getChildFragmentManager(), "delete checkpoint");
                    }
                });
            }

            double lat = getArguments().getDouble(ARG_CHECKPOINT_LATITUDE, selectedLatLng.latitude);
            double lng = getArguments().getDouble(ARG_CHECKPOINT_LONGITUDE, selectedLatLng.longitude);
            selectedLatLng = new LatLng(lat, lng);
            if (isMapLoaded) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 12));

            Timestamp time = getArguments().getParcelable(ARG_CHECKPOINT_TIME);
            if (time != null) selectedTime.setTime(time.toDate());

            editName.setText(getArguments().getString(ARG_CHECKPOINT_NAME, ""));
            editLocation.setHint(getArguments().getString(ARG_CHECKPOINT_LOCATION, ""));
            editTime.setText(DateFormatter.format(selectedTime));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_create_checkpoint, container, false);
        mapView = view.findViewById(R.id.map_checkpoint_select);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        editLocation = view.findViewById(R.id.edit_search_location_checkpoint);
        btnClearLocation = view.findViewById(R.id.btn_clear_edit_search);
        btnBack = view.findViewById(R.id.btn_back_dialog_create_checkpoint);
        btnMyLocation = view.findViewById(R.id.btn_my_location_dialog_create_checkpoint);
        editName = view.findViewById(R.id.edit_name_dialog_checkpoint_trip_create);
        editTime = view.findViewById(R.id.edit_time_checkpoint);
        btnCancel = view.findViewById(R.id.btn_cancel_checkpoint);
        btnOk = view.findViewById(R.id.btn_add_checkpoint);


        return view;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapLoaded = true;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        UiSettings uiSetting = mMap.getUiSettings();
        uiSetting.setMapToolbarEnabled(false);
//        uiSetting.setZoomControlsEnabled(false);
//        mMap.setMyLocationEnabled(true);
        try {
            int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                if (!googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.maps_night))) {
                    Log.e(TAG, "Style parsing failed.");
                }
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
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
                selectedLatLng = midLatLng;
                MapboxHttpService.getGeocodingAddress(requireContext(), midLatLng.latitude, midLatLng.longitude).addOnCompleteListener(new OnCompleteListener<GeocodingResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GeocodingResult> task) {
                        if (task.isSuccessful()) {
                            GeocodingResult geocodingResult = task.getResult();
                            assert geocodingResult != null;
                            editLocation.setHint(geocodingResult.getFullPlaceName());
                            editLocation.setText("");
                        }
                    }
                });
            }
        });
    }

    public interface OnDeleteCheckpointListener {
        void onCheckpointDeleted();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.onCheckpointSetListener = null;
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
