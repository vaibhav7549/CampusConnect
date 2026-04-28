package com.vaibhav.campusserviceapp.ui.productivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.textfield.TextInputEditText;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.databinding.FragmentTodoBinding;
import com.vaibhav.campusserviceapp.models.Todo;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.TodoAdapter;
import com.vaibhav.campusserviceapp.viewmodels.TodoViewModel;
import java.util.Calendar;

public class TodoSubFragment extends Fragment implements TodoAdapter.OnTodoActionListener {
    private FragmentTodoBinding binding;
    private TodoViewModel todoViewModel;
    private TodoAdapter todoAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTodoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        todoViewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        todoAdapter = new TodoAdapter(this);
        binding.recyclerTodos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTodos.setAdapter(todoAdapter);

        loadTodos();

        binding.fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void loadTodos() {
        binding.progressBar.setVisibility(View.VISIBLE);
        todoViewModel.getTodos().observe(getViewLifecycleOwner(), resource -> {
            binding.progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                todoAdapter.setTodos(resource.data);
                binding.tvEmpty.setVisibility(resource.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_note, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etDate = dialogView.findViewById(R.id.etDate);

        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (dp, year, month, day) -> {
                etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_note)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    if (!title.isEmpty()) {
                        todoViewModel.addTodo(title, date.isEmpty() ? null : date).observe(getViewLifecycleOwner(), resource -> {
                            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                                loadTodos();
                            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onToggleComplete(Todo todo, boolean isComplete) {
        todoViewModel.toggleComplete(todo.getId(), isComplete).observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                loadTodos();
            }
        });
    }

    @Override
    public void onDelete(Todo todo) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage("Delete \"" + todo.getTitle() + "\"?")
                .setPositiveButton(R.string.delete, (d, w) -> {
                    todoViewModel.deleteTodo(todo.getId()).observe(getViewLifecycleOwner(), resource -> {
                        if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                            loadTodos();
                        } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
