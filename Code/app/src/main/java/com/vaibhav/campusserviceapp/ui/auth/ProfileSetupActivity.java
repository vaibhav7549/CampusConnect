package com.vaibhav.campusserviceapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.vaibhav.campusserviceapp.MainActivity;
import com.vaibhav.campusserviceapp.databinding.ActivityProfileSetupBinding;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.AuthViewModel;
import com.vaibhav.campusserviceapp.viewmodels.ProfileViewModel;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {
    private ActivityProfileSetupBinding binding;
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        sessionManager = new SessionManager(this);

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String college = binding.etCollege.getText().toString().trim();
        String branch = binding.etBranch.getText().toString().trim();
        String yearStr = binding.etYear.getText().toString().trim();
        String collegeEmail = binding.etCollegeEmail.getText().toString().trim();

        if (name.isEmpty() || college.isEmpty() || branch.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int year = Integer.parseInt(yearStr);
        String uid = sessionManager.getUserId();

        binding.btnSave.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // First try to insert profile (for new users)
        User profile = new User(uid, name, college, branch, year, collegeEmail);
        authViewModel.insertProfile(profile);

        // Then update (in case profile already exists)
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("college", college);
        updates.put("branch", branch);
        updates.put("year", year);
        if (!collegeEmail.isEmpty()) {
            updates.put("college_email", collegeEmail);
        }

        profileViewModel.updateProfile(uid, updates).observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                sessionManager.saveUserName(name);

                // Check verified status
                if (!collegeEmail.isEmpty()) {
                    authViewModel.checkAndSetVerified(uid, collegeEmail);
                }

                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                // Profile might have been inserted, go ahead anyway
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }
}
