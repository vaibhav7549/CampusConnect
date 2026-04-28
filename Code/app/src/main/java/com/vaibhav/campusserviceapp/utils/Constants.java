package com.vaibhav.campusserviceapp.utils;

public class Constants {
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // REPLACE THESE WITH YOUR SUPABASE PROJECT VALUES
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    public static final String SUPABASE_URL = com.vaibhav.campusserviceapp.BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = com.vaibhav.campusserviceapp.BuildConfig.SUPABASE_ANON_KEY;

    // Derived endpoints
    public static final String REST_URL = SUPABASE_URL + "/rest/v1/";
    public static final String AUTH_URL = SUPABASE_URL + "/auth/v1/";
    public static final String STORAGE_URL = SUPABASE_URL + "/storage/v1/";
    public static final String REALTIME_URL = SUPABASE_URL.replace("https://", "wss://") + "/realtime/v1/websocket?apikey=" + SUPABASE_ANON_KEY + "&vsn=1.0.0";

    // Storage bucket names
    public static final String BUCKET_AVATARS = "avatars";
    public static final String BUCKET_POST_IMAGES = "post-images";
    public static final String BUCKET_POST_PDFS = "post-pdfs";
    public static final String BUCKET_LISTING_IMAGES = "listing-images";
    public static final String BUCKET_LOST_ITEM_IMAGES = "lost-item-images";
    public static final String BUCKET_CHAT_IMAGES = "chat-images";

    // SharedPreferences
    public static final String PREF_NAME = "campus_connect_prefs";
    public static final String PREF_ACCESS_TOKEN = "access_token";
    public static final String PREF_REFRESH_TOKEN = "refresh_token";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_PHOTO = "user_photo_url";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_ATTENDANCE_SUBJECTS = "attendance_subjects";

    // Pagination
    public static final int PAGE_SIZE = 15;
}
