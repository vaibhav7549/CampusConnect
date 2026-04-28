package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vaibhav.campusserviceapp.models.Exam;
import com.vaibhav.campusserviceapp.network.ExamApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamRepository {
    private final ExamApi examApi;
    private final SessionManager sessionManager;

    public ExamRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        examApi = client.getExamApi();
        sessionManager = client.getSessionManager();
    }

    public LiveData<AuthRepository.Resource<List<Exam>>> getExams() {
        MutableLiveData<AuthRepository.Resource<List<Exam>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        examApi.getExams("eq." + sessionManager.getUserId(), "exam_date.asc").enqueue(new Callback<List<Exam>>() {
            @Override
            public void onResponse(Call<List<Exam>> call, Response<List<Exam>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load exams"));
                }
            }

            @Override
            public void onFailure(Call<List<Exam>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> addExam(String subject, String examDate) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        Exam exam = new Exam();
        exam.setUserId(sessionManager.getUserId());
        exam.setSubject(subject);
        exam.setExamDate(examDate);

        examApi.addExam(exam, "return=minimal").enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(response.isSuccessful() ? AuthRepository.Resource.success(null) : AuthRepository.Resource.error("Failed"));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> deleteExam(String examId) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        examApi.deleteExam("eq." + examId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(response.isSuccessful() ? AuthRepository.Resource.success(null) : AuthRepository.Resource.error("Failed"));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }
}
