package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class LostItem {
    @SerializedName("id")
    private String id;

    @SerializedName("owner_id")
    private String ownerId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("location")
    private String location;

    @SerializedName("contact")
    private String contact;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("category")
    private String category;

    @SerializedName("is_found")
    private boolean isFound;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("profiles")
    private User owner;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isFound() { return isFound; }
    public void setFound(boolean found) { isFound = found; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
