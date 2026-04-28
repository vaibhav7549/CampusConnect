package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.Friend;
import com.vaibhav.campusserviceapp.models.FriendRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FriendApi {

    // ── Friend Requests ───────────────────────────────

    @POST("friend_requests")
    Call<Void> sendFriendRequest(
            @Body FriendRequest request,
            @Header("Prefer") String prefer
    );

    @GET("friend_requests")
    Call<List<FriendRequest>> getReceivedRequests(
            @Query("to_uid") String toUidFilter,
            @Query("status") String statusFilter,
            @Query("select") String select
    );

    @GET("friend_requests")
    Call<List<FriendRequest>> getSentRequests(
            @Query("from_uid") String fromUidFilter,
            @Query("to_uid") String toUidFilter,
            @Query("select") String select
    );

    @PATCH("friend_requests")
    Call<Void> updateRequestStatus(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );

    // ── Friends ───────────────────────────────────────

    @POST("friends")
    Call<Void> addFriend(
            @Body Friend friend,
            @Header("Prefer") String prefer
    );

    @GET("friends")
    Call<List<Friend>> getFriends(
            @Query("user_id") String userIdFilter,
            @Query("select") String select
    );

    @GET("friends")
    Call<List<Friend>> checkFriendship(
            @Query("user_id") String userIdFilter,
            @Query("friend_id") String friendIdFilter
    );
}
