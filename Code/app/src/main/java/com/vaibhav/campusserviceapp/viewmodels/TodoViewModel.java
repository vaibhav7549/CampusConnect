package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.Todo;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.TodoRepository;
import java.util.List;

public class TodoViewModel extends AndroidViewModel {
    private final TodoRepository todoRepository;

    public TodoViewModel(@NonNull Application application) {
        super(application);
        todoRepository = new TodoRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Todo>>> getTodos() { return todoRepository.getTodos(); }
    public LiveData<AuthRepository.Resource<Void>> addTodo(String title, String dueDate) { return todoRepository.addTodo(title, dueDate); }
    public LiveData<AuthRepository.Resource<Void>> toggleComplete(String id, boolean isComplete) { return todoRepository.toggleComplete(id, isComplete); }
    public LiveData<AuthRepository.Resource<Void>> deleteTodo(String id) { return todoRepository.deleteTodo(id); }
}
