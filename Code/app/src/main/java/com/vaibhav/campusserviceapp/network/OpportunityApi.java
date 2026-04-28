package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.Opportunity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OpportunityApi {

    @GET("opportunities")
    Call<List<Opportunity>> getOpportunities(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("opportunities")
    Call<List<Opportunity>> filterOpportunities(
            @Query("type") String typeFilter,
            @Query("or") String branchFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("opportunities")
    Call<List<Opportunity>> getOpportunityById(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @POST("opportunities")
    Call<Void> createOpportunity(
            @Body Opportunity opportunity,
            @Header("Prefer") String prefer
    );

    @PATCH("opportunities")
    Call<Void> updateOpportunity(
            @Query("id") String idFilter,
            @Body Opportunity opportunity
    );

    @DELETE("opportunities")
    Call<Void> deleteOpportunity(@Query("id") String idFilter);
}
