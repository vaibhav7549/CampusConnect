package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("id")
    private String id;

    @SerializedName("post_id")
    private String postId;

    @SerializedName("user_id")
    private String authorId;

    @SerializedName("content")
    private String content;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("profiles")
    private User author;

    public Comment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
}
