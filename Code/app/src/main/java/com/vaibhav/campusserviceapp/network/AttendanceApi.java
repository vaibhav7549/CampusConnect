package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.Attendance;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AttendanceApi {

    @GET("attendance")
    Call<List<Attendance>> getAttendance(
            @Query("user_id") String userIdFilter,
            @Query("subject") String subjectFilter,
            @Query("order") String order
    );

    @GET("attendance")
    Call<List<Attendance>> getAttendanceForDate(
            @Query("user_id") String userIdFilter,
            @Query("subject") String subjectFilter,
            @Query("date") String dateFilter
    );

    @GET("attendance")
    Call<List<Attendance>> getAllAttendanceForSubject(
            @Query("user_id") String userIdFilter,
            @Query("subject") String subjectFilter,
            @Query("select") String select
    );

    @POST("attendance")
    Call<Void> markAttendance(
            @Body Attendance attendance,
            @Header("Prefer") String prefer
    );

    @PATCH("attendance")
    Call<Void> updateAttendance(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );
}
