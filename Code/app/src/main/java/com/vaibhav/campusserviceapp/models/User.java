package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("college")
    private String college;

    @SerializedName("branch")
    private String branch;

    @SerializedName("year")
    private int year;

    @SerializedName("college_email")
    private String collegeEmail;

    @SerializedName("photo_url")
    private String photoUrl;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("bio")
    private String bio;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("fcm_token")
    private String fcmToken;

    @SerializedName("created_at")
    private String createdAt;

    public User() {}

    public User(String id, String name, String college, String branch, int year, String collegeEmail) {
        this.id = id;
        this.name = name;
        this.college = college;
        this.branch = branch;
        this.year = year;
        this.collegeEmail = collegeEmail;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getCollegeEmail() { return collegeEmail; }
    public void setCollegeEmail(String collegeEmail) { this.collegeEmail = collegeEmail; }

    public String getPhotoUrl() { 
        if (photoUrl != null && !photoUrl.isEmpty()) return photoUrl;
        return avatarUrl;
    }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
