package com.vaibhav.campusserviceapp.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Reuse login layout, but repurpose

        // Simple dialog-style approach
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        TextInputEditText etEmail = findViewById(R.id.etEmail);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView tvTitle = null; // We'll use toast-based flow

        btnLogin.setText(R.string.reset_password);
        findViewById(R.id.tvSignup).setVisibility(View.GONE);
        findViewById(R.id.tvForgotPassword).setVisibility(View.GONE);
        findViewById(R.id.passwordLayout).setVisibility(View.GONE);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            btnLogin.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            authViewModel.recoverPassword(email).observe(this, resource -> {
                progressBar.setVisibility(View.GONE);
                if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                    Toast.makeText(this, getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show();
                    finish();
                } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
