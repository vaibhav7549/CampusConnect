package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("id")
    private String id;

    @SerializedName("author_id")
    private String authorUid;

    @SerializedName("subject")
    private String subject;

    @SerializedName("branch")
    private String branch;

    @SerializedName("content")
    private String content;

    @SerializedName("pdf_url")
    private String pdfUrl;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("upvotes")
    private int upvotes;

    @SerializedName("created_at")
    private String createdAt;

    // Joined profile data
    @SerializedName("profiles")
    private User author;

    // Transient fields for UI state
    private transient boolean isUpvoted;
    private transient boolean isBookmarked;
    private transient int upvoteCount;

    public Post() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorUid() { return authorUid; }
    public void setAuthorUid(String authorUid) { this.authorUid = authorUid; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public boolean isUpvoted() { return isUpvoted; }
    public void setUpvoted(boolean upvoted) { isUpvoted = upvoted; }

    public boolean isBookmarked() { return isBookmarked; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    public int getUpvoteCount() { return upvoteCount; }
    public void setUpvoteCount(int upvoteCount) { this.upvoteCount = upvoteCount; }
}
