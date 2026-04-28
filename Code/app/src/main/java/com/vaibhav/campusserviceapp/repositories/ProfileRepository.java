package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.network.ProfileApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.Constants;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {
    private static final String TAG = "ProfileRepository";
    private final ProfileApi profileApi;
    private final SessionManager sessionManager;
    private final OkHttpClient okHttpClient;

    public ProfileRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        profileApi = client.getProfileApi();
        sessionManager = client.getSessionManager();
        okHttpClient = client.getOkHttpClient();
    }

    public LiveData<AuthRepository.Resource<User>> getProfile(String userId) {
        MutableLiveData<AuthRepository.Resource<User>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        profileApi.getProfile("eq." + userId, "*").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = withDefaultAvatar(response.body().get(0));
                    sessionManager.saveUserName(user.getName() != null ? user.getName() : "");
                    sessionManager.saveUserPhoto(user.getPhotoUrl() != null ? user.getPhotoUrl() : "");
                    result.setValue(AuthRepository.Resource.success(user));
                } else {
                    String err = "Profile not found";
                    try { err = response.errorBody() != null ? response.errorBody().string() : err; } catch (Exception ignored) {}
                    Log.e(TAG, "Profile not found for: " + userId + " code: " + response.code() + " - " + err);
                    result.setValue(AuthRepository.Resource.error(err));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, "getProfile error", t);
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> updateProfile(String userId, Map<String, Object> updates) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        profileApi.updateProfile("eq." + userId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (updates.containsKey("name")) {
                        sessionManager.saveUserName((String) updates.get("name"));
                    }
                    if (updates.containsKey("photo_url")) {
                        sessionManager.saveUserPhoto((String) updates.get("photo_url"));
                    }
                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception e) {}
                    Log.e(TAG, "Update failed: " + response.code() + " " + err);
                    result.setValue(AuthRepository.Resource.error("Update failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "updateProfile error", t);
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<String>> uploadAvatar(String userId, File file) {
        MutableLiveData<AuthRepository.Resource<String>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        new Thread(() -> {
            try {
                String path = userId + "/avatar.jpg";
                String url = Constants.STORAGE_URL + "object/" + Constants.BUCKET_AVATARS + "/" + path;

                RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));

                // Try POST first for Supabase Storage API
                Request request = new Request.Builder()
                        .url(url)
                        .post(fileBody)
                        .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .addHeader("Content-Type", "image/jpeg")
                        .addHeader("x-upsert", "true")
                        .build();

                okhttp3.Response response = okHttpClient.newCall(request).execute();

                if (response.isSuccessful() || response.code() == 200 || response.code() == 201) {
                    // Force cache busting by appending timestamp
                    String publicUrl = Constants.STORAGE_URL + "object/public/" + Constants.BUCKET_AVATARS + "/" + path + "?t=" + System.currentTimeMillis();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("photo_url", publicUrl);
                    updates.put("avatar_url", publicUrl);
                    
                    retrofit2.Response<Void> dbResponse = profileApi.updateProfile("eq." + userId, updates).execute();
                    if (dbResponse.isSuccessful() || dbResponse.code() == 200 || dbResponse.code() == 204) {
                        sessionManager.saveUserPhoto(publicUrl);
                        result.postValue(AuthRepository.Resource.success(publicUrl));
                    } else {
                        result.postValue(AuthRepository.Resource.error("Failed to update profile DB"));
                    }
                } else {
                    String errBody = response.body() != null ? response.body().string() : response.message();
                    Log.e(TAG, "Avatar upload failed: " + response.code() + " " + errBody);
                    result.postValue(AuthRepository.Resource.error("Upload failed: " + response.code()));
                }
            } catch (IOException e) {
                Log.e(TAG, "Avatar upload error", e);
                result.postValue(AuthRepository.Resource.error("Upload error: " + e.getMessage()));
            }
        }).start();

        return result;
    }

    public LiveData<AuthRepository.Resource<List<User>>> searchUsers(String query, String currentUserId) {
        MutableLiveData<AuthRepository.Resource<List<User>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        // Fixed: Supabase ilike format is "ilike.%query%"
        profileApi.searchProfiles(
                "id,name,photo_url,avatar_url,college,branch,year,bio,is_verified",
                "neq." + currentUserId,
                "ilike.%" + query + "%",
                "name.asc"
        ).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        withDefaultAvatar(user);
                    }
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    Log.e(TAG, "Search failed: " + response.code());
                    result.setValue(AuthRepository.Resource.error("Search failed"));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<User>>> getAllUsers(String currentUserId) {
        MutableLiveData<AuthRepository.Resource<List<User>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        profileApi.getAllProfiles(
                "id,name,photo_url,avatar_url,college,branch,year,bio,is_verified",
                "neq." + currentUserId
        ).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        withDefaultAvatar(user);
                    }
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    Log.e(TAG, "getAllUsers failed: " + response.code());
                    result.setValue(AuthRepository.Resource.error("Failed to load users"));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    private User withDefaultAvatar(User user) {
        if (user == null) return null;
        if (user.getPhotoUrl() == null || user.getPhotoUrl().trim().isEmpty()) {
            String displayName = user.getName() != null && !user.getName().trim().isEmpty()
                    ? user.getName().trim().replace(" ", "+")
                    : "Campus+Connect";
            user.setPhotoUrl("https://ui-avatars.com/api/?background=6C5CE7&color=ffffff&name=" + displayName);
        }
        return user;
    }
}
