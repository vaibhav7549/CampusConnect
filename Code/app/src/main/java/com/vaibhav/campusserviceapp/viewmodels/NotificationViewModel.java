package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.Notification;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.NotificationRepository;
import java.util.List;

public class NotificationViewModel extends AndroidViewModel {
    private final NotificationRepository notificationRepository;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = new NotificationRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Notification>>> getNotifications() { return notificationRepository.getNotifications(); }
    public LiveData<AuthRepository.Resource<Boolean>> hasUnread() { return notificationRepository.hasUnread(); }
    public LiveData<AuthRepository.Resource<Void>> markAllRead() { return notificationRepository.markAllRead(); }
}
