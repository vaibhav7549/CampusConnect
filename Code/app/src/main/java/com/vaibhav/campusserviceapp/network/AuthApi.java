package com.vaibhav.campusserviceapp.network;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApi {

    @POST("signup")
    Call<JsonObject> signUp(@Body JsonObject body);

    @POST("token?grant_type=password")
    Call<JsonObject> signIn(@Body JsonObject body);

    @POST("token?grant_type=refresh_token")
    Call<JsonObject> refreshToken(@Body JsonObject body);

    @GET("user")
    Call<JsonObject> getUser(@Header("Authorization") String bearerToken);

    @POST("recover")
    Call<JsonObject> recoverPassword(@Body JsonObject body);

    @POST("logout")
    Call<JsonObject> logout(@Header("Authorization") String bearerToken);
}
