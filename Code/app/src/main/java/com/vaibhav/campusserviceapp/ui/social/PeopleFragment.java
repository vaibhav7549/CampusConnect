package com.vaibhav.campusserviceapp.ui.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.vaibhav.campusserviceapp.databinding.FragmentPeopleBinding;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.UserAdapter;
import com.vaibhav.campusserviceapp.ui.profile.ViewProfileActivity;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.PeopleViewModel;

public class PeopleFragment extends Fragment implements UserAdapter.OnUserActionListener {
    private FragmentPeopleBinding binding;
    private PeopleViewModel peopleViewModel;
    private UserAdapter userAdapter;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPeopleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        peopleViewModel = new ViewModelProvider(this).get(PeopleViewModel.class);

        userAdapter = new UserAdapter(requireContext(), this);
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUsers.setAdapter(userAdapter);

        loadUsers();

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = binding.etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsers(query);
            } else {
                loadUsers();
            }
            return true;
        });

        binding.btnRequests.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), FriendRequestsActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        if (sessionManager.getUserId() == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        
        peopleViewModel.getFriendsList().observe(getViewLifecycleOwner(), friendResource -> {
            if (friendResource.status == AuthRepository.Resource.Status.SUCCESS || friendResource.status == AuthRepository.Resource.Status.ERROR) {
                java.util.List<String> friendIds = new java.util.ArrayList<>();
                if (friendResource.data != null) {
                    for (com.vaibhav.campusserviceapp.models.Friend f : friendResource.data) {
                        friendIds.add(f.getFriendId());
                    }
                }
                
                peopleViewModel.getAllUsers(sessionManager.getUserId()).observe(getViewLifecycleOwner(), resource -> {
                    if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (resource.data != null) {
                            java.util.List<User> filtered = new java.util.ArrayList<>();
                            for (User u : resource.data) {
                                if (!friendIds.contains(u.getId())) {
                                    filtered.add(u);
                                }
                            }
                            userAdapter.setUsers(filtered);
                        }
                    } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void searchUsers(String query) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        peopleViewModel.getFriendsList().observe(getViewLifecycleOwner(), friendResource -> {
            if (friendResource.status == AuthRepository.Resource.Status.SUCCESS || friendResource.status == AuthRepository.Resource.Status.ERROR) {
                java.util.List<String> friendIds = new java.util.ArrayList<>();
                if (friendResource.data != null) {
                    for (com.vaibhav.campusserviceapp.models.Friend f : friendResource.data) {
                        friendIds.add(f.getFriendId());
                    }
                }
                
                peopleViewModel.searchUsers(query, sessionManager.getUserId()).observe(getViewLifecycleOwner(), resource -> {
                    if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (resource.data != null) {
                            java.util.List<User> filtered = new java.util.ArrayList<>();
                            for (User u : resource.data) {
                                if (!friendIds.contains(u.getId())) {
                                    filtered.add(u);
                                }
                            }
                            userAdapter.setUsers(filtered);
                        }
                    } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        // Open Instagram-style profile view
        Intent intent = new Intent(requireContext(), ViewProfileActivity.class);
        intent.putExtra("user_id", user.getId());
        startActivity(intent);
    }

    @Override
    public void onActionClick(User user, int position) {
        peopleViewModel.sendFriendRequest(user.getId()).observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Friend request sent!", Toast.LENGTH_SHORT).show();
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
