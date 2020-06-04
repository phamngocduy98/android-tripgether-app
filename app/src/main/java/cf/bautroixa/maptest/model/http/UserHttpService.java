package cf.bautroixa.maptest.model.http;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.types.UserPublicData;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class UserHttpService {
    public static Task<UserPublicData> getUserPublicData(String userId) {
        final TaskCompletionSource<UserPublicData> taskCompletionSource = new TaskCompletionSource<>();
        HttpRequest.getInstance().getUserService().getUserPublicData(userId).enqueue(new retrofit2.Callback<HttpRequest.APIResponse<UserPublicData>>() {
            @Override
            public void onResponse(retrofit2.Call<HttpRequest.APIResponse<UserPublicData>> call, retrofit2.Response<HttpRequest.APIResponse<UserPublicData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HttpRequest.APIResponse<UserPublicData> resBody = response.body();
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
            public void onFailure(retrofit2.Call<HttpRequest.APIResponse<UserPublicData>> call, Throwable t) {
                taskCompletionSource.setException(new Exception(t.getMessage()));
            }
        });
        return taskCompletionSource.getTask();
    }

    public static Task<UserPublicData> findUser(String email, String phoneNum) {
        final TaskCompletionSource<UserPublicData> taskCompletionSource = new TaskCompletionSource<>();
        HttpRequest.getInstance().getUserService().findUser(email, phoneNum).enqueue(new retrofit2.Callback<HttpRequest.APIResponse<UserPublicData>>() {
            @Override
            public void onResponse(retrofit2.Call<HttpRequest.APIResponse<UserPublicData>> call, retrofit2.Response<HttpRequest.APIResponse<UserPublicData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HttpRequest.APIResponse<UserPublicData> resBody = response.body();
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
            public void onFailure(retrofit2.Call<HttpRequest.APIResponse<UserPublicData>> call, Throwable t) {
                taskCompletionSource.setException(new Exception(t.getMessage()));
            }
        });
        return taskCompletionSource.getTask();
    }

    public static Task<HttpRequest.APIResponse> sendAddFriend(User currentUser, String friendId, String addFriendAction) {
        final TaskCompletionSource<HttpRequest.APIResponse> taskCompletionSource = new TaskCompletionSource<>();
        HttpRequest.getInstance().getUserService().addFriend(currentUser.getId(), friendId, addFriendAction).enqueue(new retrofit2.Callback<HttpRequest.APIResponse>() {
            @Override
            public void onResponse(retrofit2.Call<HttpRequest.APIResponse> call, retrofit2.Response<HttpRequest.APIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskCompletionSource.setResult(response.body());
                } else {
                    taskCompletionSource.setException(new Exception("Network error"));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<HttpRequest.APIResponse> call, Throwable t) {
                taskCompletionSource.setException(new Exception(t.getMessage()));
            }
        });
        return taskCompletionSource.getTask();
    }

    public interface UserService {
        @FormUrlEncoded
        @POST("/getUser")
        Call<HttpRequest.APIResponse<UserPublicData>> getUserPublicData(@Field("uid") String userId);

        @FormUrlEncoded
        @POST("/findUser")
        Call<HttpRequest.APIResponse<UserPublicData>> findUser(@Field("email") @Nullable String email, @Field("phoneNum") @Nullable String phoneNum);

        @FormUrlEncoded
        @POST("/addFriend")
        Call<HttpRequest.APIResponse> addFriend(@Field("uid") String userId, @Field("friendId") String friendId, @Field("action") String action);
    }

    public interface AddFriendActions {
        String REQUEST = "REQUEST";
        String CANCEL = "CANCEL";
        String ACCEPT = "ACCEPT";
        String REJECT = "REJECT";
        String REMOVE = "REMOVE";
    }
}
