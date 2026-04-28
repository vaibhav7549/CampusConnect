package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class FriendRequest {
    @SerializedName("id")
    private String id;

    @SerializedName("from_uid")
    private String fromUid;

    @SerializedName("to_uid")
    private String toUid;

    @SerializedName("status")
    private String status;

    @SerializedName("sent_at")
    private String sentAt;

    // Joined profile of the sender
    @SerializedName("profiles")
    private User fromUser;

    public FriendRequest() {}

    public FriendRequest(String fromUid, String toUid, String status) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromUid() { return fromUid; }
    public void setFromUid(String fromUid) { this.fromUid = fromUid; }

    public String getToUid() { return toUid; }
    public void setToUid(String toUid) { this.toUid = toUid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }
}
