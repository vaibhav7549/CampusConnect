package com.vaibhav.campusserviceapp.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_SENT = 1;
    private static final int VIEW_RECEIVED = 2;
    private List<Message> messages = new ArrayList<>();
    private final String currentUserId;

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        return (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) ? VIEW_SENT : VIEW_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof SentViewHolder) {
            SentViewHolder sent = (SentViewHolder) holder;
            bindMessageContent(sent.tvMessage, sent.ivImage, msg);
            sent.tvTime.setText(formatTime(msg.getCreatedAt()));
            if (msg.isRead()) {
                sent.ivSeen.setVisibility(View.VISIBLE);
            } else {
                sent.ivSeen.setVisibility(View.GONE);
            }
            sent.tvReaction.setVisibility(View.GONE);
        } else {
            ReceivedViewHolder received = (ReceivedViewHolder) holder;
            bindMessageContent(received.tvMessage, received.ivImage, msg);
            received.tvTime.setText(formatTime(msg.getCreatedAt()));
            received.tvReaction.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(String timestamp) {
        if (timestamp == null) return "";
        try { return timestamp.substring(11, 16); } catch (Exception e) { return ""; }
    }

    private void bindMessageContent(TextView tvMessage, ImageView ivImage, Message msg) {
        String content = msg.getContent();
        if (content != null && !content.trim().isEmpty()) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(content);
        } else {
            tvMessage.setVisibility(View.GONE);
            tvMessage.setText("");
        }

        if (msg.getImageUrl() != null && !msg.getImageUrl().isEmpty()) {
            ivImage.setVisibility(View.VISIBLE);
            Glide.with(ivImage.getContext())
                    .load(msg.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(ivImage);
            ivImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(msg.getImageUrl()));
                v.getContext().startActivity(intent);
            });
        } else {
            ivImage.setVisibility(View.GONE);
            ivImage.setImageDrawable(null);
            ivImage.setOnClickListener(null);
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvReaction;
        ImageView ivSeen, ivImage;
        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivSeen = itemView.findViewById(R.id.ivSeen);
            tvReaction = itemView.findViewById(R.id.tvReaction);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvReaction;
        ImageView ivImage;
        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}
