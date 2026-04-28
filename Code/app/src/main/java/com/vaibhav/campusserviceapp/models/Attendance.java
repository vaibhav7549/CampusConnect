package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class Attendance {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("subject")
    private String subject;

    @SerializedName("date")
    private String date;

    @SerializedName("status")
    private String status;

    public Attendance() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
