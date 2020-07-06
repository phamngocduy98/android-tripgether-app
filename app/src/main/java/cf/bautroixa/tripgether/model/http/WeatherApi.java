package cf.bautroixa.tripgether.model.http;

import com.fasterxml.jackson.annotation.JsonIgnore;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WeatherApi {
    private static WeatherApi instance = null;
    Retrofit retrofit;
    WeatherService weatherService;

    private WeatherApi() {
        this.retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        this.weatherService = retrofit.create(WeatherService.class);
    }

    public static WeatherApi getInstance() {
        synchronized (HttpRequest.class) {
            if (instance == null) {
                instance = new WeatherApi();
            }
            return instance;
        }
    }

    public WeatherService getWeatherService() {
        return weatherService;
    }

    public interface WeatherService {
        @GET("/data/2.5/weather?lang=vi&units=metric")
        Call<WeatherApiRes> getWeather(@Query("lat") double latitude, @Query("lon") double longitude, @Query("appid") String token);
    }

    public static class WeatherApiRes {
        public int id, cod, visibility;
        public String name, base;
        public Coord coord;
        /**
         * dt: timestamp when data is updated
         */
        public long dt;
        public Main main;
        public Weather[] weather;
        @JsonIgnore
        public Ignore wind, clouds, sys, timezone, rain;
    }

    public static class Coord {
        public double lat, lon;
    }

    public static class Main {
        public double temp, temp_min, temp_max, feels_like;
        public int pressure, humidity;
        public char sea_level;
    }

    public static class Weather {
        public int id;
        public String main, description, icon;
    }

    public static class Ignore {

    }
}
