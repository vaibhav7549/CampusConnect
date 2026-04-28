package com.vaibhav.campusserviceapp.ui.marketplace;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.databinding.FragmentMarketplaceBinding;
import com.vaibhav.campusserviceapp.models.Listing;
import com.vaibhav.campusserviceapp.models.LostItem;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.ListingAdapter;
import com.vaibhav.campusserviceapp.ui.adapters.LostItemAdapter;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.MarketplaceViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MarketplaceFragment extends Fragment implements ListingAdapter.OnListingClickListener, LostItemAdapter.OnLostItemClickListener {
    private enum Mode { MARKETPLACE, LOST_ITEMS }

    private FragmentMarketplaceBinding binding;
    private MarketplaceViewModel marketplaceViewModel;
    private ListingAdapter listingAdapter;
    private LostItemAdapter lostItemAdapter;
    private Mode currentMode = Mode.MARKETPLACE;
    private Uri pendingImageUri;
    private ImageView pendingPreview;
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                pendingImageUri = uri;
                if (pendingPreview != null) {
                    Glide.with(this).load(uri).centerCrop().into(pendingPreview);
                }
            });
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null) return;
            if (currentMode == Mode.MARKETPLACE) {
                loadListings();
            } else {
                loadLostItems();
            }
            refreshHandler.postDelayed(this, 8000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMarketplaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        marketplaceViewModel = new ViewModelProvider(this).get(MarketplaceViewModel.class);

        listingAdapter = new ListingAdapter(requireContext(), this);
        lostItemAdapter = new LostItemAdapter(requireContext(), this);
        binding.recyclerListings.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerListings.setAdapter(listingAdapter);

        loadListings();

        binding.fabCreate.setOnClickListener(v -> {
            if (currentMode == Mode.MARKETPLACE) {
                showCreateListingDialog();
            } else {
                showCreateLostItemDialog();
            }
        });

        binding.chipMarketplace.setOnClickListener(v -> switchMode(Mode.MARKETPLACE));
        binding.chipLostItems.setOnClickListener(v -> switchMode(Mode.LOST_ITEMS));

        // Search
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = binding.etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                if (currentMode == Mode.MARKETPLACE) {
                    marketplaceViewModel.searchListings(query).observe(getViewLifecycleOwner(), resource -> {
                        if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                            listingAdapter.setListings(resource.data);
                        }
                    });
                } else {
                    marketplaceViewModel.searchLostItems(query).observe(getViewLifecycleOwner(), resource -> {
                        if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                            lostItemAdapter.setLostItems(resource.data);
                        }
                    });
                }
            } else {
                if (currentMode == Mode.MARKETPLACE) {
                    loadListings();
                } else {
                    loadLostItems();
                }
            }
            return true;
        });
    }

    private void switchMode(Mode mode) {
        currentMode = mode;
        if (mode == Mode.MARKETPLACE) {
            binding.recyclerListings.setAdapter(listingAdapter);
            binding.fabCreate.setContentDescription(getString(R.string.create_listing));
            binding.tvEmpty.setText(R.string.no_listings);
            loadListings();
        } else {
            binding.recyclerListings.setAdapter(lostItemAdapter);
            binding.fabCreate.setContentDescription(getString(R.string.report_lost_item));
            binding.tvEmpty.setText(R.string.no_lost_items);
            loadLostItems();
        }
    }

    private void loadListings() {
        binding.progressBar.setVisibility(View.VISIBLE);
        marketplaceViewModel.getListings().observe(getViewLifecycleOwner(), resource -> {
            binding.progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                listingAdapter.setListings(resource.data);
                binding.tvEmpty.setVisibility(resource.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void loadLostItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        marketplaceViewModel.getLostItems().observe(getViewLifecycleOwner(), resource -> {
            binding.progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                lostItemAdapter.setLostItems(resource.data);
                binding.tvEmpty.setVisibility(resource.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateListingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_listing, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etPrice);
        ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);
        View btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        pendingImageUri = null;
        pendingPreview = ivPreview;
        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.create_listing)
                .setView(dialogView)
                .setPositiveButton(R.string.post, (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String desc = etDescription.getText().toString().trim();
                    String price = etPrice.getText().toString().trim();

                    if (title.isEmpty() || price.isEmpty()) {
                        Toast.makeText(requireContext(), "Title and price are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Listing listing = new Listing();
                    listing.setSellerUid(new SessionManager(requireContext()).getUserId());
                    listing.setTitle(title);
                    listing.setDescription(desc);
                    listing.setPrice(Double.parseDouble(price));
                    listing.setCategory("General");
                    File photoFile = pendingImageUri != null ? copyUriToTempFile(pendingImageUri, "listing_") : null;
                    marketplaceViewModel.createListing(listing, photoFile).observe(getViewLifecycleOwner(), resource -> {
                        if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), "Listing created", Toast.LENGTH_SHORT).show();
                            loadListings();
                        } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showCreateLostItemDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_lost_item, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etLocation);
        TextInputEditText etContact = dialogView.findViewById(R.id.etContact);
        TextInputEditText etCategory = dialogView.findViewById(R.id.etCategory);
        ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);
        View btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        pendingImageUri = null;
        pendingPreview = ivPreview;
        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.report_lost_item)
                .setView(dialogView)
                .setPositiveButton(R.string.post, (d, w) -> {
                    String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                    String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
                    String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
                    String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
                    String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";

                    if (title.isEmpty() || location.isEmpty() || contact.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.lost_item_required_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LostItem lostItem = new LostItem();
                    lostItem.setOwnerId(new SessionManager(requireContext()).getUserId());
                    lostItem.setTitle(title);
                    lostItem.setDescription(desc);
                    lostItem.setLocation(location);
                    lostItem.setContact(contact);
                    lostItem.setCategory(category.isEmpty() ? getString(R.string.other) : category);
                    lostItem.setFound(false);
                    File photoFile = pendingImageUri != null ? copyUriToTempFile(pendingImageUri, "lost_") : null;
                    marketplaceViewModel.createLostItem(lostItem, photoFile).observe(getViewLifecycleOwner(), resource -> {
                        if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), R.string.lost_item_posted, Toast.LENGTH_SHORT).show();
                            loadLostItems();
                        } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onListingClick(Listing listing) {
        // Could open detail view
        new AlertDialog.Builder(requireContext())
                .setTitle(listing.getTitle())
                .setMessage("Price: ₹" + listing.getPrice() + "\n\n" + listing.getDescription())
                .setPositiveButton(R.string.contact_seller, (d, w) -> {
                    // Open chat with seller
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onLostItemClick(LostItem lostItem) {
        String message = getString(
                R.string.lost_item_detail,
                lostItem.getLocation() == null ? "-" : lostItem.getLocation(),
                lostItem.getContact() == null ? "-" : lostItem.getContact(),
                lostItem.getDescription() == null ? "" : lostItem.getDescription()
        );
        new AlertDialog.Builder(requireContext())
                .setTitle(lostItem.getTitle())
                .setMessage(message)
                .setPositiveButton(R.string.mark_found, (d, w) -> {
                    if (lostItem.getId() == null) return;
                    marketplaceViewModel.markLostItemFound(lostItem.getId()).observe(getViewLifecycleOwner(), resource -> {
                        if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), R.string.marked_as_found, Toast.LENGTH_SHORT).show();
                            loadLostItems();
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
        refreshHandler.removeCallbacks(autoRefreshRunnable);
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.removeCallbacks(autoRefreshRunnable);
        refreshHandler.postDelayed(autoRefreshRunnable, 4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    private File copyUriToTempFile(Uri uri, String prefix) {
        try {
            File temp = File.createTempFile(prefix, ".jpg", requireContext().getCacheDir());
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(temp);
            byte[] buffer = new byte[4096];
            int len;
            while (is != null && (len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            if (is != null) is.close();
            return temp;
        } catch (Exception e) {
            return null;
        }
    }
}
