package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.Chat;
import com.vaibhav.campusserviceapp.models.Message;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatApi {

    // ── Chats ─────────────────────────────────────────

    @GET("chat_rooms")
    Call<List<Chat>> getUserChats(
            @Query("or") String orFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("chat_rooms")
    Call<List<Chat>> findChat(
            @Query("or") String orFilter,
            @Query("select") String select
    );

    @POST("chat_rooms")
    Call<List<Chat>> createChat(
            @Body Chat chat,
            @Header("Prefer") String prefer
    );

    @PATCH("chat_rooms")
    Call<Void> updateChat(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );

    // ── Messages ──────────────────────────────────────

    @GET("messages")
    Call<List<Message>> getMessages(
            @Query("room_id") String roomIdFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @POST("messages")
    Call<Void> sendMessage(
            @Body Message message,
            @Header("Prefer") String prefer
    );

    @PATCH("messages")
    Call<Void> updateMessage(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );

    @PATCH("messages")
    Call<Void> markMessagesRead(
            @Query("room_id") String roomIdFilter,
            @Query("sender_id") String senderIdFilter,
            @Body Map<String, Object> updates
    );
}
