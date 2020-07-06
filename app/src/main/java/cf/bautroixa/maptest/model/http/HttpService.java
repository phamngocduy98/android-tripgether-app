package cf.bautroixa.maptest.model.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class HttpService {
    private static HttpService httpService = null;
    Retrofit retrofit;
    TripHttpService.TripService tripService;
    UserHttpService.UserService userService;
    PostHttpService.PostService postService;

    private HttpService() {
        this.retrofit = new Retrofit.Builder()
                .baseUrl("https://asia-east2-tripgether-b135a.cloudfunctions.net/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        this.tripService = retrofit.create(TripHttpService.TripService.class);
        this.userService = retrofit.create(UserHttpService.UserService.class);
        this.postService = retrofit.create(PostHttpService.PostService.class);
    }

    public static HttpService getInstance() {
        synchronized (HttpService.class) {
            if (httpService == null) {
                httpService = new HttpService();
            }
            return httpService;
        }
    }

    public TripHttpService.TripService getTripService() {
        return tripService;
    }

    public UserHttpService.UserService getUserService() {
        return userService;
    }

    public PostHttpService.PostService getPostService() {
        return postService;
    }

    public static Task<String> getToken() {
        final TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            taskCompletionSource.setException(new Exception("User not logged in"));
        }
        mAuth.getCurrentUser().getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    taskCompletionSource.setResult(task.getResult().getToken());
                }
                if (task.getException() != null)
                    taskCompletionSource.setException(task.getException());
            }
        });
        return taskCompletionSource.getTask();
    }

    public static class APIResponse<T extends Object> {
        @JsonProperty("success")
        public boolean success;

        @Nullable
        @JsonProperty("reason")
        public String reason;

        @Nullable
        @JsonProperty("code")
        public String code;

        @Nullable
        @JsonProperty("data")
        public T data;

        public APIResponse() {
        }

        public APIResponse(boolean success, @Nullable String reason, @Nullable T data) {
            this.success = success;
            this.reason = reason;
            this.data = data;
        }
    }
}
