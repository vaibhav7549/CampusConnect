package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class Friend {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("friend_id")
    private String friendId;

    @SerializedName("since")
    private String since;

    public Friend() {}

    public Friend(String userId, String friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFriendId() { return friendId; }
    public void setFriendId(String friendId) { this.friendId = friendId; }

    public String getSince() { return since; }
    public void setSince(String since) { this.since = since; }
}
