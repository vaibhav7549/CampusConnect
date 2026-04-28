package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class PostUpvote {
    @SerializedName("post_id")
    private String postId;

    @SerializedName("user_id")
    private String userId;

    public PostUpvote() {}

    public PostUpvote(String postId, String userId) {
        this.postId = postId;
        this.userId = userId;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
