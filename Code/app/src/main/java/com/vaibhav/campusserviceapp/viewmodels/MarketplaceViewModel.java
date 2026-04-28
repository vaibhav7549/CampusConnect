package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.vaibhav.campusserviceapp.models.Listing;
import com.vaibhav.campusserviceapp.models.LostItem;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.ListingRepository;
import com.vaibhav.campusserviceapp.repositories.LostItemRepository;
import java.io.File;
import java.util.List;

public class MarketplaceViewModel extends AndroidViewModel {
    private final ListingRepository listingRepository;
    private final LostItemRepository lostItemRepository;

    public MarketplaceViewModel(@NonNull Application application) {
        super(application);
        listingRepository = new ListingRepository(application);
        lostItemRepository = new LostItemRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Listing>>> getListings() { return listingRepository.getListings(); }
    public LiveData<AuthRepository.Resource<List<Listing>>> searchListings(String query) { return listingRepository.searchListings(query); }
    public LiveData<AuthRepository.Resource<List<Listing>>> filterByCategory(String category) { return listingRepository.filterByCategory(category); }
    public LiveData<AuthRepository.Resource<Listing>> createListing(Listing listing, File photoFile) { return listingRepository.createListing(listing, photoFile); }
    public LiveData<AuthRepository.Resource<Void>> markAsSold(String listingId) { return listingRepository.markAsSold(listingId); }
    public LiveData<AuthRepository.Resource<List<LostItem>>> getLostItems() { return lostItemRepository.getLostItems(); }
    public LiveData<AuthRepository.Resource<List<LostItem>>> searchLostItems(String query) { return lostItemRepository.searchLostItems(query); }
    public LiveData<AuthRepository.Resource<List<LostItem>>> filterLostItemsByFoundStatus(boolean isFound) { return lostItemRepository.filterByFoundStatus(isFound); }
    public LiveData<AuthRepository.Resource<LostItem>> createLostItem(LostItem lostItem, File photoFile) { return lostItemRepository.createLostItem(lostItem, photoFile); }
    public LiveData<AuthRepository.Resource<Void>> markLostItemFound(String lostItemId) { return lostItemRepository.markAsFound(lostItemId); }
}
