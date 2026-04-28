package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaibhav.campusserviceapp.models.Listing;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.network.ListingApi;
import com.vaibhav.campusserviceapp.network.ProfileApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.Constants;
import com.vaibhav.campusserviceapp.utils.SessionManager;

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

public class ListingRepository {
    private final ListingApi listingApi;
    private final ProfileApi profileApi;
    private final SessionManager sessionManager;
    private final OkHttpClient okHttpClient;
    private final Gson gson = new Gson();

    public ListingRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        listingApi = client.getListingApi();
        profileApi = client.getProfileApi();
        sessionManager = client.getSessionManager();
        okHttpClient = client.getOkHttpClient();
    }

    public LiveData<AuthRepository.Resource<List<Listing>>> getListings() {
        MutableLiveData<AuthRepository.Resource<List<Listing>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        // Use simple select without join to avoid FK resolution issues
        listingApi.getListings("*", "id.desc").enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Listing> listings = response.body();
                    // Fetch seller profiles on background thread
                    new Thread(() -> {
                        populateSellerProfiles(listings);
                        result.postValue(AuthRepository.Resource.success(listings));
                    }).start();
                } else {
                    result.setValue(AuthRepository.Resource.error(extractError(response)));
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<Listing>>> searchListings(String query) {
        MutableLiveData<AuthRepository.Resource<List<Listing>>> result = new MutableLiveData<>();

        listingApi.searchListings("ilike.*" + query + "*", "*", "id.desc").enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Listing> listings = response.body();
                    new Thread(() -> {
                        populateSellerProfiles(listings);
                        result.postValue(AuthRepository.Resource.success(listings));
                    }).start();
                } else {
                    result.setValue(AuthRepository.Resource.error(extractError(response)));
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<Listing>>> filterByCategory(String category) {
        MutableLiveData<AuthRepository.Resource<List<Listing>>> result = new MutableLiveData<>();

        listingApi.filterListings("eq." + category, "*", "id.desc").enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Listing> listings = response.body();
                    new Thread(() -> {
                        populateSellerProfiles(listings);
                        result.postValue(AuthRepository.Resource.success(listings));
                    }).start();
                } else {
                    result.setValue(AuthRepository.Resource.error(extractError(response)));
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    /**
     * Fetches seller profiles for each listing on the current (background) thread.
     * Must be called from a background thread.
     */
    private void populateSellerProfiles(List<Listing> listings) {
        for (Listing listing : listings) {
            try {
                String sellerId = listing.getSellerUid();
                if (sellerId != null && !sellerId.isEmpty()) {
                    Response<List<User>> pResp = profileApi.getProfile("eq." + sellerId, "id,name,photo_url,avatar_url,is_verified").execute();
                    if (pResp.isSuccessful() && pResp.body() != null && !pResp.body().isEmpty()) {
                        listing.setSeller(pResp.body().get(0));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public LiveData<AuthRepository.Resource<Listing>> createListing(Listing listing, File photoFile) {
        MutableLiveData<AuthRepository.Resource<Listing>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());
        String userId = sessionManager.getUserId();
        if (userId == null || sessionManager.getAccessToken() == null) {
            result.setValue(AuthRepository.Resource.error("Session expired. Please log in again."));
            return result;
        }
        if (listing.getSellerUid() == null) {
            listing.setSellerUid(userId);
        }

        new Thread(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("seller_id", listing.getSellerUid());
                payload.put("title", listing.getTitle());
                payload.put("description", listing.getDescription());
                payload.put("price", listing.getPrice());
                payload.put("category", listing.getCategory());

                if (photoFile != null) {
                    String path = UUID.randomUUID().toString() + ".jpg";
                    String url = Constants.STORAGE_URL + "object/" + Constants.BUCKET_LISTING_IMAGES + "/" + path;
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
                        payload.put("photo_url", Constants.STORAGE_URL + "object/public/" + Constants.BUCKET_LISTING_IMAGES + "/" + path);
                    }
                }

                listingApi.createListing(payload, "return=representation").enqueue(new Callback<List<Listing>>() {
                    @Override
                    public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            result.postValue(AuthRepository.Resource.success(response.body().get(0)));
                        } else {
                            result.postValue(AuthRepository.Resource.error(extractError(response)));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Listing>> call, Throwable t) {
                        result.postValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
                    }
                });
            } catch (IOException e) {
                result.postValue(AuthRepository.Resource.error("Upload error: " + e.getMessage()));
            }
        }).start();

        return result;
    }

    private String extractError(Response<?> response) {
        try {
            if (response.errorBody() == null) return "Failed to load listings";
            String raw = response.errorBody().string();
            JsonObject obj = gson.fromJson(raw, JsonObject.class);
            if (obj != null && obj.has("message")) return obj.get("message").getAsString();
            if (obj != null && obj.has("msg")) return obj.get("msg").getAsString();
            return raw;
        } catch (Exception e) {
            return "Failed to load listings";
        }
    }

    public LiveData<AuthRepository.Resource<Void>> markAsSold(String listingId) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.error("Mark-as-sold is unavailable until the listings schema includes an is_sold column."));
        return result;
    }
}
