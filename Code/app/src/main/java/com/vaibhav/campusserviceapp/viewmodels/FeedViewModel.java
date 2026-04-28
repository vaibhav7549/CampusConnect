package com.vaibhav.campusserviceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.vaibhav.campusserviceapp.models.Bookmark;
import com.vaibhav.campusserviceapp.models.Comment;
import com.vaibhav.campusserviceapp.models.Post;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.repositories.PostRepository;
import java.io.File;
import java.util.List;

public class FeedViewModel extends AndroidViewModel {
    private final PostRepository postRepository;
    private int currentOffset = 0;

    public FeedViewModel(@NonNull Application application) {
        super(application);
        postRepository = new PostRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Post>>> getFeed(String branchFilter, String subjectFilter) {
        return postRepository.getFeed(currentOffset, branchFilter, subjectFilter);
    }

    public LiveData<AuthRepository.Resource<List<Post>>> loadMore(String branchFilter, String subjectFilter) {
        currentOffset += 15;
        return postRepository.getFeed(currentOffset, branchFilter, subjectFilter);
    }

    public void resetPagination() {
        currentOffset = 0;
    }

    public LiveData<AuthRepository.Resource<Post>> createPost(Post post, File imageFile, File pdfFile) {
        return postRepository.createPost(post, imageFile, pdfFile);
    }

    public void toggleUpvote(String postId, String userId, boolean isUpvoted, MutableLiveData<Boolean> state) {
        postRepository.toggleUpvote(postId, userId, isUpvoted, state);
    }

    public void toggleBookmark(String postId, String userId, boolean isBookmarked, MutableLiveData<Boolean> state) {
        postRepository.toggleBookmark(postId, userId, isBookmarked, state);
    }

    public LiveData<AuthRepository.Resource<List<Bookmark>>> getBookmarks(String userId) {
        return postRepository.getBookmarks(userId);
    }

    public LiveData<AuthRepository.Resource<List<Comment>>> getComments(String postId) {
        return postRepository.getComments(postId);
    }

    public LiveData<AuthRepository.Resource<Void>> addComment(Comment comment) {
        return postRepository.addComment(comment);
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkUpvoted(String postId, String userId) {
        return postRepository.checkUpvoted(postId, userId);
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkBookmarked(String postId, String userId) {
        return postRepository.checkBookmarked(postId, userId);
    }
}
