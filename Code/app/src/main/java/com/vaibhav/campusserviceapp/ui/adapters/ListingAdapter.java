package com.vaibhav.campusserviceapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Listing;
import java.util.ArrayList;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {
    private List<Listing> listings = new ArrayList<>();
    private final Context context;
    private final OnListingClickListener listener;

    public interface OnListingClickListener {
        void onListingClick(Listing listing);
    }

    public ListingAdapter(Context context, OnListingClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setListings(List<Listing> newListings) {
        this.listings = newListings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing listing = listings.get(position);
        holder.tvTitle.setText(listing.getTitle());
        holder.tvPrice.setText("₹" + listing.getPrice());
        holder.tvCategory.setText(listing.getCategory());

        if (listing.getPhotoUrl() != null) {
            Glide.with(context).load(listing.getPhotoUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .centerCrop().into(holder.ivPhoto);
        }

        if (listing.getSeller() != null) {
            holder.tvSellerName.setText(listing.getSeller().getName() != null ? listing.getSeller().getName() : "User");
            if (listing.getSeller().getPhotoUrl() != null && !listing.getSeller().getPhotoUrl().isEmpty()) {
                Glide.with(context).load(listing.getSeller().getPhotoUrl())
                        .placeholder(R.drawable.placeholder_avatar)
                        .circleCrop().into(holder.ivSellerAvatar);
            } else {
                holder.ivSellerAvatar.setImageResource(R.drawable.placeholder_avatar);
            }
            
            holder.layoutSellerInfo.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context, com.vaibhav.campusserviceapp.ui.profile.ViewProfileActivity.class);
                intent.putExtra("user_id", listing.getSeller().getId());
                context.startActivity(intent);
            });
        }

        holder.itemView.setOnClickListener(v -> listener.onListingClick(listing));
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvTitle, tvPrice, tvCategory, tvSellerName;
        de.hdodenhof.circleimageview.CircleImageView ivSellerAvatar;
        android.widget.LinearLayout layoutSellerInfo;
        
        ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvSellerName = itemView.findViewById(R.id.tvSellerName);
            ivSellerAvatar = itemView.findViewById(R.id.ivSellerAvatar);
            layoutSellerInfo = itemView.findViewById(R.id.layoutSellerInfo);
        }
    }
}
