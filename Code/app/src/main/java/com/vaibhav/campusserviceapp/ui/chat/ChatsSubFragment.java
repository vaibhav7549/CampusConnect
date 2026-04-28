package com.vaibhav.campusserviceapp.ui.chat;

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
import com.vaibhav.campusserviceapp.databinding.FragmentChatsBinding;
import com.vaibhav.campusserviceapp.models.Chat;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.ChatAdapter;
import com.vaibhav.campusserviceapp.viewmodels.ChatViewModel;

public class ChatsSubFragment extends Fragment implements ChatAdapter.OnChatClickListener {
    private FragmentChatsBinding binding;
    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        chatAdapter = new ChatAdapter(requireContext(), this);
        binding.recyclerChats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerChats.setAdapter(chatAdapter);

        loadChats();
    }

    private void loadChats() {
        binding.progressBar.setVisibility(View.VISIBLE);
        chatViewModel.getUserChats().observe(getViewLifecycleOwner(), resource -> {
            binding.progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                chatAdapter.setChats(resource.data);
                binding.tvEmpty.setVisibility(resource.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChatClick(Chat chat) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("chat_id", chat.getId());
        String otherName = (chat.getOtherUser() != null) ? chat.getOtherUser().getName() : "User";
        intent.putExtra("other_user_name", otherName);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
