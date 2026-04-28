package com.vaibhav.campusserviceapp.ui.notifications;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.vaibhav.campusserviceapp.databinding.FragmentNotificationsBinding;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.NotificationAdapter;
import com.vaibhav.campusserviceapp.viewmodels.NotificationViewModel;

public class NotificationsActivity extends AppCompatActivity {
    private FragmentNotificationsBinding binding;
    private NotificationViewModel notificationViewModel;
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        notificationAdapter = new NotificationAdapter();
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerNotifications.setAdapter(notificationAdapter);

        loadNotifications();

        binding.btnMarkRead.setOnClickListener(v -> {
            notificationViewModel.markAllRead().observe(this, resource -> {
                if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                    loadNotifications();
                }
            });
        });
    }

    private void loadNotifications() {
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        notificationViewModel.getNotifications().observe(this, resource -> {
            binding.progressBar.setVisibility(android.view.View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                notificationAdapter.setNotifications(resource.data);
                binding.tvEmpty.setVisibility(resource.data.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
