package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.Listing;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ListingApi {

    @GET("listings")
    Call<List<Listing>> getListings(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("listings")
    Call<List<Listing>> searchListings(
            @Query("title") String titleFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("listings")
    Call<List<Listing>> filterListings(
            @Query("category") String categoryFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("listings")
    Call<List<Listing>> getListingById(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @POST("listings")
    Call<List<Listing>> createListing(
            @Body Map<String, Object> listing,
            @Header("Prefer") String prefer
    );

    @PATCH("listings")
    Call<Void> updateListing(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );
}
