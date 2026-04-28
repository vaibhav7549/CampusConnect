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
import com.vaibhav.campusserviceapp.models.User;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users = new ArrayList<>();
    private final Context context;
    private final OnUserActionListener listener;
    private String actionLabel;

    public interface OnUserActionListener {
        void onUserClick(User user);
        void onActionClick(User user, int position);
    }

    public UserAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.actionLabel = context.getString(R.string.send_request);
    }

    public void setUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getName() != null ? user.getName() : "User");

        String info = "";
        if (user.getCollege() != null) info += user.getCollege();
        if (user.getBranch() != null) info += (info.isEmpty() ? "" : " • ") + user.getBranch();
        holder.tvCollege.setText(info.isEmpty() ? "CampusConnect Member" : info);

        if (user.isVerified()) {
            holder.ivVerified.setVisibility(View.VISIBLE);
        } else {
            holder.ivVerified.setVisibility(View.GONE);
        }

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(context).load(user.getPhotoUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .error(R.drawable.placeholder_avatar)
                    .circleCrop().into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.placeholder_avatar);
        }

        holder.btnAction.setText(actionLabel);
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
        holder.btnAction.setOnClickListener(v -> listener.onActionClick(user, position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvCollege;
        ImageView ivVerified;
        MaterialButton btnAction;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvCollege = itemView.findViewById(R.id.tvCollege);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
