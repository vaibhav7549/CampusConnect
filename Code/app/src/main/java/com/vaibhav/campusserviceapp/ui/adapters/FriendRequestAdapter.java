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
import com.google.android.material.button.MaterialButton;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.FriendRequest;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FRViewHolder> {
    private List<FriendRequest> requests = new ArrayList<>();
    private final Context context;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(FriendRequest request, int position);
        void onReject(FriendRequest request, int position);
        void onUserClick(FriendRequest request);
    }

    public FriendRequestAdapter(Context context, OnRequestActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setRequests(List<FriendRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < requests.size()) {
            requests.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public FRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new FRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FRViewHolder holder, int position) {
        FriendRequest req = requests.get(position);

        if (req.getFromUser() != null) {
            holder.tvName.setText(req.getFromUser().getName() != null ? req.getFromUser().getName() : "User");

            if (req.getFromUser().isVerified()) {
                holder.ivVerified.setVisibility(View.VISIBLE);
            } else {
                holder.ivVerified.setVisibility(View.GONE);
            }

            if (req.getFromUser().getPhotoUrl() != null) {
                Glide.with(context).load(req.getFromUser().getPhotoUrl())
                        .placeholder(R.drawable.placeholder_avatar)
                        .circleCrop().into(holder.ivAvatar);
            }
        } else {
            holder.tvName.setText("User");
        }

        if (req.getSentAt() != null) {
            holder.tvTime.setText(req.getSentAt().substring(0, 10));
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(req, holder.getAdapterPosition()));
        holder.btnReject.setOnClickListener(v -> listener.onReject(req, holder.getAdapterPosition()));
        holder.itemView.setOnClickListener(v -> listener.onUserClick(req));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class FRViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvTime;
        ImageView ivVerified;
        MaterialButton btnAccept, btnReject;

        FRViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
