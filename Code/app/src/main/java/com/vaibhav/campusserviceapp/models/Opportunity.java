package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class Opportunity {
    @SerializedName("id")
    private String id;

    @SerializedName("poster_uid")
    private String posterUid;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("type")
    private String type;

    @SerializedName("apply_link")
    private String applyLink;

    @SerializedName("branch")
    private String branch;

    @SerializedName("deadline")
    private String deadline;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("profiles")
    private User poster;

    public Opportunity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPosterUid() { return posterUid; }
    public void setPosterUid(String posterUid) { this.posterUid = posterUid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getApplyLink() { return applyLink; }
    public void setApplyLink(String applyLink) { this.applyLink = applyLink; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public User getPoster() { return poster; }
    public void setPoster(User poster) { this.poster = poster; }
}
