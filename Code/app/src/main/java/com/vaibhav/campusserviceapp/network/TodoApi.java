package com.vaibhav.campusserviceapp.network;

import com.vaibhav.campusserviceapp.models.Todo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TodoApi {

    @GET("todos")
    Call<List<Todo>> getTodos(
            @Query("user_id") String userIdFilter,
            @Query("order") String order
    );

    @POST("todos")
    Call<Void> addTodo(
            @Body Map<String, Object> todo,
            @Header("Prefer") String prefer
    );

    @PATCH("todos")
    Call<Void> updateTodo(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates
    );

    @DELETE("todos")
    Call<Void> deleteTodo(@Query("id") String idFilter);
}
