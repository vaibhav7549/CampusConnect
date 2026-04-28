package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.Exam;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.ExamRepository;
import java.util.List;

public class ExamViewModel extends AndroidViewModel {
    private final ExamRepository examRepository;

    public ExamViewModel(@NonNull Application application) {
        super(application);
        examRepository = new ExamRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Exam>>> getExams() { return examRepository.getExams(); }
    public LiveData<AuthRepository.Resource<Void>> addExam(String subject, String examDate) { return examRepository.addExam(subject, examDate); }
    public LiveData<AuthRepository.Resource<Void>> deleteExam(String id) { return examRepository.deleteExam(id); }
}
