package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class Todo {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("title")
    private String title;

    @SerializedName("due_date")
    private String dueDate;

    @SerializedName(value = "is_complete", alternate = {"is_completed"})
    private boolean isComplete;

    public Todo() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public boolean isComplete() { return isComplete; }
    public void setComplete(boolean complete) { isComplete = complete; }
}
