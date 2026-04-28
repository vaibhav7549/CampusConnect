package com.vaibhav.campusserviceapp.network;

import android.content.Context;

import com.vaibhav.campusserviceapp.utils.Constants;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    private static SupabaseClient instance;
    private final Retrofit restRetrofit;
    private final Retrofit authRetrofit;
    private final OkHttpClient okHttpClient;
    private SessionManager sessionManager;

    private SupabaseClient(Context context) {
        sessionManager = new SessionManager(context);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new ApiKeyInterceptor())
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        restRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.REST_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.AUTH_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context.getApplicationContext());
        }
        return instance;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    // ── REST API Interfaces ───────────────────────────

    public ProfileApi getProfileApi() {
        return restRetrofit.create(ProfileApi.class);
    }

    public PostApi getPostApi() {
        return restRetrofit.create(PostApi.class);
    }

    public ChatApi getChatApi() {
        return restRetrofit.create(ChatApi.class);
    }

    public FriendApi getFriendApi() {
        return restRetrofit.create(FriendApi.class);
    }

    public TodoApi getTodoApi() {
        return restRetrofit.create(TodoApi.class);
    }

    public ExamApi getExamApi() {
        return restRetrofit.create(ExamApi.class);
    }

    public AttendanceApi getAttendanceApi() {
        return restRetrofit.create(AttendanceApi.class);
    }

    public ListingApi getListingApi() {
        return restRetrofit.create(ListingApi.class);
    }

    public LostItemApi getLostItemApi() {
        return restRetrofit.create(LostItemApi.class);
    }

    public OpportunityApi getOpportunityApi() {
        return restRetrofit.create(OpportunityApi.class);
    }

    public NotificationApi getNotificationApi() {
        return restRetrofit.create(NotificationApi.class);
    }

    // ── Auth API Interface ────────────────────────────

    public AuthApi getAuthApi() {
        return authRetrofit.create(AuthApi.class);
    }

    // ── Interceptors ──────────────────────────────────

    private static class ApiKeyInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("apikey", Constants.SUPABASE_ANON_KEY)
                    .build();
            return chain.proceed(request);
        }
    }

    private static class AuthInterceptor implements Interceptor {
        private final SessionManager sessionManager;

        AuthInterceptor(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();

            String token = sessionManager.getAccessToken();
            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            } else {
                builder.header("Authorization", "Bearer " + Constants.SUPABASE_ANON_KEY);
            }

            return chain.proceed(builder.build());
        }
    }
}
