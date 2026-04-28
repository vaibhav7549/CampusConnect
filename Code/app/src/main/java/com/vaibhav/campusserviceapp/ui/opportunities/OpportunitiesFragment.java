package com.vaibhav.campusserviceapp.ui.opportunities;

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
import com.vaibhav.campusserviceapp.databinding.FragmentOpportunitiesBinding;
import com.vaibhav.campusserviceapp.models.Opportunity;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.OpportunityAdapter;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.OpportunityViewModel;

public class OpportunitiesFragment extends Fragment {
    private FragmentOpportunitiesBinding binding;
    private OpportunityViewModel opportunityViewModel;
    private OpportunityAdapter opportunityAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOpportunitiesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        opportunityViewModel = new ViewModelProvider(this).get(OpportunityViewModel.class);

        opportunityAdapter = new OpportunityAdapter(requireContext());
        binding.recyclerOpportunities.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerOpportunities.setAdapter(opportunityAdapter);

        loadOpportunities();

        binding.fabCreate.setOnClickListener(v -> showCreateDialog());

        // Filters
        binding.chipAll.setOnClickListener(v -> loadOpportunities());
        binding.chipInternship.setOnClickListener(v -> filterByType("internship"));
        binding.chipProject.setOnClickListener(v -> filterByType("project"));
        binding.chipHackathon.setOnClickListener(v -> filterByType("hackathon"));
    }

    private void loadOpportunities() {
        binding.progressBar.setVisibility(View.VISIBLE);
        opportunityViewModel.getOpportunities().observe(getViewLifecycleOwner(), resource -> {
            binding.progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                opportunityAdapter.setOpportunities(resource.data);
                binding.tvEmpty.setVisibility(resource.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void filterByType(String type) {
        opportunityViewModel.filterOpportunities(type, null).observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                opportunityAdapter.setOpportunities(resource.data);
            }
        });
    }

    private void showCreateDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_opportunity, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etType = dialogView.findViewById(R.id.etType);
        TextInputEditText etLink = dialogView.findViewById(R.id.etLink);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.post_opportunity)
                .setView(dialogView)
                .setPositiveButton(R.string.post, (d, w) -> {
                    Opportunity opp = new Opportunity();
                    opp.setPosterUid(new SessionManager(requireContext()).getUserId());
                    opp.setTitle(etTitle.getText().toString().trim());
                    opp.setDescription(etDescription.getText().toString().trim());
                    opp.setType(etType.getText().toString().trim());
                    opp.setApplyLink(etLink.getText().toString().trim());

                    opportunityViewModel.createOpportunity(opp);
                    loadOpportunities();
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
