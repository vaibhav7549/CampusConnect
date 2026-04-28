package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vaibhav.campusserviceapp.models.Friend;
import com.vaibhav.campusserviceapp.models.FriendRequest;
import com.vaibhav.campusserviceapp.models.Notification;
import com.vaibhav.campusserviceapp.network.FriendApi;
import com.vaibhav.campusserviceapp.network.NotificationApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRepository {
    private final FriendApi friendApi;
    private final NotificationApi notificationApi;
    private final SessionManager sessionManager;

    public FriendRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        friendApi = client.getFriendApi();
        notificationApi = client.getNotificationApi();
        sessionManager = client.getSessionManager();
    }

    public LiveData<AuthRepository.Resource<Void>> sendFriendRequest(String toUid) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        String uid = sessionManager.getUserId();
        FriendRequest request = new FriendRequest(uid, toUid, "pending");

        friendApi.sendFriendRequest(request, "return=minimal").enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Send notification
                    Notification notif = new Notification();
                    notif.setUserId(toUid);
                    notif.setType("friend_request");
                    notif.setMessage(sessionManager.getUserName() + " sent you a friend request");
                    notif.setFromUid(uid);

                    notificationApi.createNotification(notif, "return=minimal").enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> c, Response<Void> r) {}
                        @Override
                        public void onFailure(Call<Void> c, Throwable t) {}
                    });

                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to send request"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<FriendRequest>>> getReceivedRequests() {
        MutableLiveData<AuthRepository.Resource<List<FriendRequest>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        String uid = sessionManager.getUserId();
        friendApi.getReceivedRequests("eq." + uid, "eq.pending", "*,profiles!from_uid(name,photo_url,is_verified)").enqueue(new Callback<List<FriendRequest>>() {
            @Override
            public void onResponse(Call<List<FriendRequest>> call, Response<List<FriendRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load requests"));
                }
            }

            @Override
            public void onFailure(Call<List<FriendRequest>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> acceptRequest(String requestId, String fromUid) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        String uid = sessionManager.getUserId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "accepted");

        friendApi.updateRequestStatus("eq." + requestId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Add friendship rows
                    friendApi.addFriend(new Friend(uid, fromUid), "return=minimal").enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> c, Response<Void> r) {}
                        @Override
                        public void onFailure(Call<Void> c, Throwable t) {}
                    });
                    friendApi.addFriend(new Friend(fromUid, uid), "return=minimal").enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> c, Response<Void> r) {}
                        @Override
                        public void onFailure(Call<Void> c, Throwable t) {}
                    });

                    // Send notification to sender
                    Notification notif = new Notification();
                    notif.setUserId(fromUid);
                    notif.setType("friend_accepted");
                    notif.setMessage(sessionManager.getUserName() + " accepted your friend request");
                    notif.setFromUid(uid);
                    notificationApi.createNotification(notif, "return=minimal").enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> c, Response<Void> r) {}
                        @Override
                        public void onFailure(Call<Void> c, Throwable t) {}
                    });

                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to accept"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> rejectRequest(String requestId) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");

        friendApi.updateRequestStatus("eq." + requestId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(response.isSuccessful() ? AuthRepository.Resource.success(null) : AuthRepository.Resource.error("Failed"));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkFriendship(String otherUserId) {
        MutableLiveData<AuthRepository.Resource<Boolean>> result = new MutableLiveData<>();

        String uid = sessionManager.getUserId();
        friendApi.checkFriendship("eq." + uid, "eq." + otherUserId).enqueue(new Callback<List<Friend>>() {
            @Override
            public void onResponse(Call<List<Friend>> call, Response<List<Friend>> response) {
                boolean isFriend = response.isSuccessful() && response.body() != null && !response.body().isEmpty();
                result.setValue(AuthRepository.Resource.success(isFriend));
            }

            @Override
            public void onFailure(Call<List<Friend>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.success(false));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<Friend>>> getFriendsList() {
        MutableLiveData<AuthRepository.Resource<List<Friend>>> result = new MutableLiveData<>();

        String uid = sessionManager.getUserId();
        friendApi.getFriends("eq." + uid, "*").enqueue(new Callback<List<Friend>>() {
            @Override
            public void onResponse(Call<List<Friend>> call, Response<List<Friend>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed"));
                }
            }

            @Override
            public void onFailure(Call<List<Friend>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkRequestSent(String toUid) {
        MutableLiveData<AuthRepository.Resource<Boolean>> result = new MutableLiveData<>();

        String uid = sessionManager.getUserId();
        friendApi.getSentRequests("eq." + uid, "eq." + toUid, "*").enqueue(new Callback<List<FriendRequest>>() {
            @Override
            public void onResponse(Call<List<FriendRequest>> call, Response<List<FriendRequest>> response) {
                boolean sent = response.isSuccessful() && response.body() != null && !response.body().isEmpty();
                result.setValue(AuthRepository.Resource.success(sent));
            }

            @Override
            public void onFailure(Call<List<FriendRequest>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.success(false));
            }
        });

        return result;
    }
}
