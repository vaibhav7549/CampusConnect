package com.vaibhav.campusserviceapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    // ── Token Management ──────────────────────────────

    public void saveAuthTokens(String accessToken, String refreshToken) {
        editor.putString(Constants.PREF_ACCESS_TOKEN, accessToken);
        editor.putString(Constants.PREF_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public String getAccessToken() {
        return prefs.getString(Constants.PREF_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.PREF_REFRESH_TOKEN, null);
    }

    // ── User Info ─────────────────────────────────────

    public void saveUserId(String userId) {
        editor.putString(Constants.PREF_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.PREF_USER_ID, null);
    }

    public void saveUserName(String name) {
        editor.putString(Constants.PREF_USER_NAME, name);
        editor.apply();
    }

    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, "");
    }

    public void saveUserPhoto(String photoUrl) {
        editor.putString(Constants.PREF_USER_PHOTO, photoUrl);
        editor.apply();
    }

    public String getUserPhoto() {
        return prefs.getString(Constants.PREF_USER_PHOTO, "");
    }

    public void saveUserEmail(String email) {
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, "");
    }

    // ── Attendance Subjects (stored locally) ──────────

    public void saveAttendanceSubjects(List<String> subjects) {
        String json = gson.toJson(subjects);
        editor.putString(Constants.PREF_ATTENDANCE_SUBJECTS, json);
        editor.apply();
    }

    public List<String> getAttendanceSubjects() {
        String json = prefs.getString(Constants.PREF_ATTENDANCE_SUBJECTS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void addAttendanceSubject(String subject) {
        List<String> subjects = getAttendanceSubjects();
        if (!subjects.contains(subject)) {
            subjects.add(subject);
            saveAttendanceSubjects(subjects);
        }
    }

    public void removeAttendanceSubject(String subject) {
        List<String> subjects = getAttendanceSubjects();
        subjects.remove(subject);
        saveAttendanceSubjects(subjects);
    }

    // ── Session State ─────────────────────────────────

    public boolean isLoggedIn() {
        return getAccessToken() != null && getUserId() != null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
