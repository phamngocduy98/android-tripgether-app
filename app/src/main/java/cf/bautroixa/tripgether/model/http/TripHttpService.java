package cf.bautroixa.tripgether.model.http;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import cf.bautroixa.tripgether.model.repo.objects.TripPublic;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class TripHttpService {
    public static Task<TripPublic> getTrip(final String tripId) {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<TripPublic>>() {
            @Override
            public Task<TripPublic> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<TripPublic> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getTripService().getTrip("Bearer " + task.getResult(), tripId).enqueue(new retrofit2.Callback<HttpService.APIResponse<TripPublic>>() {
                    @Override
                    public void onResponse(retrofit2.Call<HttpService.APIResponse<TripPublic>> call, retrofit2.Response<HttpService.APIResponse<TripPublic>> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse<TripPublic> resBody = response.body();
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
                    public void onFailure(retrofit2.Call<HttpService.APIResponse<TripPublic>> call, Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });
    }

    public static Task<ArrayList<UserPublic>> getTripWaitingRoom(final String tripId) {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<ArrayList<UserPublic>>>() {
            @Override
            public Task<ArrayList<UserPublic>> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<ArrayList<UserPublic>> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getTripService().getTripWaitingRoom("Bearer " + task.getResult(), tripId).enqueue(new retrofit2.Callback<HttpService.APIResponse<ArrayList<UserPublic>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<HttpService.APIResponse<ArrayList<UserPublic>>> call, retrofit2.Response<HttpService.APIResponse<ArrayList<UserPublic>>> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse<ArrayList<UserPublic>> resBody = response.body();
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
                    public void onFailure(retrofit2.Call<HttpService.APIResponse<ArrayList<UserPublic>>> call, Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });
    }

    public static Task<HttpService.APIResponse> joinTrip(final String userId, final String tripRefId, final String joinCode) {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<HttpService.APIResponse>>() {
            @Override
            public Task<HttpService.APIResponse> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<HttpService.APIResponse> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getTripService().joinTrip("Bearer " + task.getResult(), userId, tripRefId, joinCode).enqueue(new Callback<HttpService.APIResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<HttpService.APIResponse> call, @NotNull Response<HttpService.APIResponse> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse<TripPublic> resBody = response.body();
                            if (resBody.success) {
                                taskCompletionSource.setResult(response.body());
                            } else {
                                taskCompletionSource.setException(new Exception(resBody.reason));
                            }
                        } else {
                            taskCompletionSource.setException(new Exception("Network error"));
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<HttpService.APIResponse> call, @NotNull Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });
    }

    public static Task<HttpService.APIResponse> leaveTrip() {
        return HttpService.getToken().continueWithTask(new Continuation<String, Task<HttpService.APIResponse>>() {
            @Override
            public Task<HttpService.APIResponse> then(@NonNull Task<String> task) throws Exception {
                final TaskCompletionSource<HttpService.APIResponse> taskCompletionSource = new TaskCompletionSource<>();
                HttpService.getInstance().getTripService().leaveTrip("Bearer " + task.getResult()).enqueue(new Callback<HttpService.APIResponse>() {
                    @Override
                    public void onResponse(Call<HttpService.APIResponse> call, Response<HttpService.APIResponse> response) {
                        if (response.body() != null) {
                            HttpService.APIResponse<TripPublic> resBody = response.body();
                            if (resBody.success) {
                                taskCompletionSource.setResult(response.body());
                            } else {
                                taskCompletionSource.setException(new Exception(resBody.reason));
                            }
                        } else {
                            taskCompletionSource.setException(new Exception("Network error"));
                        }
                    }

                    @Override
                    public void onFailure(Call<HttpService.APIResponse> call, Throwable t) {
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
                return taskCompletionSource.getTask();
            }
        });

    }

    public interface TripService {
        @FormUrlEncoded
        @POST("/trip/getTrip")
        Call<HttpService.APIResponse<TripPublic>> getTrip(@Header("Authorization") String auth, @Field("tripId") String tripId);

        @FormUrlEncoded
        @POST("/trip/getTrip/getWaitingRoom")
        Call<HttpService.APIResponse<ArrayList<UserPublic>>> getTripWaitingRoom(@Header("Authorization") String auth, @Field("tripId") String tripId);

        @FormUrlEncoded
        @POST("/trip/joinTrip")
        Call<HttpService.APIResponse> joinTrip(@Header("Authorization") String auth, @Field("uid") String userId, @Field("tripId") String tripId, @Field("joinCode") String joinCode);

        @POST("/trip/leaveTrip")
        Call<HttpService.APIResponse> leaveTrip(@Header("Authorization") String auth);
    }
}
