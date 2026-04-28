package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.Chat;
import com.vaibhav.campusserviceapp.models.Message;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.ChatRepository;
import java.io.File;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository chatRepository;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Chat>>> getUserChats() {
        return chatRepository.getUserChats();
    }

    public LiveData<AuthRepository.Resource<Chat>> findOrCreateChat(String otherUserId) {
        return chatRepository.findOrCreateChat(otherUserId);
    }

    public LiveData<AuthRepository.Resource<List<Message>>> getMessages(String chatId) {
        return chatRepository.getMessages(chatId);
    }

    public LiveData<AuthRepository.Resource<Void>> sendMessage(String chatId, String text, File imageFile) {
        return chatRepository.sendMessage(chatId, text, imageFile);
    }

    public void markMessagesRead(String chatId) {
        chatRepository.markMessagesRead(chatId);
    }

    public void addReaction(String messageId, String emoji) {
        chatRepository.addReaction(messageId, emoji);
    }
}
