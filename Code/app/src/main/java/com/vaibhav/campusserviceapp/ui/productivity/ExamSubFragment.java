package com.vaibhav.campusserviceapp.ui.productivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Exam;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.viewmodels.ExamViewModel;
import java.util.ArrayList;
import java.util.List;

public class ExamSubFragment extends Fragment {
    private ExamViewModel examViewModel;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        examViewModel = new ViewModelProvider(this).get(ExamViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerTodos);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        TextView tvEmpty = view.findViewById(R.id.tvEmpty);
        tvEmpty.setText(R.string.no_exams);

        FloatingActionButton fab = view.findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddExamDialog());

        loadExams(tvEmpty);
    }

    private void loadExams(TextView tvEmpty) {
        examViewModel.getExams().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                recyclerView.setAdapter(new SimpleExamAdapter(resource.data));
                tvEmpty.setVisibility(resource.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showAddExamDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_post, null);
        TextInputEditText etSubject = dialogView.findViewById(R.id.etSubject);
        TextInputEditText etDate = dialogView.findViewById(R.id.etContent);
        etSubject.setHint("Exam Subject");
        etDate.setHint("Date (YYYY-MM-DD)");

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_exam)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (d, w) -> {
                    String subject = etSubject.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    if (!subject.isEmpty() && !date.isEmpty()) {
                        examViewModel.addExam(subject, date);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Simple inner adapter for exams
    private static class SimpleExamAdapter extends RecyclerView.Adapter<SimpleExamAdapter.VH> {
        private final List<Exam> exams;
        SimpleExamAdapter(List<Exam> exams) { this.exams = exams; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Exam exam = exams.get(position);
            holder.tvSubject.setText(exam.getSubject());
            holder.tvDate.setText(exam.getExamDate());

            // Simple countdown
            try {
                long examTime = java.text.DateFormat.getDateInstance().parse(exam.getExamDate()).getTime();
                long diff = examTime - System.currentTimeMillis();
                long days = diff / (1000 * 60 * 60 * 24);
                holder.tvCountdown.setText(days > 0 ? days + " days left" : "Today!");
            } catch (Exception e) {
                holder.tvCountdown.setText(exam.getExamDate());
            }
        }

        @Override
        public int getItemCount() { return exams.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvSubject, tvCountdown, tvDate;
            VH(@NonNull View itemView) {
                super(itemView);
                tvSubject = itemView.findViewById(R.id.tvSubject);
                tvCountdown = itemView.findViewById(R.id.tvCountdown);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }
}
