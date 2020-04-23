package cf.bautroixa.maptest.network_io;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public class HttpRequest {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final MediaType FORM = MediaType.parse("multipart/form-data");

    private static HttpRequest httpRequest = null;
    Retrofit retrofit;
    TripService tripService;
    OkHttpClient client;

    private HttpRequest() {
        this.client = new OkHttpClient();
        this.retrofit = new Retrofit.Builder()
                .baseUrl("https://us-central1-tripgether-b135a.cloudfunctions.net/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        this.tripService = retrofit.create(TripService.class);
    }

    public static HttpRequest getInstance() {
        synchronized (HttpRequest.class) {
            if (httpRequest == null) {
                httpRequest = new HttpRequest();
            }
            return httpRequest;
        }
    }

    public TripService getTripService() {
        return tripService;
    }

    public void sendPostJsonRequest(String url, String json, okhttp3.Callback callback){
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
//        return response.body().string();

    }

    public interface TripService {
        @FormUrlEncoded
        @POST("/joinTrip")
        Call<APIResponse> joinTrip(@Field("uid") String userId, @Field("tripId") String tripId);
    }

    public static class APIResponse {
        public boolean success;
        public String reason;
    }

    public void sendPostFormRequest(String url, RequestBody formBody, okhttp3.Callback callback){
        Request request = new Request.Builder().url(url).post(formBody).build();
        client.newCall(request).enqueue(callback);
//        return response.body().string();
    }

    public void sendGetRequest(String url, okhttp3.Callback callback){
        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(callback);
//        return response.body().string();
    }

    public interface Callback<T> {
        void onResponse(T response);
        void onFailure(String reason);
    }
}
