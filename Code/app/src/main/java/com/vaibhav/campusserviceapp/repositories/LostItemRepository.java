package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaibhav.campusserviceapp.models.LostItem;
import com.vaibhav.campusserviceapp.network.LostItemApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LostItemRepository {
    private final LostItemApi lostItemApi;
    private final OkHttpClient okHttpClient;
    private final com.vaibhav.campusserviceapp.utils.SessionManager sessionManager;
    private final Gson gson = new Gson();

    public LostItemRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        this.lostItemApi = client.getLostItemApi();
        this.okHttpClient = client.getOkHttpClient();
        this.sessionManager = client.getSessionManager();
    }

    public LiveData<AuthRepository.Resource<List<LostItem>>> getLostItems() {
        MutableLiveData<AuthRepository.Resource<List<LostItem>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        // Avoid selecting avatar_url; some DB instances don't have that column
        lostItemApi.getLostItems("*,profiles!owner_id(id,name,photo_url,avatar_url,is_verified)", "created_at.desc")
                .enqueue(new Callback<List<LostItem>>() {
                    @Override
                    public void onResponse(Call<List<LostItem>> call, Response<List<LostItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(AuthRepository.Resource.success(response.body()));
                        } else {
                            result.setValue(AuthRepository.Resource.error(extractError(response)));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LostItem>> call, Throwable t) {
                        result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
                    }
                });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<LostItem>>> searchLostItems(String query) {
        MutableLiveData<AuthRepository.Resource<List<LostItem>>> result = new MutableLiveData<>();
        lostItemApi.searchLostItems("ilike.*" + query + "*", "*,profiles!owner_id(id,name,photo_url,avatar_url,is_verified)", "created_at.desc")
                .enqueue(new Callback<List<LostItem>>() {
                    @Override
                    public void onResponse(Call<List<LostItem>> call, Response<List<LostItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(AuthRepository.Resource.success(response.body()));
                        } else {
                            result.setValue(AuthRepository.Resource.error(extractError(response)));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LostItem>> call, Throwable t) {
                        result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
                    }
                });
        return result;
    }

    public LiveData<AuthRepository.Resource<List<LostItem>>> filterByFoundStatus(boolean found) {
        MutableLiveData<AuthRepository.Resource<List<LostItem>>> result = new MutableLiveData<>();
        lostItemApi.filterLostItems("eq." + found, "*,profiles!owner_id(id,name,photo_url,avatar_url,is_verified)", "created_at.desc")
                .enqueue(new Callback<List<LostItem>>() {
                    @Override
                    public void onResponse(Call<List<LostItem>> call, Response<List<LostItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(AuthRepository.Resource.success(response.body()));
                        } else {
                            result.setValue(AuthRepository.Resource.error(extractError(response)));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LostItem>> call, Throwable t) {
                        result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
                    }
                });
        return result;
    }

    public LiveData<AuthRepository.Resource<LostItem>> createLostItem(LostItem lostItem, File photoFile) {
        MutableLiveData<AuthRepository.Resource<LostItem>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());
        String userId = sessionManager.getUserId();
        if (userId == null || sessionManager.getAccessToken() == null) {
            result.setValue(AuthRepository.Resource.error("Session expired. Please log in again."));
            return result;
        }
        if (lostItem.getOwnerId() == null) {
            lostItem.setOwnerId(userId);
        }
        new Thread(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("owner_id", lostItem.getOwnerId());
                payload.put("title", lostItem.getTitle());
                payload.put("description", lostItem.getDescription());
                payload.put("location", lostItem.getLocation());
                payload.put("contact", lostItem.getContact());
                payload.put("category", lostItem.getCategory());
                payload.put("is_found", lostItem.isFound());

                if (photoFile != null) {
                    try {
                        String path = UUID.randomUUID().toString() + ".jpg";
                        String url = Constants.STORAGE_URL + "object/" + Constants.BUCKET_LOST_ITEM_IMAGES + "/" + path;
                        RequestBody body = RequestBody.create(photoFile, MediaType.parse("image/jpeg"));
                        Request request = new Request.Builder()
                                .url(url).post(body)
                                .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                                .addHeader("Content-Type", "image/jpeg")
                                .addHeader("x-upsert", "true")
                                .build();
                        okhttp3.Response uploadResp = okHttpClient.newCall(request).execute();
                        if (uploadResp.isSuccessful()) {
                            payload.put("image_url", Constants.STORAGE_URL + "object/public/" + Constants.BUCKET_LOST_ITEM_IMAGES + "/" + path);
                        }
                    } catch (Exception ignored) {
                        // Allow lost-item posts without an image if storage is unavailable.
                    }
                }
                lostItemApi.createLostItem(payload, "return=representation")
                        .enqueue(new Callback<List<LostItem>>() {
                            @Override
                            public void onResponse(Call<List<LostItem>> call, Response<List<LostItem>> response) {
                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                    result.postValue(AuthRepository.Resource.success(response.body().get(0)));
                                } else {
                                    result.postValue(AuthRepository.Resource.error(extractError(response)));
                                }
                            }

                            @Override
                            public void onFailure(Call<List<LostItem>> call, Throwable t) {
                                result.postValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
                            }
                        });
            } catch (Exception e) {
                result.postValue(AuthRepository.Resource.error("Failed to create lost item: " + e.getMessage()));
            }
        }).start();

        return result;
    }

    private String extractError(Response<?> response) {
        try {
            if (response.errorBody() == null) return "Failed to create lost item";
            String raw = response.errorBody().string();
            JsonObject obj = gson.fromJson(raw, JsonObject.class);
            if (obj != null && obj.has("message")) return obj.get("message").getAsString();
            if (obj != null && obj.has("msg")) return obj.get("msg").getAsString();
            return raw;
        } catch (Exception e) {
            return "Failed to create lost item";
        }
    }

    public LiveData<AuthRepository.Resource<Void>> markAsFound(String lostItemId) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_found", true);

        lostItemApi.updateLostItem("eq." + lostItemId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to update"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }
}
