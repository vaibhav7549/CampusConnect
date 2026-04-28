package com.vaibhav.campusserviceapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.vaibhav.campusserviceapp.MainActivity;
import com.vaibhav.campusserviceapp.databinding.ActivityLoginBinding;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.ProfileRepository;
import com.vaibhav.campusserviceapp.viewmodels.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> login());
        binding.tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });
        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void login() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        authViewModel.signIn(email, password).observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                // Load profile to check if setup is complete
                ProfileRepository profileRepo = new ProfileRepository(this);
                String uid = resource.data.getUser().getId();
                profileRepo.getProfile(uid).observe(this, profileResource -> {
                    if (profileResource.status == AuthRepository.Resource.Status.SUCCESS && profileResource.data != null) {
                        if (profileResource.data.getName() == null || profileResource.data.getName().isEmpty()) {
                            startActivity(new Intent(this, ProfileSetupActivity.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                        }
                    } else {
                        startActivity(new Intent(this, ProfileSetupActivity.class));
                    }
                    finish();
                });
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                binding.btnLogin.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
