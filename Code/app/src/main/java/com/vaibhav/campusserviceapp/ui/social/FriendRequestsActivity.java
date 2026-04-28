package com.vaibhav.campusserviceapp.ui.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.FriendRequest;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.FriendRequestAdapter;
import com.vaibhav.campusserviceapp.ui.profile.ViewProfileActivity;
import com.vaibhav.campusserviceapp.viewmodels.PeopleViewModel;

public class FriendRequestsActivity extends AppCompatActivity implements FriendRequestAdapter.OnRequestActionListener {
    private PeopleViewModel peopleViewModel;
    private FriendRequestAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        peopleViewModel = new ViewModelProvider(this).get(PeopleViewModel.class);

        ImageView btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView recycler = findViewById(R.id.recyclerRequests);

        btnBack.setOnClickListener(v -> finish());

        adapter = new FriendRequestAdapter(this, this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);
        peopleViewModel.getReceivedRequests().observe(this, resource -> {
            progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                adapter.setRequests(resource.data);
                tvEmpty.setVisibility(resource.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAccept(FriendRequest request, int position) {
        peopleViewModel.acceptRequest(request.getId(), request.getFromUid()).observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                Toast.makeText(this, "Friend request accepted!", Toast.LENGTH_SHORT).show();
                adapter.removeItem(position);
                if (adapter.getItemCount() == 0) tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onReject(FriendRequest request, int position) {
        peopleViewModel.rejectRequest(request.getId()).observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                adapter.removeItem(position);
                if (adapter.getItemCount() == 0) tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onUserClick(FriendRequest request) {
        if (request.getFromUid() != null) {
            Intent intent = new Intent(this, ViewProfileActivity.class);
            intent.putExtra("user_id", request.getFromUid());
            startActivity(intent);
        }
    }
}
