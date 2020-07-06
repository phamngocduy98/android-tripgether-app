package cf.bautroixa.maptest.model.http;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class HttpRequest {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final MediaType FORM = MediaType.parse("multipart/form-data");

    private static HttpRequest httpRequest = null;
    OkHttpClient client;

    private HttpRequest() {
        this.client = new OkHttpClient();
    }

    public static HttpRequest getInstance() {
        synchronized (HttpRequest.class) {
            if (httpRequest == null) {
                httpRequest = new HttpRequest();
            }
            return httpRequest;
        }
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
