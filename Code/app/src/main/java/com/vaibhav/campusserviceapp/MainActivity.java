package com.vaibhav.campusserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.vaibhav.campusserviceapp.databinding.ActivityMainBinding;
import com.vaibhav.campusserviceapp.ui.auth.LoginActivity;
import com.vaibhav.campusserviceapp.ui.notifications.NotificationsActivity;
import com.vaibhav.campusserviceapp.ui.profile.ViewProfileActivity;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.NotificationViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);
        }

        // Toolbar menu
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            } else if (id == R.id.action_profile) {
                startActivity(new Intent(this, ViewProfileActivity.class));
                return true;
            }
            return false;
        });

        // Check for unread notifications badge
        checkUnreadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUnreadNotifications();
    }

    private void checkUnreadNotifications() {
        try {
            NotificationViewModel notifVM = new ViewModelProvider(this).get(NotificationViewModel.class);
            notifVM.hasUnread().observe(this, resource -> {
                // Badge logic could go here
            });
        } catch (Exception e) {
            // Notifications may not be set up yet
        }
    }

    public void logout() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.clearSession();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }
}