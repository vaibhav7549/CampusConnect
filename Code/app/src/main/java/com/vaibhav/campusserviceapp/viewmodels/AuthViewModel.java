package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.AuthResponse;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<AuthRepository.Resource<AuthResponse>> signUp(String email, String password) {
        return authRepository.signUp(email, password);
    }

    public LiveData<AuthRepository.Resource<AuthResponse>> signIn(String email, String password) {
        return authRepository.signIn(email, password);
    }

    public LiveData<AuthRepository.Resource<Boolean>> validateToken() {
        return authRepository.validateToken();
    }

    public void insertProfile(User profile) {
        authRepository.insertProfile(profile);
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkAndSetVerified(String userId, String email) {
        return authRepository.checkAndSetVerified(userId, email);
    }

    public LiveData<AuthRepository.Resource<String>> recoverPassword(String email) {
        return authRepository.recoverPassword(email);
    }

    public void logout() {
        authRepository.logout();
    }
}
