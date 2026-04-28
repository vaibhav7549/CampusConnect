package com.vaibhav.campusserviceapp.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.auth.LoginActivity;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.AuthViewModel;
import com.vaibhav.campusserviceapp.viewmodels.ProfileViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private CircleImageView ivAvatar;
    private TextInputEditText etName, etBio, etCollege, etBranch, etYear, etCollegeEmail;
    private MaterialButton btnSave, btnLogout;
    private View progressBar;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this).load(selectedImageUri).circleCrop().into(ivAvatar);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        ImageView btnBack = findViewById(R.id.btnBack);
        ivAvatar = findViewById(R.id.ivAvatar);
        FloatingActionButton fabChangePhoto = findViewById(R.id.fabChangePhoto);
        etName = findViewById(R.id.etName);
        etBio = findViewById(R.id.etBio);
        etCollege = findViewById(R.id.etCollege);
        etBranch = findViewById(R.id.etBranch);
        etYear = findViewById(R.id.etYear);
        etCollegeEmail = findViewById(R.id.etCollegeEmail);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        // Load existing profile
        loadProfile();

        fabChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveProfile());

        btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
            sessionManager.clearSession();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = sessionManager.getUserId();
        profileViewModel.getProfile(uid).observe(this, resource -> {
            progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                User user = resource.data;
                etName.setText(user.getName());
                etBio.setText(user.getBio());
                etCollege.setText(user.getCollege());
                etBranch.setText(user.getBranch());
                if (user.getYear() > 0) etYear.setText(String.valueOf(user.getYear()));
                etCollegeEmail.setText(user.getCollegeEmail());

                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    Glide.with(this).load(user.getPhotoUrl())
                            .placeholder(R.drawable.placeholder_avatar)
                            .circleCrop().into(ivAvatar);
                }
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String college = etCollege.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String collegeEmail = etCollegeEmail.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        String uid = sessionManager.getUserId();

        // Upload avatar if changed
        if (selectedImageUri != null) {
            try {
                File tempFile = new File(getCacheDir(), "avatar_temp.jpg");
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                FileOutputStream fos = new FileOutputStream(tempFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
                fos.close();
                is.close();

                profileViewModel.uploadAvatar(uid, tempFile).observe(this, uploadRes -> {
                    if (uploadRes.status == AuthRepository.Resource.Status.SUCCESS) {
                        updateProfileFields(uid, name, bio, college, branch, yearStr, collegeEmail);
                    } else if (uploadRes.status == AuthRepository.Resource.Status.ERROR) {
                        // Still save other fields
                        updateProfileFields(uid, name, bio, college, branch, yearStr, collegeEmail);
                        Toast.makeText(this, "Avatar upload failed, other fields saved", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                updateProfileFields(uid, name, bio, college, branch, yearStr, collegeEmail);
            }
        } else {
            updateProfileFields(uid, name, bio, college, branch, yearStr, collegeEmail);
        }
    }

    private void updateProfileFields(String uid, String name, String bio, String college, String branch, String yearStr, String collegeEmail) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        updates.put("college", college);
        updates.put("branch", branch);
        if (!yearStr.isEmpty()) updates.put("year", Integer.parseInt(yearStr));
        if (!collegeEmail.isEmpty()) updates.put("college_email", collegeEmail);

        profileViewModel.updateProfile(uid, updates).observe(this, resource -> {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                sessionManager.saveUserName(name);
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();

                // Check verified
                if (!collegeEmail.isEmpty()) {
                    authViewModel.checkAndSetVerified(uid, collegeEmail);
                }

                finish();
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(this, "Update failed: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
