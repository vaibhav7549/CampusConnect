package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaibhav.campusserviceapp.models.Chat;
import com.vaibhav.campusserviceapp.models.Message;
import com.vaibhav.campusserviceapp.network.ChatApi;
import com.vaibhav.campusserviceapp.network.ProfileApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.Constants;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ChatApi chatApi;
    private final ProfileApi profileApi;
    private final SessionManager sessionManager;
    private final OkHttpClient okHttpClient;
    private final Gson gson = new Gson();

    public ChatRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        chatApi = client.getChatApi();
        profileApi = client.getProfileApi();
        sessionManager = client.getSessionManager();
        okHttpClient = client.getOkHttpClient();
    }

    public LiveData<AuthRepository.Resource<List<Chat>>> getUserChats() {
        MutableLiveData<AuthRepository.Resource<List<Chat>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        String uid = sessionManager.getUserId();
        String orFilter = "(user1_id.eq." + uid + ",user2_id.eq." + uid + ")";

        chatApi.getUserChats(orFilter, "*", "last_message_at.desc").enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Chat> chats = response.body();
                    // Move profile fetching to background thread to avoid NetworkOnMainThreadException
                    new Thread(() -> {
                        for (Chat chat : chats) {
                            try {
                                String otherId = chat.getOtherParticipantId(uid);
                                if (otherId != null) {
                                    retrofit2.Response<java.util.List<com.vaibhav.campusserviceapp.models.User>> pResp = profileApi.getProfile("eq." + otherId, "id,name,photo_url,avatar_url").execute();
                                    if (pResp.isSuccessful() && pResp.body() != null && !pResp.body().isEmpty()) {
                                        chat.setOtherUser(pResp.body().get(0));
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        result.postValue(AuthRepository.Resource.success(chats));
                    }).start();
                } else {
                    result.setValue(AuthRepository.Resource.error(extractError(response)));
                }
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Chat>> findOrCreateChat(String otherUserId) {
        MutableLiveData<AuthRepository.Resource<Chat>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        String uid = sessionManager.getUserId();
        if (uid == null || otherUserId == null || otherUserId.trim().isEmpty()) {
            result.setValue(AuthRepository.Resource.error("Invalid user selected"));
            return result;
        }
        if (uid.equals(otherUserId)) {
            result.setValue(AuthRepository.Resource.error("Cannot chat with yourself"));
            return result;
        }

        String userOne = uid.compareTo(otherUserId) <= 0 ? uid : otherUserId;
        String userTwo = uid.compareTo(otherUserId) <= 0 ? otherUserId : uid;
        String orFilter = "(and(user1_id.eq." + uid + ",user2_id.eq." + otherUserId + "),and(user1_id.eq." + otherUserId + ",user2_id.eq." + uid + "))";

        chatApi.findChat(orFilter, "*").enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    result.setValue(AuthRepository.Resource.success(response.body().get(0)));
                } else {
                    // Create new chat
                    Chat newChat = new Chat();
                    newChat.setParticipantOne(userOne);
                    newChat.setParticipantTwo(userTwo);

                    chatApi.createChat(newChat, "return=representation").enqueue(new Callback<List<Chat>>() {
                        @Override
                        public void onResponse(Call<List<Chat>> call2, Response<List<Chat>> response2) {
                            if (response2.isSuccessful() && response2.body() != null && !response2.body().isEmpty()) {
                                result.setValue(AuthRepository.Resource.success(response2.body().get(0)));
                            } else {
                                chatApi.findChat(orFilter, "*").enqueue(new Callback<List<Chat>>() {
                                    @Override
                                    public void onResponse(Call<List<Chat>> call3, Response<List<Chat>> response3) {
                                        if (response3.isSuccessful() && response3.body() != null && !response3.body().isEmpty()) {
                                            result.setValue(AuthRepository.Resource.success(response3.body().get(0)));
                                        } else {
                                            result.setValue(AuthRepository.Resource.error(extractError(response2)));
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<List<Chat>> call3, Throwable t3) {
                                        result.setValue(AuthRepository.Resource.error("Failed to create chat"));
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Chat>> call2, Throwable t2) {
                            result.setValue(AuthRepository.Resource.error("Network error: " + t2.getMessage()));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<Message>>> getMessages(String chatId) {
        MutableLiveData<AuthRepository.Resource<List<Message>>> result = new MutableLiveData<>();

        chatApi.getMessages("eq." + chatId, "*,profiles!sender_id(id,name,photo_url,avatar_url)", "created_at.asc").enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load messages"));
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> sendMessage(String chatId, String text, File imageFile) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        if ((text == null || text.trim().isEmpty()) && imageFile == null) {
            result.setValue(AuthRepository.Resource.error("Message is empty"));
            return result;
        }

        new Thread(() -> {
            try {
                Message message = new Message();
                message.setRoomId(chatId);
                message.setSenderId(sessionManager.getUserId());
                message.setContent(text != null && !text.trim().isEmpty() ? text.trim() : null);

                if (imageFile != null) {
                    try {
                        String imagePath = "messages/" + chatId + "/" + UUID.randomUUID() + ".jpg";
                        String imageUrl = uploadFile(imageFile, Constants.BUCKET_CHAT_IMAGES, imagePath, "image/jpeg");
                        message.setImageUrl(imageUrl);
                    } catch (IOException e) {
                        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                            result.postValue(AuthRepository.Resource.error("Failed to upload chat image"));
                            return;
                        }
                    }
                }

                chatApi.sendMessage(message, "return=minimal").enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Map<String, Object> chatUpdate = new HashMap<>();
                            String preview = (text != null && !text.trim().isEmpty()) ? text.trim() : "Photo";
                            chatUpdate.put("last_message", preview);
                            chatUpdate.put("last_message_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(new java.util.Date()));

                            chatApi.updateChat("eq." + chatId, chatUpdate).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call2, Response<Void> response2) {}

                                @Override
                                public void onFailure(Call<Void> call2, Throwable t2) {}
                            });

                            result.postValue(AuthRepository.Resource.success(null));
                        } else {
                            result.postValue(AuthRepository.Resource.error(extractError(response)));
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        result.postValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
                    }
                });
            } catch (Exception e) {
                result.postValue(AuthRepository.Resource.error("Failed to send message: " + e.getMessage()));
            }
        }).start();

        return result;
    }

    public void markMessagesRead(String chatId) {
        String uid = sessionManager.getUserId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_read", true);

        chatApi.markMessagesRead("eq." + chatId, "neq." + uid, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {}

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    public void addReaction(String messageId, String emoji) {
        // Reaction column is not part of current schema snapshot.
    }

    private String uploadFile(File file, String bucket, String path, String contentType) throws IOException {
        String url = Constants.STORAGE_URL + "object/" + bucket + "/" + path;
        RequestBody body = RequestBody.create(file, MediaType.parse(contentType));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Content-Type", contentType)
                .addHeader("x-upsert", "true")
                .build();
        okhttp3.Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Upload failed: " + response.code());
        }
        return Constants.STORAGE_URL + "object/public/" + bucket + "/" + path;
    }

    private String extractError(Response<?> response) {
        try {
            if (response.errorBody() == null) return "Failed to send message";
            String raw = response.errorBody().string();
            JsonObject obj = gson.fromJson(raw, JsonObject.class);
            if (obj != null && obj.has("message")) return obj.get("message").getAsString();
            if (obj != null && obj.has("msg")) return obj.get("msg").getAsString();
            return raw;
        } catch (Exception e) {
            return "Failed to send message";
        }
    }
}
