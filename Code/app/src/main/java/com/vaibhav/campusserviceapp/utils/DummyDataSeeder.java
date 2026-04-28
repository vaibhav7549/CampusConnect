package com.vaibhav.campusserviceapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vaibhav.campusserviceapp.network.SupabaseClient;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Seeds dummy data into Supabase to make the app look active and alive.
 * Only runs ONCE per install.
 */
public class DummyDataSeeder {
    private static final String TAG = "DummyDataSeeder";
    private static final String PREF_SEEDED = "dummy_data_seeded_v2";

    private final OkHttpClient client;
    private final SessionManager sessionManager;
    private final SharedPreferences prefs;

    public DummyDataSeeder(Context context) {
        SupabaseClient sc = SupabaseClient.getInstance(context);
        this.client = sc.getOkHttpClient();
        this.sessionManager = sc.getSessionManager();
        this.prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void seedIfNeeded() {
        if (prefs.getBoolean(PREF_SEEDED, false)) return;

        new Thread(() -> {
            try {
                String userId = sessionManager.getUserId();
                if (userId == null) return;

                // Seed dummy posts from THIS user so RLS allows it
                seedPosts(userId);

                prefs.edit().putBoolean(PREF_SEEDED, true).apply();
                Log.d(TAG, "✅ Dummy data seeded successfully!");
            } catch (Exception e) {
                Log.e(TAG, "Seeding failed: " + e.getMessage(), e);
            }
        }).start();
    }

    private void seedPosts(String userId) throws IOException {
        String[][] posts = {
            {"📚 Just finished the Data Structures assignment! Anyone else find Red-Black trees mind-bending? #CSE #DataStructures", "CSE", "Data Structures"},
            {"🎯 Pro tip: Use Anki flashcards for exam prep. Spaced repetition is a game changer! Went from 65% to 92% in DBMS.", "General", "Study Tips"},
            {"🏫 The new library hours are 7 AM - 11 PM now! Perfect for late-night study sessions. See you all there!", "General", "Campus News"},
            {"💻 Looking for teammates for Google Solution Challenge 2026! Need: 1 ML dev, 1 Backend, 1 Designer. DM me if interested!", "CSE", "Hackathon"},
            {"☕ Discovered the best chai spot near Gate 3 — Sharma ji ka dhaba. Only ₹15 for cutting chai + bun maska. You're welcome 😄", "General", "Food"},
            {"📊 Sharing my notes for Operating Systems — Chapter 5 (Process Scheduling). Upvote if useful!", "CSE", "Operating Systems"},
            {"🎉 Placement season update: 15 companies visiting next month! Amazon, Flipkart, and Microsoft confirmed. Start grinding LeetCode!", "General", "Placements"},
            {"🔬 Physics lab viva tomorrow. Who has the experiment list? Let's prepare together at the hostel common room — 8 PM!", "Physics", "Lab Prep"},
            {"🎨 Our college fest \"Technowave 2026\" registrations are OPEN! Events: Hackathon, Robo-Wars, Cultural Night. Link in bio 🔥", "General", "Fest"},
            {"📱 Built my first Android app using CampusConnect! It's a to-do list with Supabase sync. MVVM architecture FTW! 💪", "CSE", "Android Dev"},
            {"🤝 Shoutout to the seniors who organized the mock interview drive. Got amazing feedback on my resume!", "General", "Career"},
            {"📝 Mid-sem timetable is out! First exam: Engineering Mathematics on May 5th. RIP sleep schedule 😅", "General", "Exams"},
        };

        for (String[] post : posts) {
            JsonObject body = new JsonObject();
            body.addProperty("author_id", userId);
            body.addProperty("content", post[0]);
            body.addProperty("branch", post[1]);
            body.addProperty("subject", post[2]);
            body.addProperty("upvotes", (int)(Math.random() * 25) + 3);

            postToSupabase("posts", body.toString());
            try { Thread.sleep(100); } catch (InterruptedException ignored) {} // Small delay to avoid rate limits
        }
    }

    private void postToSupabase(String table, String jsonBody) throws IOException {
        String url = Constants.REST_URL + table;
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "";
                Log.w(TAG, "Seed " + table + " failed: " + response.code() + " - " + errBody);
            }
        }
    }
}
