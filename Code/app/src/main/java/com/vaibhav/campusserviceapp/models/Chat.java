package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class Chat {
    @SerializedName("id")
    private String id;

    @SerializedName("user1_id")
    private String participantOne;

    @SerializedName("user2_id")
    private String participantTwo;

    @SerializedName("last_message")
    private String lastMessage;

    @SerializedName("last_message_at")
    private String lastMessageAt;

    @SerializedName("created_at")
    private String createdAt;

    // Transient for UI
    private transient User otherUser;

    public Chat() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParticipantOne() { return participantOne; }
    public void setParticipantOne(String participantOne) { this.participantOne = participantOne; }

    public String getParticipantTwo() { return participantTwo; }
    public void setParticipantTwo(String participantTwo) { this.participantTwo = participantTwo; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(String lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public User getOtherUser() { return otherUser; }
    public void setOtherUser(User otherUser) { this.otherUser = otherUser; }

    public String getOtherParticipantId(String myUid) {
        if (participantOne != null && participantOne.equals(myUid)) {
            return participantTwo;
        }
        return participantOne;
    }
}
