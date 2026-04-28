package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class Listing {
    @SerializedName("id")
    private String id;

    @SerializedName("seller_id")
    private String sellerUid;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName(value = "photo_url", alternate = {"image_url"})
    private String photoUrl;

    @SerializedName("category")
    private String category;

    // Transient - populated via separate profile fetch
    private transient User seller;

    public Listing() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSellerUid() { return sellerUid; }
    public void setSellerUid(String sellerUid) { this.sellerUid = sellerUid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
}
