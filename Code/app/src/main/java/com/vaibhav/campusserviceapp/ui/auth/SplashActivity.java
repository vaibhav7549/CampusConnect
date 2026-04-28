package com.vaibhav.campusserviceapp.ui.auth;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.vaibhav.campusserviceapp.MainActivity;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.AuthViewModel;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_MIN_DURATION = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager sessionManager = new SessionManager(this);
        long startTime = System.currentTimeMillis();

        // Animate splash elements
        View logo = findViewById(R.id.tvLogo);
        if (logo != null) {
            logo.setAlpha(0f);
            logo.setScaleX(0.7f);
            logo.setScaleY(0.7f);
            logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                // Try to validate/refresh token in background
                AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
                authViewModel.validateToken().observe(this, resource -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remainingDelay = Math.max(0, SPLASH_MIN_DURATION - elapsed);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                            navigateTo(MainActivity.class);
                        } else {
                            // Token refresh failed — go to login
                            navigateTo(LoginActivity.class);
                        }
                    }, remainingDelay);
                });
            } else {
                long elapsed = System.currentTimeMillis() - startTime;
                long remainingDelay = Math.max(0, SPLASH_MIN_DURATION - elapsed);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    navigateTo(LoginActivity.class);
                }, remainingDelay);
            }
        }, 300); // Small initial delay for animation
    }

    private void navigateTo(Class<?> target) {
        startActivity(new Intent(this, target));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
