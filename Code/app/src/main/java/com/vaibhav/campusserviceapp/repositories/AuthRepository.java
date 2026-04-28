package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaibhav.campusserviceapp.models.AuthResponse;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.network.AuthApi;
import com.vaibhav.campusserviceapp.network.ProfileApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.utils.VerifiedDomains;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final AuthApi authApi;
    private final ProfileApi profileApi;
    private final SessionManager sessionManager;
    private final Gson gson = new Gson();

    public AuthRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        authApi = client.getAuthApi();
        profileApi = client.getProfileApi();
        sessionManager = client.getSessionManager();
    }

    public LiveData<Resource<AuthResponse>> signUp(String email, String password) {
        MutableLiveData<Resource<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        authApi.signUp(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = gson.fromJson(response.body(), AuthResponse.class);
                    if (authResponse.getAccessToken() != null) {
                        sessionManager.saveAuthTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
                        sessionManager.saveUserId(authResponse.getUser().getId());
                        sessionManager.saveUserEmail(authResponse.getUser().getEmail());
                        result.setValue(Resource.success(authResponse));
                    } else {
                        result.setValue(Resource.error("Signup successful. Verify your email, then log in."));
                    }
                } else {
                    String errBody = extractErrorMessage(response);
                    Log.e(TAG, "Signup failed: " + response.code() + " " + errBody);
                    result.setValue(Resource.error("Signup failed: " + errBody));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Signup network error", t);
                result.setValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<AuthResponse>> signIn(String email, String password) {
        MutableLiveData<Resource<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        authApi.signIn(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = gson.fromJson(response.body(), AuthResponse.class);
                    sessionManager.saveAuthTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
                    sessionManager.saveUserId(authResponse.getUser().getId());
                    sessionManager.saveUserEmail(authResponse.getUser().getEmail());
                    result.setValue(Resource.success(authResponse));
                } else {
                    String errBody = extractErrorMessage(response);
                    Log.e(TAG, "SignIn failed: " + response.code() + " " + errBody);
                    result.setValue(Resource.error(errBody));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "SignIn network error", t);
                result.setValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    /**
     * Validates the current session. If the access token is expired,
     * attempts to refresh it using the refresh token. This ensures
     * the user stays logged in like Instagram/professional apps.
     */
    public LiveData<Resource<Boolean>> validateToken() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        String token = sessionManager.getAccessToken();
        String refreshToken = sessionManager.getRefreshToken();

        if (token == null) {
            result.setValue(Resource.error("No token"));
            return result;
        }

        authApi.getUser("Bearer " + token).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else if (response.code() == 401 && refreshToken != null) {
                    // Token expired — try to refresh
                    refreshAccessToken(refreshToken, result);
                } else if (response.code() == 401) {
                    sessionManager.clearSession();
                    result.setValue(Resource.error("Session expired. Please log in again."));
                } else {
                    // Other error — still let user in if they have tokens
                    result.setValue(Resource.success(true));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Network failure — don't log out! Allow offline access
                Log.w(TAG, "Token validation failed (offline?): " + t.getMessage());
                result.setValue(Resource.success(true));
            }
        });

        return result;
    }

    /**
     * Refreshes the access token using Supabase's refresh_token grant.
     * This is the key to persistent sessions.
     */
    private void refreshAccessToken(String refreshToken, MutableLiveData<Resource<Boolean>> result) {
        JsonObject body = new JsonObject();
        body.addProperty("refresh_token", refreshToken);

        authApi.refreshToken(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = gson.fromJson(response.body(), AuthResponse.class);
                    if (authResponse.getAccessToken() != null) {
                        sessionManager.saveAuthTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
                        Log.d(TAG, "Token refreshed successfully");
                        result.setValue(Resource.success(true));
                    } else {
                        sessionManager.clearSession();
                        result.setValue(Resource.error("Session expired"));
                    }
                } else {
                    String errBody = "";
                    try { errBody = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception e) {}
                    Log.e(TAG, "Token refresh failed: " + response.code() + " " + errBody);
                    sessionManager.clearSession();
                    result.setValue(Resource.error("Session expired. Please log in again."));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Network error during refresh — don't log out
                Log.w(TAG, "Token refresh network error (offline?): " + t.getMessage());
                result.setValue(Resource.success(true));
            }
        });
    }

    public void insertProfile(User profile) {
        profileApi.insertProfile(profile, "return=representation,resolution=merge-duplicates").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Profile upserted successfully");
                } else {
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception e) {}
                    Log.e(TAG, "Profile insert failed: " + response.code() + " " + err);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, "Profile insert network error", t);
            }
        });
    }

    public LiveData<Resource<Boolean>> checkAndSetVerified(String userId, String collegeEmail) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        if (VerifiedDomains.isVerifiedEmail(collegeEmail)) {
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("is_verified", true);

            profileApi.updateProfile("eq." + userId, updates).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    result.setValue(Resource.success(true));
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    result.setValue(Resource.success(false));
                }
            });
        } else {
            result.setValue(Resource.success(false));
        }

        return result;
    }

    public LiveData<Resource<String>> recoverPassword(String email) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        JsonObject body = new JsonObject();
        body.addProperty("email", email);

        authApi.recoverPassword(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success("Reset email sent"));
                } else {
                    result.setValue(Resource.error("Failed to send reset email"));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public void logout() {
        String token = sessionManager.getAccessToken();
        if (token != null) {
            authApi.logout("Bearer " + token).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {}

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {}
            });
        }
        sessionManager.clearSession();
    }

    private String extractErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() == null) {
                return response.message() != null ? response.message() : "Request failed";
            }
            String raw = response.errorBody().string();
            JsonObject obj = gson.fromJson(raw, JsonObject.class);
            if (obj == null) return raw;

            JsonElement msg = obj.get("msg");
            if (msg != null && !msg.isJsonNull()) return msg.getAsString();

            JsonElement message = obj.get("message");
            if (message != null && !message.isJsonNull()) return message.getAsString();

            JsonElement errorDescription = obj.get("error_description");
            if (errorDescription != null && !errorDescription.isJsonNull()) return errorDescription.getAsString();

            JsonElement error = obj.get("error");
            if (error != null && !error.isJsonNull()) return error.getAsString();

            return raw;
        } catch (Exception e) {
            return response.message() != null ? response.message() : "Request failed";
        }
    }

    // ── Resource wrapper ──────────────────────────────

    public static class Resource<T> {
        public enum Status { SUCCESS, ERROR, LOADING }

        public final Status status;
        public final T data;
        public final String message;

        private Resource(Status status, T data, String message) {
            this.status = status;
            this.data = data;
            this.message = message;
        }

        public static <T> Resource<T> success(T data) {
            return new Resource<>(Status.SUCCESS, data, null);
        }

        public static <T> Resource<T> error(String msg) {
            return new Resource<>(Status.ERROR, null, msg);
        }

        public static <T> Resource<T> loading() {
            return new Resource<>(Status.LOADING, null, null);
        }
    }
}
