package com.vaibhav.campusserviceapp.ui.feed;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.vaibhav.campusserviceapp.databinding.FragmentFeedBinding;
import com.vaibhav.campusserviceapp.models.Post;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.ui.adapters.PostAdapter;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.FeedViewModel;

public class FeedFragment extends Fragment implements PostAdapter.OnPostActionListener {
    private FragmentFeedBinding binding;
    private FeedViewModel feedViewModel;
    private PostAdapter postAdapter;
    private SessionManager sessionManager;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null) return;
            feedViewModel.resetPagination();
            loadFeed();
            refreshHandler.postDelayed(this, 8000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        postAdapter = new PostAdapter(requireContext(), sessionManager.getUserId(), this);
        binding.recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPosts.setAdapter(postAdapter);

        loadFeed();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            feedViewModel.resetPagination();
            loadFeed();
        });

        binding.fabCreatePost.setOnClickListener(v -> {
            CreatePostDialog dialog = new CreatePostDialog();
            dialog.setOnPostCreatedListener(() -> {
                feedViewModel.resetPagination();
                loadFeed();
            });
            dialog.show(getChildFragmentManager(), "create_post");
        });

    }

    private void loadFeed() {
        binding.progressBar.setVisibility(View.VISIBLE);
        feedViewModel.getFeed(null, null).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            binding.progressBar.setVisibility(View.GONE);
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                postAdapter.setPosts(resource.data);
                binding.tvEmpty.setVisibility(resource.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUpvoteClick(Post post, int position) {
        MutableLiveData<Boolean> state = new MutableLiveData<>();
        feedViewModel.toggleUpvote(post.getId(), sessionManager.getUserId(), post.isUpvoted(), state);
        post.setUpvoted(!post.isUpvoted());
        post.setUpvoteCount(post.isUpvoted() ? post.getUpvoteCount() + 1 : post.getUpvoteCount() - 1);
        postAdapter.notifyItemChanged(position);
    }

    @Override
    public void onCommentClick(Post post) {
        CommentsBottomSheet sheet = CommentsBottomSheet.newInstance(post.getId());
        sheet.show(getChildFragmentManager(), "comments");
    }

    @Override
    public void onImageClick(String imageUrl) {
        // Open image in full screen or browser
    }

    @Override
    public void onPdfClick(String pdfUrl) {
        // Open PDF in browser
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refreshHandler.removeCallbacks(refreshRunnable);
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(refreshRunnable, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }
}
