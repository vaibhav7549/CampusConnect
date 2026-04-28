package com.vaibhav.campusserviceapp.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Opportunity;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class OpportunityAdapter extends RecyclerView.Adapter<OpportunityAdapter.OppViewHolder> {
    private List<Opportunity> opportunities = new ArrayList<>();
    private final Context context;

    public OpportunityAdapter(Context context) {
        this.context = context;
    }

    public void setOpportunities(List<Opportunity> list) {
        this.opportunities = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_opportunity, parent, false);
        return new OppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OppViewHolder holder, int position) {
        Opportunity opp = opportunities.get(position);
        holder.tvTitle.setText(opp.getTitle());
        holder.tvDescription.setText(opp.getDescription());
        holder.chipType.setText(opp.getType());

        // Set type chip color
        switch (opp.getType() != null ? opp.getType().toLowerCase() : "") {
            case "internship":
                holder.chipType.setChipBackgroundColorResource(R.color.type_internship);
                break;
            case "project":
                holder.chipType.setChipBackgroundColorResource(R.color.type_project);
                break;
            case "hackathon":
                holder.chipType.setChipBackgroundColorResource(R.color.type_hackathon);
                break;
        }
        holder.chipType.setTextColor(context.getColor(R.color.on_primary));

        if (opp.getDeadline() != null) {
            holder.tvDeadline.setText("Deadline: " + opp.getDeadline().substring(0, 10));
        }

        if (opp.getPoster() != null) {
            holder.tvPosterName.setText(opp.getPoster().getName());
            if (opp.getPoster().getPhotoUrl() != null) {
                Glide.with(context).load(opp.getPoster().getPhotoUrl())
                        .placeholder(R.drawable.placeholder_avatar)
                        .circleCrop().into(holder.ivPosterAvatar);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (opp.getApplyLink() != null) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(opp.getApplyLink())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    static class OppViewHolder extends RecyclerView.ViewHolder {
        Chip chipType;
        TextView tvTitle, tvDescription, tvDeadline, tvPosterName;
        CircleImageView ivPosterAvatar;
        OppViewHolder(@NonNull View itemView) {
            super(itemView);
            chipType = itemView.findViewById(R.id.chipType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvPosterName = itemView.findViewById(R.id.tvPosterName);
            ivPosterAvatar = itemView.findViewById(R.id.ivPosterAvatar);
        }
    }
}
