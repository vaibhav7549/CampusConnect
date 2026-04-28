package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vaibhav.campusserviceapp.models.Notification;
import com.vaibhav.campusserviceapp.network.NotificationApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {
    private final NotificationApi notificationApi;
    private final SessionManager sessionManager;

    public NotificationRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        notificationApi = client.getNotificationApi();
        sessionManager = client.getSessionManager();
    }

    public LiveData<AuthRepository.Resource<List<Notification>>> getNotifications() {
        MutableLiveData<AuthRepository.Resource<List<Notification>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        notificationApi.getNotifications("eq." + sessionManager.getUserId(), "created_at.desc").enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load notifications"));
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Boolean>> hasUnread() {
        MutableLiveData<AuthRepository.Resource<Boolean>> result = new MutableLiveData<>();

        notificationApi.getUnreadNotifications("eq." + sessionManager.getUserId(), "eq.false", "id").enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                boolean hasUnread = response.isSuccessful() && response.body() != null && !response.body().isEmpty();
                result.setValue(AuthRepository.Resource.success(hasUnread));
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.success(false));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> markAllRead() {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("is_read", true);

        notificationApi.markAllRead("eq." + sessionManager.getUserId(), updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(AuthRepository.Resource.success(null));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Failed"));
            }
        });

        return result;
    }

    public void sendNotification(String userId, String type, String message) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(type);
        notif.setMessage(message);
        notif.setFromUid(sessionManager.getUserId());

        notificationApi.createNotification(notif, "return=minimal").enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {}

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
