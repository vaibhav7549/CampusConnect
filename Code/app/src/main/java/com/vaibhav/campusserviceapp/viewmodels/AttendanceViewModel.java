package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.Attendance;
import com.vaibhav.campusserviceapp.repositories.AttendanceRepository;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import java.util.List;

public class AttendanceViewModel extends AndroidViewModel {
    private final AttendanceRepository attendanceRepository;

    public AttendanceViewModel(@NonNull Application application) {
        super(application);
        attendanceRepository = new AttendanceRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Attendance>>> getAttendanceForSubject(String subject) { return attendanceRepository.getAttendanceForSubject(subject); }
    public LiveData<AuthRepository.Resource<float[]>> getAttendancePercentage(String subject) { return attendanceRepository.getAttendancePercentage(subject); }
    public LiveData<AuthRepository.Resource<Void>> markAttendance(String subject, String status) { return attendanceRepository.markAttendance(subject, status); }
}
