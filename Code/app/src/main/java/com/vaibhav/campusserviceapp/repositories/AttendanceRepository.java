package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vaibhav.campusserviceapp.models.Attendance;
import com.vaibhav.campusserviceapp.network.AttendanceApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceRepository {
    private final AttendanceApi attendanceApi;
    private final SessionManager sessionManager;

    public AttendanceRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        attendanceApi = client.getAttendanceApi();
        sessionManager = client.getSessionManager();
    }

    public LiveData<AuthRepository.Resource<List<Attendance>>> getAttendanceForSubject(String subject) {
        MutableLiveData<AuthRepository.Resource<List<Attendance>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        attendanceApi.getAttendance("eq." + sessionManager.getUserId(), "eq." + subject, "date.desc").enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load attendance"));
                }
            }

            @Override
            public void onFailure(Call<List<Attendance>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<float[]>> getAttendancePercentage(String subject) {
        MutableLiveData<AuthRepository.Resource<float[]>> result = new MutableLiveData<>();

        attendanceApi.getAllAttendanceForSubject("eq." + sessionManager.getUserId(), "eq." + subject, "status").enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Attendance> records = response.body();
                    int total = records.size();
                    int present = 0;
                    for (Attendance a : records) {
                        if ("present".equals(a.getStatus())) present++;
                    }
                    float percentage = total > 0 ? (present * 100f / total) : 0f;
                    result.setValue(AuthRepository.Resource.success(new float[]{percentage, present, total}));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed"));
                }
            }

            @Override
            public void onFailure(Call<List<Attendance>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> markAttendance(String subject, String status) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String uid = sessionManager.getUserId();

        // Check if record exists for today
        attendanceApi.getAttendanceForDate("eq." + uid, "eq." + subject, "eq." + today).enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Update existing
                    String id = response.body().get(0).getId();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", status);
                    attendanceApi.updateAttendance("eq." + id, updates).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> c, Response<Void> r) {
                            result.setValue(AuthRepository.Resource.success(null));
                        }

                        @Override
                        public void onFailure(Call<Void> c, Throwable t) {
                            result.setValue(AuthRepository.Resource.error("Failed"));
                        }
                    });
                } else {
                    // Insert new
                    Attendance attendance = new Attendance();
                    attendance.setUserId(uid);
                    attendance.setSubject(subject);
                    attendance.setDate(today);
                    attendance.setStatus(status);

                    attendanceApi.markAttendance(attendance, "return=minimal").enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> c, Response<Void> r) {
                            result.setValue(r.isSuccessful() ? AuthRepository.Resource.success(null) : AuthRepository.Resource.error("Failed"));
                        }

                        @Override
                        public void onFailure(Call<Void> c, Throwable t) {
                            result.setValue(AuthRepository.Resource.error("Failed"));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Attendance>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }
}
