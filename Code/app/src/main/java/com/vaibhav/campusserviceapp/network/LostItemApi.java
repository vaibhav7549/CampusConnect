package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.LostItem;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LostItemApi {
    @GET("lost_items")
    Call<List<LostItem>> getLostItems(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("lost_items")
    Call<List<LostItem>> searchLostItems(
            @Query("title") String titleFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("lost_items")
    Call<List<LostItem>> filterLostItems(
            @Query("is_found") String isFoundFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @POST("lost_items")
    Call<List<LostItem>> createLostItem(
            @Body Map<String, Object> lostItem,
            @Header("Prefer") String prefer
    );

    @PATCH("lost_items")
    Call<Void> updateLostItem(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );
}
