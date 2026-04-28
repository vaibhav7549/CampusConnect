package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.Exam;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ExamApi {

    @GET("exams")
    Call<List<Exam>> getExams(
            @Query("user_id") String userIdFilter,
            @Query("order") String order
    );

    @POST("exams")
    Call<Void> addExam(
            @Body Exam exam,
            @Header("Prefer") String prefer
    );

    @DELETE("exams")
    Call<Void> deleteExam(@Query("id") String idFilter);
}
