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
import com.vaibhav.campusserviceapp.models.Chat;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chats = new ArrayList<>();
    private final Context context;
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(Context context, OnChatClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setChats(List<Chat> newChats) {
        this.chats = newChats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);

        String name = (chat.getOtherUser() != null && chat.getOtherUser().getName() != null) ? chat.getOtherUser().getName() : "User";
        holder.tvName.setText(name);
        holder.tvLastMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "");
        holder.tvTime.setText(chat.getLastMessageAt() != null ? chat.getLastMessageAt().substring(11, 16) : "");

        if (chat.getOtherUser() != null && chat.getOtherUser().getPhotoUrl() != null && !chat.getOtherUser().getPhotoUrl().isEmpty()) {
            Glide.with(context).load(chat.getOtherUser().getPhotoUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .error(R.drawable.placeholder_avatar)
                    .circleCrop().into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.placeholder_avatar);
        }

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvLastMessage, tvTime;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
