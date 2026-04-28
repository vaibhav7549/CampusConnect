package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ProfileApi {

    @POST("profiles")
    Call<List<User>> insertProfile(
            @Body User profile,
            @Header("Prefer") String prefer
    );

    @GET("profiles")
    Call<List<User>> getProfile(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @PATCH("profiles")
    Call<Void> updateProfile(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );

    @GET("profiles")
    Call<List<User>> searchProfiles(
            @Query("select") String select,
            @Query("id") String idFilter,
            @Query("name") String nameFilter,
            @Query("order") String order
    );

    @GET("profiles")
    Call<List<User>> getAllProfiles(
            @Query("select") String select,
            @Query("id") String idFilter
    );

    @GET("profiles")
    Call<List<User>> getProfileById(
            @Query("id") String idFilter,
            @Query("select") String select
    );
}
