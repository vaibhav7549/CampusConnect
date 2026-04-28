package com.vaibhav.campusserviceapp.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.User;
import com.vaibhav.campusserviceapp.models.Friend;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.UserAdapter;
import com.vaibhav.campusserviceapp.ui.profile.ViewProfileActivity;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.ChatViewModel;
import com.vaibhav.campusserviceapp.viewmodels.PeopleViewModel;
import java.util.ArrayList;
import java.util.List;

public class FriendsSubFragment extends Fragment implements UserAdapter.OnUserActionListener {
    private PeopleViewModel peopleViewModel;
    private ChatViewModel chatViewModel;
    private UserAdapter userAdapter;
    private SessionManager sessionManager;
    private RecyclerView recyclerFriends;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerFriends = view.findViewById(R.id.recyclerFriends);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        sessionManager = new SessionManager(requireContext());
        peopleViewModel = new ViewModelProvider(this).get(PeopleViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        userAdapter = new UserAdapter(requireContext(), this);
        userAdapter.setActionLabel("Message");
        recyclerFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerFriends.setAdapter(userAdapter);
        
        loadFriends();
    }

    private void loadFriends() {
        progressBar.setVisibility(View.VISIBLE);
        peopleViewModel.getFriendsList().observe(getViewLifecycleOwner(), friendResource -> {
            if (friendResource.status == AuthRepository.Resource.Status.SUCCESS) {
                List<String> friendIds = new ArrayList<>();
                if (friendResource.data != null) {
                    for (Friend f : friendResource.data) {
                        friendIds.add(f.getFriendId());
                    }
                }
                
                if (friendIds.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    userAdapter.setUsers(new ArrayList<>());
                    return;
                }
                
                // Fetch user data for all users and filter
                peopleViewModel.getAllUsers(sessionManager.getUserId()).observe(getViewLifecycleOwner(), resource -> {
                    progressBar.setVisibility(View.GONE);
                    if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                        List<User> filtered = new ArrayList<>();
                        for (User u : resource.data) {
                            if (friendIds.contains(u.getId())) {
                                filtered.add(u);
                            }
                        }
                        userAdapter.setUsers(filtered);
                        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            } else if (friendResource.status == AuthRepository.Resource.Status.ERROR) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load friends", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        // Tapping a friend opens their profile
        Intent intent = new Intent(requireContext(), ViewProfileActivity.class);
        intent.putExtra("user_id", user.getId());
        startActivity(intent);
    }

    @Override
    public void onActionClick(User user, int position) {
        // "Message" button - directly open/create chat with this friend
        openChatWithUser(user);
    }

    private void openChatWithUser(User user) {
        progressBar.setVisibility(View.VISIBLE);
        chatViewModel.findOrCreateChat(user.getId()).observe(getViewLifecycleOwner(), resource -> {
            progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("chat_id", resource.data.getId());
                intent.putExtra("other_user_name", user.getName() != null ? user.getName() : "User");
                startActivity(intent);
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message != null ? resource.message : "Failed to open chat", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadFriends(); // Reload if new friends added
    }
}
