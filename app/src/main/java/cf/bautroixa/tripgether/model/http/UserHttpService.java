package cf.bautroixa.tripgether.model.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class UserHttpService {
    public static Task<UserPublic> getUserPublicData(final String userId) {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<UserPublic>>() {
            @Override
            public Task<UserPublic> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<UserPublic> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getUserService().getUserPublicData("Bearer " + task.getResult(), userId).enqueue(new retrofit2.Callback<HttpService.APIResponse<UserPublic>>() {
                    @Override
                    public void onResponse(retrofit2.Call<HttpService.APIResponse<UserPublic>> call, retrofit2.Response<HttpService.APIResponse<UserPublic>> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse<UserPublic> resBody = response.body();
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
                    public void onFailure(retrofit2.Call<HttpService.APIResponse<UserPublic>> call, Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });
    }

    public static Task<List<UserPublic>> getBatchUserPublicData(final Set<String> uidSet) {
        if (uidSet.size() > 1024) throw new RuntimeException("uidSet size exceeded");
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<List<UserPublic>>>() {
            @Override
            public Task<List<UserPublic>> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<List<UserPublic>> taskCompletionSource = new TaskCompletionSource<>();
                ArrayList<String> uidList = new ArrayList<>(uidSet);
                HttpService.getInstance().getUserService().getAllUser("Bearer " + task.getResult(), uidList).enqueue(new retrofit2.Callback<HttpService.APIResponse<List<UserPublic>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<HttpService.APIResponse<List<UserPublic>>> call, retrofit2.Response<HttpService.APIResponse<List<UserPublic>>> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse<List<UserPublic>> resBody = response.body();
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
                    public void onFailure(retrofit2.Call<HttpService.APIResponse<List<UserPublic>>> call, Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });
    }

    public static Task<UserPublic> findUser(final String email, final String phoneNum) {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<UserPublic>>() {
            @Override
            public Task<UserPublic> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<UserPublic> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getUserService().findUser("Bearer " + task.getResult(), email, phoneNum).enqueue(new retrofit2.Callback<HttpService.APIResponse<UserPublic>>() {
                    @Override
                    public void onResponse(retrofit2.Call<HttpService.APIResponse<UserPublic>> call, retrofit2.Response<HttpService.APIResponse<UserPublic>> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse<UserPublic> resBody = response.body();
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
                    public void onFailure(retrofit2.Call<HttpService.APIResponse<UserPublic>> call, Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });
    }

    public static Task<HttpService.APIResponse> sendAddFriend(final String friendId, final String addFriendAction) {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<HttpService.APIResponse>>() {
            @Override
            public Task<HttpService.APIResponse> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<HttpService.APIResponse> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getUserService().addFriend("Bearer " + task.getResult(), friendId, addFriendAction).enqueue(new retrofit2.Callback<HttpService.APIResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<HttpService.APIResponse> call, retrofit2.Response<HttpService.APIResponse> response) {
                        if (response.body() != null) {
                            taskCompletionSource.setResult(response.body());
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

    public interface UserService {
        @FormUrlEncoded
        @POST("user/getUser")
        Call<HttpService.APIResponse<UserPublic>> getUserPublicData(@Header("Authorization") String auth, @Field("uid") String userId);

        @FormUrlEncoded
        @POST("user/findUser")
        Call<HttpService.APIResponse<UserPublic>> findUser(@Header("Authorization") String auth, @Field("email") @Nullable String email, @Field("phoneNum") @Nullable String phoneNum);

        @FormUrlEncoded
        @POST("user/getAllUser")
        Call<HttpService.APIResponse<List<UserPublic>>> getAllUser(@Header("Authorization") String auth, @Field("uids[]") List<String> uids);

        @FormUrlEncoded
        @POST("user/addFriend")
        Call<HttpService.APIResponse> addFriend(@Header("Authorization") String auth, @Field("friendId") String friendId, @Field("action") String action);


    }

    public interface AddFriendActions {
        String REQUEST = "REQUEST";
        String CANCEL = "CANCEL";
        String ACCEPT = "ACCEPT";
        String REJECT = "REJECT";
        String REMOVE = "REMOVE";
    }
}
