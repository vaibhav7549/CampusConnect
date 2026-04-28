package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.ProfileRepository;
import java.io.File;
import java.util.Map;

public class ProfileViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        profileRepository = new ProfileRepository(application);
    }

    public LiveData<AuthRepository.Resource<User>> getProfile(String userId) {
        return profileRepository.getProfile(userId);
    }

    public LiveData<AuthRepository.Resource<Void>> updateProfile(String userId, Map<String, Object> updates) {
        return profileRepository.updateProfile(userId, updates);
    }

    public LiveData<AuthRepository.Resource<String>> uploadAvatar(String userId, File file) {
        return profileRepository.uploadAvatar(userId, file);
    }
}
