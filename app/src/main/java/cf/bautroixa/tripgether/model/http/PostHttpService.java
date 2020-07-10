package cf.bautroixa.tripgether.model.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;

import cf.bautroixa.tripgether.model.repo.objects.PostPublic;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class PostHttpService {
    public static Task<ArrayList<PostPublic>> getPosts() {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<ArrayList<PostPublic>>>() {
            @Override
            public Task<ArrayList<PostPublic>> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<ArrayList<PostPublic>> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getPostService().getPosts("Bearer " + task.getResult()).enqueue(new retrofit2.Callback<HttpService.APIResponse<ArrayList<PostPublic>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<HttpService.APIResponse<ArrayList<PostPublic>>> call, retrofit2.Response<HttpService.APIResponse<ArrayList<PostPublic>>> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse<ArrayList<PostPublic>> resBody = response.body();
                            if (resBody.success) {
                                taskCompletionSource.setResult(response.body().data);
                            } else {
                                taskCompletionSource.setException(new Exception(resBody.reason));
                            }
                        } else {
                            taskCompletionSource.setException(new Exception("Network error"));
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<HttpService.APIResponse<ArrayList<PostPublic>>> call, Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });
    }

    public static Task<Void> likePost(String postId, @Nullable String commentId, boolean like) {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getPostService().like("Bearer " + task.getResult(), postId, commentId, like).enqueue(new retrofit2.Callback<HttpService.APIResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<HttpService.APIResponse> call, retrofit2.Response<HttpService.APIResponse> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse resBody = response.body();
                            if (resBody.success) {
                                taskCompletionSource.setResult(null);
                            } else {
                                taskCompletionSource.setException(new Exception(resBody.reason));
                            }
                        } else {
                            taskCompletionSource.setException(new Exception("Network error"));
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<HttpService.APIResponse> call, Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });
    }

    public interface PostService {
        @POST("post/getAllPosts")
        Call<HttpService.APIResponse<ArrayList<PostPublic>>> getPosts(@Header("Authorization") String auth);

        @FormUrlEncoded
        @POST("post/likePost")
        Call<HttpService.APIResponse> like(@Header("Authorization") String auth, @Field("postId") @NonNull String postId, @Field("commentId") @Nullable String commentId, @Field("like") boolean like);
    }
}
