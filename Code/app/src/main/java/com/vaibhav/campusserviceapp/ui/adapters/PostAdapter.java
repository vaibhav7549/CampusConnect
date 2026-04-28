package com.vaibhav.campusserviceapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Post;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>();
    private final Context context;
    private final OnPostActionListener listener;
    private final String currentUserId;

    public interface OnPostActionListener {
        void onUpvoteClick(Post post, int position);
        void onCommentClick(Post post);
        void onImageClick(String imageUrl);
        void onPdfClick(String pdfUrl);
    }

    public PostAdapter(Context context, String currentUserId, OnPostActionListener listener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setPosts(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    public void addPosts(List<Post> morePosts) {
        int start = posts.size();
        posts.addAll(morePosts);
        notifyItemRangeInserted(start, morePosts.size());
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.tvContent.setText(post.getContent());
        holder.tvUpvoteCount.setText(String.valueOf(post.getUpvoteCount()));

        // Author
        if (post.getAuthor() != null) {
            holder.tvAuthorName.setText(post.getAuthor().getName() != null ? post.getAuthor().getName() : "User");
            if (post.getAuthor().getPhotoUrl() != null && !post.getAuthor().getPhotoUrl().isEmpty()) {
                Glide.with(context).load(post.getAuthor().getPhotoUrl())
                        .placeholder(R.drawable.placeholder_avatar)
                        .error(R.drawable.placeholder_avatar)
                        .circleCrop().into(holder.ivAuthorAvatar);
            } else {
                holder.ivAuthorAvatar.setImageResource(R.drawable.placeholder_avatar);
            }
            if (post.getAuthor().isVerified()) {
                holder.ivVerified.setVisibility(View.VISIBLE);
            } else {
                holder.ivVerified.setVisibility(View.GONE);
            }
        } else {
            holder.tvAuthorName.setText("User");
            holder.ivAuthorAvatar.setImageResource(R.drawable.placeholder_avatar);
            holder.ivVerified.setVisibility(View.GONE);
        }

        // Subject chip
        if (post.getSubject() != null && !post.getSubject().isEmpty()) {
            holder.chipSubject.setText(post.getSubject());
            holder.chipSubject.setVisibility(View.VISIBLE);
        } else {
            holder.chipSubject.setVisibility(View.GONE);
        }

        // Image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.cardImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getImageUrl()).centerCrop().into(holder.ivPostImage);
            holder.cardImage.setOnClickListener(v -> listener.onImageClick(post.getImageUrl()));
        } else {
            holder.cardImage.setVisibility(View.GONE);
        }

        // PDF
        if (post.getPdfUrl() != null && !post.getPdfUrl().isEmpty()) {
            holder.chipPdf.setVisibility(View.VISIBLE);
            holder.chipPdf.setOnClickListener(v -> listener.onPdfClick(post.getPdfUrl()));
        } else {
            holder.chipPdf.setVisibility(View.GONE);
        }

        // Timestamp
        holder.tvTimestamp.setText(formatTime(post.getCreatedAt()));

        // Upvote and bookmark states
        if (post.isUpvoted()) {
            holder.ivUpvote.setColorFilter(context.getColor(R.color.upvote_active));
        } else {
            holder.ivUpvote.setColorFilter(context.getColor(R.color.on_surface_variant));
        }
        // Listeners for navigating to profile
        View.OnClickListener profileClickListener = v -> {
            if (post.getAuthor() != null) {
                android.content.Intent intent = new android.content.Intent(context, com.vaibhav.campusserviceapp.ui.profile.ViewProfileActivity.class);
                intent.putExtra("user_id", post.getAuthor().getId());
                context.startActivity(intent);
            }
        };
        holder.ivAuthorAvatar.setOnClickListener(profileClickListener);
        holder.tvAuthorName.setOnClickListener(profileClickListener);

        // Listeners
        holder.layoutUpvote.setOnClickListener(v -> listener.onUpvoteClick(post, position));
        holder.layoutComment.setOnClickListener(v -> listener.onCommentClick(post));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private String formatTime(String timestamp) {
        if (timestamp == null) return "";
        try {
            return timestamp.substring(0, 10); // Simple date
        } catch (Exception e) {
            return timestamp;
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAuthorAvatar;
        TextView tvAuthorName, tvTimestamp, tvContent, tvUpvoteCount, tvCommentLabel;
        ImageView ivVerified, ivPostImage, ivUpvote;
        Chip chipSubject, chipPdf;
        MaterialCardView cardImage;
        LinearLayout layoutUpvote, layoutComment;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvUpvoteCount = itemView.findViewById(R.id.tvUpvoteCount);
            tvCommentLabel = itemView.findViewById(R.id.tvCommentLabel);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivUpvote = itemView.findViewById(R.id.ivUpvote);
            chipSubject = itemView.findViewById(R.id.chipSubject);
            chipPdf = itemView.findViewById(R.id.chipPdf);
            cardImage = itemView.findViewById(R.id.cardImage);
            layoutUpvote = itemView.findViewById(R.id.layoutUpvote);
            layoutComment = itemView.findViewById(R.id.layoutComment);
        }
    }
}
