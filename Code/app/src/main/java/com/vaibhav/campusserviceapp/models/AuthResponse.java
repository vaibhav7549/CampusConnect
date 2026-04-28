package com.vaibhav.campusserviceapp.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private int expiresIn;

    @SerializedName("user")
    private AuthUser user;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public int getExpiresIn() { return expiresIn; }
    public AuthUser getUser() { return user; }

    public static class AuthUser {
        @SerializedName("id")
        private String id;

        @SerializedName("email")
        private String email;

        public String getId() { return id; }
        public String getEmail() { return email; }
    }
}
