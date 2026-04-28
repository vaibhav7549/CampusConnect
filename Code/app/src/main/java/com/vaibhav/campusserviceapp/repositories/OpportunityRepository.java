package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vaibhav.campusserviceapp.models.Opportunity;
import com.vaibhav.campusserviceapp.network.OpportunityApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpportunityRepository {
    private final OpportunityApi opportunityApi;
    private final SessionManager sessionManager;

    public OpportunityRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        opportunityApi = client.getOpportunityApi();
        sessionManager = client.getSessionManager();
    }

    public LiveData<AuthRepository.Resource<List<Opportunity>>> getOpportunities() {
        MutableLiveData<AuthRepository.Resource<List<Opportunity>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        opportunityApi.getOpportunities("*,profiles!poster_uid(name,photo_url,is_verified)", "deadline.asc").enqueue(new Callback<List<Opportunity>>() {
            @Override
            public void onResponse(Call<List<Opportunity>> call, Response<List<Opportunity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load opportunities"));
                }
            }

            @Override
            public void onFailure(Call<List<Opportunity>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<Opportunity>>> filterOpportunities(String type, String branch) {
        MutableLiveData<AuthRepository.Resource<List<Opportunity>>> result = new MutableLiveData<>();

        String typeFilter = type != null ? "eq." + type : null;
        String branchFilter = branch != null ? "(branch.eq." + branch + ",branch.eq.All)" : null;

        opportunityApi.filterOpportunities(typeFilter, branchFilter, "*,profiles!poster_uid(name,photo_url,is_verified)", "deadline.asc").enqueue(new Callback<List<Opportunity>>() {
            @Override
            public void onResponse(Call<List<Opportunity>> call, Response<List<Opportunity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Filter failed"));
                }
            }

            @Override
            public void onFailure(Call<List<Opportunity>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> createOpportunity(Opportunity opportunity) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        opportunity.setPosterUid(sessionManager.getUserId());

        opportunityApi.createOpportunity(opportunity, "return=minimal").enqueue(new Callback<Void>() {
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
