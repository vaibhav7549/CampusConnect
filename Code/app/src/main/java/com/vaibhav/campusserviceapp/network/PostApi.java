package com.vaibhav.campusserviceapp.network;

import com.google.gson.JsonObject;
import com.vaibhav.campusserviceapp.models.Bookmark;
import com.vaibhav.campusserviceapp.models.Comment;
import com.vaibhav.campusserviceapp.models.Post;
import com.vaibhav.campusserviceapp.models.PostUpvote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PostApi {

    // ── Posts ──────────────────────────────────────────

    @POST("posts")
    Call<List<Post>> createPost(
            @Body Post post,
            @Header("Prefer") String prefer
    );

    @GET("posts")
    Call<List<Post>> getPosts(
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("posts")
    Call<List<Post>> getPostsFiltered(
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("branch") String branchFilter,
            @Query("subject") String subjectFilter
    );

    @GET("posts")
    Call<List<Post>> getPostById(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @PATCH("posts")
    Call<Void> updatePostUpvotes(
            @Query("id") String idFilter,
            @Body JsonObject body
    );

    @DELETE("posts")
    Call<Void> deletePost(@Query("id") String idFilter);

    // ── Upvotes ───────────────────────────────────────

    @POST("post_upvotes")
    Call<Void> addUpvote(
            @Body PostUpvote upvote,
            @Header("Prefer") String prefer
    );

    @DELETE("post_upvotes")
    Call<Void> removeUpvote(
            @Query("post_id") String postIdFilter,
            @Query("user_id") String userIdFilter
    );

    @GET("post_upvotes")
    Call<List<PostUpvote>> getUpvotes(
            @Query("post_id") String postIdFilter,
            @Query("select") String select
    );

    @GET("post_upvotes")
    Call<List<PostUpvote>> checkUpvoted(
            @Query("post_id") String postIdFilter,
            @Query("user_id") String userIdFilter
    );

    // ── Bookmarks ─────────────────────────────────────

    @POST("bookmarks")
    Call<Void> addBookmark(
            @Body Bookmark bookmark,
            @Header("Prefer") String prefer
    );

    @DELETE("bookmarks")
    Call<Void> removeBookmark(
            @Query("post_id") String postIdFilter,
            @Query("user_id") String userIdFilter
    );

    @GET("bookmarks")
    Call<List<Bookmark>> checkBookmarked(
            @Query("post_id") String postIdFilter,
            @Query("user_id") String userIdFilter
    );

    @GET("bookmarks")
    Call<List<Bookmark>> getUserBookmarks(
            @Query("user_id") String userIdFilter,
            @Query("select") String select
    );

    // ── Comments ──────────────────────────────────────

    @POST("comments")
    Call<Void> addComment(
            @Body Comment comment,
            @Header("Prefer") String prefer
    );

    @GET("comments")
    Call<List<Comment>> getComments(
            @Query("post_id") String postIdFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @DELETE("comments")
    Call<Void> deleteComment(@Query("id") String idFilter);
}
