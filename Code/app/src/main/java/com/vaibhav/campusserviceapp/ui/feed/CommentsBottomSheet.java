package com.vaibhav.campusserviceapp.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Comment;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.CommentAdapter;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.FeedViewModel;

import java.util.ArrayList;

public class CommentsBottomSheet extends BottomSheetDialogFragment {
    private String postId;
    private FeedViewModel feedViewModel;
    private CommentAdapter commentAdapter;
    private SessionManager sessionManager;
    private RecyclerView recyclerComments;
    private EditText etComment;

    public static CommentsBottomSheet newInstance(String postId) {
        CommentsBottomSheet fragment = new CommentsBottomSheet();
        Bundle args = new Bundle();
        args.putString("post_id", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("post_id");
        }
        sessionManager = new SessionManager(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        feedViewModel = new ViewModelProvider(requireParentFragment()).get(FeedViewModel.class);
        
        recyclerComments = view.findViewById(R.id.recyclerComments);
        etComment = view.findViewById(R.id.etComment);
        
        commentAdapter = new CommentAdapter(requireContext());
        recyclerComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerComments.setAdapter(commentAdapter);
        
        loadComments();

        view.findViewById(R.id.fabSend).setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (!text.isEmpty()) {
                postComment(text);
            }
        });
    }

    private void loadComments() {
        feedViewModel.getComments(postId).observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                if (resource.data != null) {
                    commentAdapter.setComments(resource.data);
                    if (resource.data.size() > 0) {
                        recyclerComments.smoothScrollToPosition(resource.data.size() - 1);
                    }
                }
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment(String text) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setContent(text);
        comment.setAuthorId(sessionManager.getUserId());
        
        feedViewModel.addComment(comment).observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == AuthRepository.Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Comment added", Toast.LENGTH_SHORT).show();
                etComment.setText("");
                loadComments(); // Refresh list to get new comment with id and author
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Failed to add comment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
