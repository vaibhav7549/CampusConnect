package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vaibhav.campusserviceapp.models.Todo;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.network.TodoApi;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TodoRepository {
    private final TodoApi todoApi;
    private final SessionManager sessionManager;

    public TodoRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        todoApi = client.getTodoApi();
        sessionManager = client.getSessionManager();
    }

    public LiveData<AuthRepository.Resource<List<Todo>>> getTodos() {
        MutableLiveData<AuthRepository.Resource<List<Todo>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        todoApi.getTodos("eq." + sessionManager.getUserId(), "id.desc").enqueue(new Callback<List<Todo>>() {
            @Override
            public void onResponse(Call<List<Todo>> call, Response<List<Todo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error(extractError(response)));
                }
            }

            @Override
            public void onFailure(Call<List<Todo>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    private String extractError(Response<?> response) {
        try {
            if (response.errorBody() == null) return "Failed to load todos";
            String raw = response.errorBody().string();
            return raw != null && !raw.isEmpty() ? raw : "Failed to load todos";
        } catch (Exception e) {
            return "Failed to load todos";
        }
    }

    public LiveData<AuthRepository.Resource<Void>> addTodo(String title, String dueDate) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", sessionManager.getUserId());
        payload.put("title", title);
        if (dueDate != null && !dueDate.isEmpty()) {
            // due_date is DATE type in DB, send as YYYY-MM-DD
            payload.put("due_date", dueDate);
        }

        todoApi.addTodo(payload, "return=minimal").enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(response.isSuccessful() ? AuthRepository.Resource.success(null) : AuthRepository.Resource.error("Failed to add todo"));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> toggleComplete(String todoId, boolean isComplete) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("is_complete", isComplete);

        todoApi.updateTodo("eq." + todoId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to update todo"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> deleteTodo(String todoId) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        todoApi.deleteTodo("eq." + todoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(response.isSuccessful() ? AuthRepository.Resource.success(null) : AuthRepository.Resource.error("Failed"));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }
}
