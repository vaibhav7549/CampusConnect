package com.vaibhav.campusserviceapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Todo;
import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<Todo> todos = new ArrayList<>();
    private final OnTodoActionListener listener;

    public interface OnTodoActionListener {
        void onToggleComplete(Todo todo, boolean isComplete);
        void onDelete(Todo todo);
    }

    public TodoAdapter(OnTodoActionListener listener) {
        this.listener = listener;
    }

    public void setTodos(List<Todo> newTodos) {
        this.todos = newTodos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todos.get(position);
        holder.tvTitle.setText(todo.getTitle());
        holder.tvDueDate.setText(todo.getDueDate() != null ? "Due: " + todo.getDueDate() : "");
        holder.cbComplete.setChecked(todo.isComplete());

        if (todo.isComplete()) {
            holder.tvTitle.setAlpha(0.5f);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setAlpha(1f);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.cbComplete.setOnCheckedChangeListener((btn, isChecked) -> {
            if (btn.isPressed()) {
                listener.onToggleComplete(todo, isChecked);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(todo);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbComplete;
        TextView tvTitle, tvDueDate;
        TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            cbComplete = itemView.findViewById(R.id.cbComplete);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
        }
    }
}
