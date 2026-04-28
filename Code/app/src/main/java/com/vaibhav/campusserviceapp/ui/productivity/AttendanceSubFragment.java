package com.vaibhav.campusserviceapp.ui.productivity;

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
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.viewmodels.AttendanceViewModel;
import java.util.Arrays;
import java.util.List;

public class AttendanceSubFragment extends Fragment {
    private AttendanceViewModel attendanceViewModel;
    private RecyclerView recyclerView;
    private List<String> subjects = Arrays.asList("Mathematics", "Physics", "Chemistry", "English", "CS");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerTodos);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        view.findViewById(R.id.tvEmpty).setVisibility(View.GONE);

        FloatingActionButton fab = view.findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddSubjectDialog());

        loadAttendance();
    }

    private void loadAttendance() {
        recyclerView.setAdapter(new AttendanceAdapter());
    }

    private void showAddSubjectDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_post, null);
        TextInputEditText etSubject = dialogView.findViewById(R.id.etSubject);
        etSubject.setHint("Subject Name");
        dialogView.findViewById(R.id.etContent).setVisibility(View.GONE);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_subject)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (d, w) -> {
                    String subject = etSubject.getText().toString().trim();
                    if (!subject.isEmpty() && !subjects.contains(subject)) {
                        subjects.add(subject);
                        loadAttendance();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_subject, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String subject = subjects.get(position);
            holder.tvSubject.setText(subject);

            attendanceViewModel.getAttendancePercentage(subject).observe(getViewLifecycleOwner(), resource -> {
                if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                    float percentage = resource.data[0];
                    holder.tvPercentage.setText(String.format("%.0f%%", percentage));
                    holder.progressAttendance.setProgress((int) percentage);

                    if (percentage < 75) {
                        holder.tvPercentage.setTextColor(requireContext().getColor(R.color.attendance_warning));
                        holder.progressAttendance.setIndicatorColor(requireContext().getColor(R.color.attendance_warning));
                        holder.tvWarning.setVisibility(View.VISIBLE);
                    } else {
                        holder.tvPercentage.setTextColor(requireContext().getColor(R.color.attendance_good));
                        holder.progressAttendance.setIndicatorColor(requireContext().getColor(R.color.attendance_good));
                        holder.tvWarning.setVisibility(View.GONE);
                    }
                } else {
                    holder.tvPercentage.setText("N/A");
                    holder.progressAttendance.setProgress(0);
                }
            });

            holder.btnPresent.setOnClickListener(v -> {
                attendanceViewModel.markAttendance(subject, "present");
                Toast.makeText(requireContext(), "Marked present for " + subject, Toast.LENGTH_SHORT).show();
                notifyItemChanged(position);
            });

            holder.btnAbsent.setOnClickListener(v -> {
                attendanceViewModel.markAttendance(subject, "absent");
                Toast.makeText(requireContext(), "Marked absent for " + subject, Toast.LENGTH_SHORT).show();
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() { return subjects.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvSubject, tvPercentage, tvWarning;
            LinearProgressIndicator progressAttendance;
            MaterialButton btnPresent, btnAbsent;
            VH(@NonNull View itemView) {
                super(itemView);
                tvSubject = itemView.findViewById(R.id.tvSubject);
                tvPercentage = itemView.findViewById(R.id.tvPercentage);
                tvWarning = itemView.findViewById(R.id.tvWarning);
                progressAttendance = itemView.findViewById(R.id.progressAttendance);
                btnPresent = itemView.findViewById(R.id.btnPresent);
                btnAbsent = itemView.findViewById(R.id.btnAbsent);
            }
        }
    }
}
