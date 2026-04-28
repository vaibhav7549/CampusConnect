package com.vaibhav.campusserviceapp.ui.adapters;

import android.content.Intent;
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
import com.vaibhav.campusserviceapp.models.LostItem;

import java.util.ArrayList;
import java.util.List;

public class LostItemAdapter extends RecyclerView.Adapter<LostItemAdapter.LostItemViewHolder> {
    private final Context context;
    private final OnLostItemClickListener listener;
    private List<LostItem> lostItems = new ArrayList<>();

    public interface OnLostItemClickListener {
        void onLostItemClick(LostItem lostItem);
    }

    public LostItemAdapter(Context context, OnLostItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setLostItems(List<LostItem> lostItems) {
        this.lostItems = lostItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LostItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lost_item, parent, false);
        return new LostItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LostItemViewHolder holder, int position) {
        LostItem lostItem = lostItems.get(position);
        holder.tvTitle.setText(lostItem.getTitle());
        holder.tvCategory.setText(lostItem.getCategory() == null ? context.getString(R.string.other) : lostItem.getCategory());
        holder.tvLocation.setText(context.getString(R.string.lost_at_label, lostItem.getLocation() == null ? "-" : lostItem.getLocation()));
        holder.tvStatus.setText(lostItem.isFound() ? context.getString(R.string.found) : context.getString(R.string.still_missing));
        holder.tvStatus.setBackgroundResource(lostItem.isFound() ? R.drawable.bg_found_status : R.drawable.bg_missing_status);

        if (lostItem.getOwner() != null && lostItem.getOwner().getName() != null) {
            holder.tvOwnerName.setText(lostItem.getOwner().getName());
        } else {
            holder.tvOwnerName.setText(R.string.unknown_user);
        }

        if (lostItem.getImageUrl() != null && !lostItem.getImageUrl().isEmpty()) {
            Glide.with(context).load(lostItem.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> listener.onLostItemClick(lostItem));
        holder.tvOwnerName.setOnClickListener(v -> {
            if (lostItem.getOwner() != null && lostItem.getOwner().getId() != null) {
                Intent intent = new Intent(context, com.vaibhav.campusserviceapp.ui.profile.ViewProfileActivity.class);
                intent.putExtra("user_id", lostItem.getOwner().getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lostItems.size();
    }

    static class LostItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvTitle;
        TextView tvCategory;
        TextView tvLocation;
        TextView tvStatus;
        TextView tvOwnerName;

        public LostItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOwnerName = itemView.findViewById(R.id.tvOwnerName);
        }
    }
}
