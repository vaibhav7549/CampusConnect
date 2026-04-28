package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.Opportunity;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.OpportunityRepository;
import java.util.List;

public class OpportunityViewModel extends AndroidViewModel {
    private final OpportunityRepository opportunityRepository;

    public OpportunityViewModel(@NonNull Application application) {
        super(application);
        opportunityRepository = new OpportunityRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Opportunity>>> getOpportunities() { return opportunityRepository.getOpportunities(); }
    public LiveData<AuthRepository.Resource<List<Opportunity>>> filterOpportunities(String type, String branch) { return opportunityRepository.filterOpportunities(type, branch); }
    public LiveData<AuthRepository.Resource<Void>> createOpportunity(Opportunity opp) { return opportunityRepository.createOpportunity(opp); }
}
