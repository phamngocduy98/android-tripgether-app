package cf.bautroixa.tripgether.ui.bottomspace;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.http.WeatherApi;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.ui.helpers.ImageHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomWeatherFragment extends Fragment {
    private static final String TAG = "WeatherWidget";
    ModelManager manager;
    WeatherApi.WeatherService weatherApi;

    TextView tvCurrentTemp, tvMinMaxTemp, tvLocation, tvTime, tvCondition, tvRain;
    ImageView imgCondition;
    WeatherApi.WeatherApiRes weather;

    public BottomWeatherFragment() {
        weatherApi = WeatherApi.getInstance().getWeatherService();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    private void updateWeather() {
        if (!manager.isLoggedIn()) return;
        tvTime.setText("Đang cập nhật ...");
        User currentUser = manager.getCurrentUser();
        if (currentUser.getCurrentCoord() == null) return; // TODO: this is temp fix
        weatherApi.getWeather(currentUser.getCurrentCoord().getLatitude(), currentUser.getCurrentCoord().getLongitude(), getString(R.string.config_open_weather_map_api_appid)).enqueue(new Callback<WeatherApi.WeatherApiRes>() {
            @Override
            public void onResponse(Call<WeatherApi.WeatherApiRes> call, Response<WeatherApi.WeatherApiRes> response) {
                if (call.isExecuted() && response.isSuccessful()) {
                    weather = response.body();
                    updateView();
                }
            }

            @Override
            public void onFailure(Call<WeatherApi.WeatherApiRes> call, Throwable t) {
                Log.e(TAG, t.getMessage());
                tvTime.setText("Lỗi mạng!");
                t.printStackTrace();
            }
        });
    }

    public void updateView() {
        if (weather == null) {
            updateWeather();
            return;
        }
        ImageHelper.loadImage("https://openweathermap.org/img/wn/" + weather.weather[0].icon + "@2x.png", imgCondition, 50, 50);
        tvCurrentTemp.setText(String.format("%.0f°C", weather.main.temp));
        tvMinMaxTemp.setText(String.format("Cảm giác như %.1f°C", weather.main.feels_like));
//        (%.1f°C~%.1f°C), weather.main.temp_min, weather.main.temp_max));
        tvCondition.setText(weather.weather[0].description);
        tvRain.setText(String.format("Độ ẩm: %d %%", weather.main.humidity));
        tvLocation.setText(weather.name);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(weather.dt * 1000);
        tvTime.setText(DateFormatter.format(calendar.getTime()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.widget_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCurrentTemp = view.findViewById(R.id.tv_curr_temp_widget_weather);
        tvMinMaxTemp = view.findViewById(R.id.tv_min_max_temp_widget_weather);
        tvLocation = view.findViewById(R.id.tv_location_widget_weather);
        tvTime = view.findViewById(R.id.tv_last_update_time_widget_weather);
        tvCondition = view.findViewById(R.id.tv_condition_widget_weather);
        tvRain = view.findViewById(R.id.tv_rain_precipitation_widget_weather);
        imgCondition = view.findViewById(R.id.iv_condition_widget_weather);

        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWeather();
            }
        });
        updateWeather();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (weather == null) {
            updateWeather();
        }
    }
}
