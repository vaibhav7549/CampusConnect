package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.Notification;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NotificationApi {

    @GET("notifications")
    Call<List<Notification>> getNotifications(
            @Query("user_id") String userIdFilter,
            @Query("order") String order
    );

    @GET("notifications")
    Call<List<Notification>> getUnreadNotifications(
            @Query("user_id") String userIdFilter,
            @Query("is_read") String isReadFilter,
            @Query("select") String select
    );

    @POST("notifications")
    Call<Void> createNotification(
            @Body Notification notification,
            @Header("Prefer") String prefer
    );

    @PATCH("notifications")
    Call<Void> markAllRead(
            @Query("user_id") String userIdFilter,
            @Body Map<String, Object> updates
    );
}
