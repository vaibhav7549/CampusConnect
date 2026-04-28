package com.vaibhav.campusserviceapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {
    private List<Notification> notifications = new ArrayList<>();

    public void setNotifications(List<Notification> list) {
        this.notifications = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        Notification n = notifications.get(position);
        holder.tvMessage.setText(n.getMessage());
        holder.tvTime.setText(n.getCreatedAt() != null ? n.getCreatedAt().substring(0, 16).replace("T", " ") : "");

        // Unread indicator
        if (!n.isRead()) {
            holder.viewDot.setBackgroundResource(R.color.primary);
            holder.viewDot.setAlpha(1.0f);
        } else {
            holder.viewDot.setAlpha(0.2f);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        View viewDot;
        TextView tvMessage, tvTime;
        NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot = itemView.findViewById(R.id.viewDot);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
