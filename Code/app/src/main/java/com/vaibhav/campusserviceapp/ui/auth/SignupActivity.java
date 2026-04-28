package com.vaibhav.campusserviceapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.vaibhav.campusserviceapp.databinding.ActivitySignupBinding;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.viewmodels.AuthViewModel;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnSignup.setOnClickListener(v -> signUp());
        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void signUp() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSignup.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        authViewModel.signUp(email, password).observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                startActivity(new Intent(this, ProfileSetupActivity.class));
                finish();
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                binding.btnSignup.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                if (resource.message != null && resource.message.toLowerCase().contains("verify your email")) {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            }
        });
    }
}
