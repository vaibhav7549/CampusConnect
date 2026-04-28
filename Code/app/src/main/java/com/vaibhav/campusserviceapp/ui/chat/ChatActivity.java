package com.vaibhav.campusserviceapp.ui.chat;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.vaibhav.campusserviceapp.databinding.ActivityChatBinding;
import com.vaibhav.campusserviceapp.models.Message;
import com.vaibhav.campusserviceapp.network.RealtimeClient;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.MessageAdapter;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.ChatViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private ChatViewModel chatViewModel;
    private MessageAdapter messageAdapter;
    private RealtimeClient realtimeClient;
    private String chatId;
    private SessionManager sessionManager;
    private Handler mainHandler;
    private Uri selectedImageUri;
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                selectedImageUri = uri;
                binding.cardPreview.setVisibility(android.view.View.VISIBLE);
                Glide.with(this).load(uri).centerCrop().into(binding.ivPreviewImage);
            });
    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            loadMessages();
            mainHandler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatId = getIntent().getStringExtra("chat_id");
        String otherUserName = getIntent().getStringExtra("other_user_name");
        sessionManager = new SessionManager(this);
        mainHandler = new Handler(Looper.getMainLooper());

        binding.toolbar.setTitle(otherUserName != null ? otherUserName : "Chat");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        messageAdapter = new MessageAdapter(sessionManager.getUserId());

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(llm);
        binding.recyclerMessages.setAdapter(messageAdapter);

        loadMessages();
        setupRealtime();

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.btnAttach.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.btnRemovePreview.setOnClickListener(v -> clearSelectedImage());

        // Mark as read
        chatViewModel.markMessagesRead(chatId);
    }

    private void loadMessages() {
        chatViewModel.getMessages(chatId).observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                messageAdapter.setMessages(resource.data);
                binding.recyclerMessages.scrollToPosition(resource.data.size() - 1);
            }
        });
    }

    private void setupRealtime() {
        realtimeClient = new RealtimeClient(SupabaseClient.getInstance(this).getOkHttpClient());
        realtimeClient.connect(chatId, message -> {
            // Don't add our own messages twice
            if (!message.getSenderId().equals(sessionManager.getUserId())) {
                mainHandler.post(() -> {
                    messageAdapter.addMessage(message);
                    binding.recyclerMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                });
            }
        });
        mainHandler.postDelayed(pollingRunnable, 3000);
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        File imageFile = selectedImageUri != null ? copyUriToTempFile(selectedImageUri) : null;
        if (text.isEmpty() && imageFile == null) return;

        binding.etMessage.setText("");
        clearSelectedImage();
        chatViewModel.sendMessage(chatId, text, imageFile).observe(this, resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                // Message sent, will appear via realtime or reload
                loadMessages();
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message != null ? resource.message : "Failed to send", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearSelectedImage() {
        selectedImageUri = null;
        binding.cardPreview.setVisibility(android.view.View.GONE);
        binding.ivPreviewImage.setImageDrawable(null);
    }

    private File copyUriToTempFile(Uri uri) {
        try {
            File temp = File.createTempFile("chat_", ".jpg", getCacheDir());
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(temp);
            byte[] buffer = new byte[4096];
            int len;
            while (is != null && (len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            if (is != null) is.close();
            return temp;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacks(pollingRunnable);
        if (realtimeClient != null) {
            realtimeClient.disconnect();
        }
    }
}
