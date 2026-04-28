package com.vaibhav.campusserviceapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Comment;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> comments = new ArrayList<>();

    public CommentAdapter(Context context) {
        this.context = context;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        notifyItemInserted(comments.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.tvContent.setText(comment.getContent());
        holder.tvTime.setText(formatTime(comment.getCreatedAt()));

        if (comment.getAuthor() != null) {
            holder.tvName.setText(comment.getAuthor().getName());
            if (comment.getAuthor().getPhotoUrl() != null && !comment.getAuthor().getPhotoUrl().isEmpty()) {
                Glide.with(context)
                        .load(comment.getAuthor().getPhotoUrl())
                        .placeholder(R.drawable.ic_person)
                        .into(holder.ivAvatar);
            }
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private String formatTime(String timestamp) {
        if (timestamp == null) return "";
        try {
            // Simple extraction for now
            return timestamp.substring(11, 16);
        } catch (Exception e) {
            return timestamp;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvTime, tvContent;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
