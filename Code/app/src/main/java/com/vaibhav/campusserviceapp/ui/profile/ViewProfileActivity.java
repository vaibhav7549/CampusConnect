package com.vaibhav.campusserviceapp.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.PeopleViewModel;
import com.vaibhav.campusserviceapp.viewmodels.ProfileViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {
    private ProfileViewModel profileViewModel;
    private PeopleViewModel peopleViewModel;
    private com.vaibhav.campusserviceapp.viewmodels.ChatViewModel chatViewModel;
    private SessionManager sessionManager;
    private String userId;
    private boolean isSelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        sessionManager = new SessionManager(this);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        peopleViewModel = new ViewModelProvider(this).get(PeopleViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(com.vaibhav.campusserviceapp.viewmodels.ChatViewModel.class);

        userId = getIntent().getStringExtra("user_id");
        if (userId == null) userId = sessionManager.getUserId();
        isSelf = userId.equals(sessionManager.getUserId());

        ImageView btnBack = findViewById(R.id.btnBack);
        CircleImageView ivAvatar = findViewById(R.id.ivAvatar);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvBio = findViewById(R.id.tvBio);
        ImageView ivVerified = findViewById(R.id.ivVerified);
        TextView tvPostCount = findViewById(R.id.tvPostCount);
        TextView tvFriendCount = findViewById(R.id.tvFriendCount);
        MaterialButton btnAction = findViewById(R.id.btnAction);
        TextView tvCollege = findViewById(R.id.tvCollege);
        TextView tvBranch = findViewById(R.id.tvBranch);
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvEmail = findViewById(R.id.tvEmail);
        MaterialCardView cardEmail = findViewById(R.id.cardEmail);

        btnBack.setOnClickListener(v -> finish());
        animateEntrance(ivAvatar, tvName, tvBio, btnAction);

        // Load profile from DB
        profileViewModel.getProfile(userId).observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                User user = resource.data;
                tvName.setText(user.getName() != null ? user.getName() : "User");
                tvCollege.setText(user.getCollege() != null ? user.getCollege() : "—");
                tvBranch.setText(user.getBranch() != null ? user.getBranch() : "—");
                tvYear.setText(user.getYear() > 0 ? "Year " + user.getYear() : "—");

                if (user.getBio() != null && !user.getBio().isEmpty()) {
                    tvBio.setText(user.getBio());
                    tvBio.setVisibility(View.VISIBLE);
                }

                if (user.isVerified()) {
                    ivVerified.setVisibility(View.VISIBLE);
                }

                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    Glide.with(this).load(user.getPhotoUrl())
                            .placeholder(R.drawable.placeholder_avatar)
                            .circleCrop().into(ivAvatar);
                }

                if (isSelf && user.getCollegeEmail() != null && !user.getCollegeEmail().isEmpty()) {
                    tvEmail.setText(user.getCollegeEmail());
                    cardEmail.setVisibility(View.VISIBLE);
                }
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Action button logic
        if (isSelf) {
            btnAction.setText(R.string.edit_profile);
            btnAction.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, ProfileActivity.class));
            });
        } else {
            btnAction.setEnabled(false);
            btnAction.setText("Loading...");

            // Check if already friends
            peopleViewModel.checkFriendship(userId).observe(this, resource -> {
                if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                    if (resource.data != null && resource.data) {
                        btnAction.setText("Message");
                        btnAction.setEnabled(true);
                        btnAction.setOnClickListener(v -> {
                            btnAction.setEnabled(false);
                            btnAction.setText("Starting...");
                            chatViewModel.findOrCreateChat(userId).observe(this, chatResource -> {
                                if (chatResource.status == AuthRepository.Resource.Status.SUCCESS && chatResource.data != null) {
                                    btnAction.setEnabled(true);
                                    btnAction.setText("Message");
                                    android.content.Intent intent = new android.content.Intent(this, com.vaibhav.campusserviceapp.ui.chat.ChatActivity.class);
                                    intent.putExtra("chat_id", chatResource.data.getId());
                                    intent.putExtra("other_user_name", tvName.getText().toString());
                                    startActivity(intent);
                                } else if (chatResource.status == AuthRepository.Resource.Status.ERROR) {
                                    btnAction.setEnabled(true);
                                    btnAction.setText("Message");
                                    Toast.makeText(this, chatResource.message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    } else {
                        // Check if request already sent
                        peopleViewModel.checkRequestSent(userId).observe(this, reqResource -> {
                            if (reqResource.status == AuthRepository.Resource.Status.SUCCESS) {
                                if (reqResource.data != null && reqResource.data) {
                                    btnAction.setText(R.string.request_sent);
                                    btnAction.setEnabled(false);
                                } else {
                                    btnAction.setText(R.string.send_request);
                                    btnAction.setEnabled(true);
                                    btnAction.setOnClickListener(v -> {
                                        peopleViewModel.sendFriendRequest(userId).observe(this, sendRes -> {
                                            if (sendRes.status == AuthRepository.Resource.Status.SUCCESS) {
                                                Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                                                btnAction.setText(R.string.request_sent);
                                                btnAction.setEnabled(false);
                                            } else if (sendRes.status == AuthRepository.Resource.Status.ERROR) {
                                                Toast.makeText(this, sendRes.message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        // Friend count
        peopleViewModel.getFriendsList().observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                tvFriendCount.setText(String.valueOf(resource.data.size()));
            }
        });
    }

    private void animateEntrance(View... views) {
        long delay = 0;
        for (View view : views) {
            if (view == null) continue;
            view.setAlpha(0f);
            view.setTranslationY(24f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(320)
                    .start();
            delay += 70;
        }
    }
}
