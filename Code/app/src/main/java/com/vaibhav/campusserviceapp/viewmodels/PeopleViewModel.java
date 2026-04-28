package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.Friend;
import com.vaibhav.campusserviceapp.models.FriendRequest;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.FriendRepository;
import com.vaibhav.campusserviceapp.repositories.ProfileRepository;
import java.util.List;

public class PeopleViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;
    private final FriendRepository friendRepository;

    public PeopleViewModel(@NonNull Application application) {
        super(application);
        profileRepository = new ProfileRepository(application);
        friendRepository = new FriendRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<User>>> getAllUsers(String currentUserId) {
        return profileRepository.getAllUsers(currentUserId);
    }

    public LiveData<AuthRepository.Resource<List<User>>> searchUsers(String query, String currentUserId) {
        return profileRepository.searchUsers(query, currentUserId);
    }

    public LiveData<AuthRepository.Resource<Void>> sendFriendRequest(String toUid) {
        return friendRepository.sendFriendRequest(toUid);
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkFriendship(String otherUserId) {
        return friendRepository.checkFriendship(otherUserId);
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkRequestSent(String toUid) {
        return friendRepository.checkRequestSent(toUid);
    }

    public LiveData<AuthRepository.Resource<List<FriendRequest>>> getReceivedRequests() {
        return friendRepository.getReceivedRequests();
    }

    public LiveData<AuthRepository.Resource<Void>> acceptRequest(String requestId, String fromUid) {
        return friendRepository.acceptRequest(requestId, fromUid);
    }

    public LiveData<AuthRepository.Resource<Void>> rejectRequest(String requestId) {
        return friendRepository.rejectRequest(requestId);
    }

    public LiveData<AuthRepository.Resource<List<Friend>>> getFriendsList() {
        return friendRepository.getFriendsList();
    }
}
