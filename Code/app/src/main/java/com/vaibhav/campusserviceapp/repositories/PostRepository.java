package com.vaibhav.campusserviceapp.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaibhav.campusserviceapp.models.Bookmark;
import com.vaibhav.campusserviceapp.models.Comment;
import com.vaibhav.campusserviceapp.models.Post;
import com.vaibhav.campusserviceapp.models.PostUpvote;
import com.vaibhav.campusserviceapp.network.PostApi;
import com.vaibhav.campusserviceapp.network.SupabaseClient;
import com.vaibhav.campusserviceapp.utils.Constants;
import com.vaibhav.campusserviceapp.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRepository {
    private final PostApi postApi;
    private final SessionManager sessionManager;
    private final OkHttpClient okHttpClient;
    private List<Post> cachedPosts;
    private final Gson gson = new Gson();

    public PostRepository(Context context) {
        SupabaseClient client = SupabaseClient.getInstance(context);
        postApi = client.getPostApi();
        sessionManager = client.getSessionManager();
        okHttpClient = client.getOkHttpClient();
    }

    public LiveData<AuthRepository.Resource<List<Post>>> getFeed(int offset, String branchFilter, String subjectFilter) {
        MutableLiveData<AuthRepository.Resource<List<Post>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        String select = "*,profiles!author_id(id,name,photo_url,avatar_url,is_verified)";
        String order = "created_at.desc";

        Callback<List<Post>> callback = new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (offset == 0) cachedPosts = response.body();
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error(extractError(response)));
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                if (cachedPosts != null && offset == 0) {
                    result.setValue(AuthRepository.Resource.success(cachedPosts));
                } else {
                    result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
                }
            }
        };

        if (branchFilter != null || subjectFilter != null) {
            postApi.getPostsFiltered(select, order, Constants.PAGE_SIZE, offset,
                    branchFilter != null ? "eq." + branchFilter : null,
                    subjectFilter != null ? "eq." + subjectFilter : null
            ).enqueue(callback);
        } else {
            postApi.getPosts(select, order, Constants.PAGE_SIZE, offset).enqueue(callback);
        }

        return result;
    }

    public LiveData<AuthRepository.Resource<Post>> createPost(Post post, File imageFile, File pdfFile) {
        MutableLiveData<AuthRepository.Resource<Post>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());
        String userId = sessionManager.getUserId();
        if (userId == null || sessionManager.getAccessToken() == null) {
            result.setValue(AuthRepository.Resource.error("Session expired. Please log in again."));
            return result;
        }
        if (post.getAuthorUid() == null) {
            post.setAuthorUid(userId);
        }

        new Thread(() -> {
            try {
                String postId = UUID.randomUUID().toString();

                if (imageFile != null) {
                    try {
                        String imageUrl = uploadFile(imageFile, Constants.BUCKET_POST_IMAGES, postId + ".jpg", "image/jpeg");
                        post.setImageUrl(imageUrl);
                    } catch (IOException e) {
                        // Ignore media upload failure so text post still works.
                    }
                }

                if (pdfFile != null) {
                    try {
                        String pdfUrl = uploadFile(pdfFile, Constants.BUCKET_POST_PDFS, postId + ".pdf", "application/pdf");
                        post.setPdfUrl(pdfUrl);
                    } catch (IOException e) {
                        // Ignore media upload failure so text post still works.
                    }
                }

                postApi.createPost(post, "return=representation").enqueue(new Callback<List<Post>>() {
                    @Override
                    public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            result.postValue(AuthRepository.Resource.success(response.body().get(0)));
                        } else {
                            result.postValue(AuthRepository.Resource.error(extractError(response)));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Post>> call, Throwable t) {
                        result.postValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
                    }
                });
            } catch (Exception e) {
                result.postValue(AuthRepository.Resource.error("Failed to create post: " + e.getMessage()));
            }
        }).start();

        return result;
    }

    public void toggleUpvote(String postId, String userId, boolean isCurrentlyUpvoted, MutableLiveData<Boolean> upvoteState) {
        if (isCurrentlyUpvoted) {
            postApi.removeUpvote("eq." + postId, "eq." + userId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    upvoteState.setValue(false);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        } else {
            PostUpvote upvote = new PostUpvote(postId, userId);
            postApi.addUpvote(upvote, "return=minimal").enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    upvoteState.setValue(true);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
    }

    public void toggleBookmark(String postId, String userId, boolean isCurrentlyBookmarked, MutableLiveData<Boolean> bookmarkState) {
        if (isCurrentlyBookmarked) {
            postApi.removeBookmark("eq." + postId, "eq." + userId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    bookmarkState.setValue(false);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        } else {
            Bookmark bookmark = new Bookmark(postId, userId);
            postApi.addBookmark(bookmark, "return=minimal").enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    bookmarkState.setValue(true);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
    }

    public LiveData<AuthRepository.Resource<List<Bookmark>>> getBookmarks(String userId) {
        MutableLiveData<AuthRepository.Resource<List<Bookmark>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        postApi.getUserBookmarks("eq." + userId, "post_id,posts(*,profiles!author_id(id,name,photo_url,avatar_url,is_verified))").enqueue(new Callback<List<Bookmark>>() {
            @Override
            public void onResponse(Call<List<Bookmark>> call, Response<List<Bookmark>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load bookmarks"));
                }
            }

            @Override
            public void onFailure(Call<List<Bookmark>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<List<Comment>>> getComments(String postId) {
        MutableLiveData<AuthRepository.Resource<List<Comment>>> result = new MutableLiveData<>();

        postApi.getComments("eq." + postId, "*,profiles(id,name,photo_url,avatar_url)", "created_at.asc").enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(AuthRepository.Resource.success(response.body()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load comments"));
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> addComment(Comment comment) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        postApi.addComment(comment, "return=minimal").enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to add comment"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkUpvoted(String postId, String userId) {
        MutableLiveData<AuthRepository.Resource<Boolean>> result = new MutableLiveData<>();

        postApi.checkUpvoted("eq." + postId, "eq." + userId).enqueue(new Callback<List<PostUpvote>>() {
            @Override
            public void onResponse(Call<List<PostUpvote>> call, Response<List<PostUpvote>> response) {
                boolean isUpvoted = response.isSuccessful() && response.body() != null && !response.body().isEmpty();
                result.setValue(AuthRepository.Resource.success(isUpvoted));
            }

            @Override
            public void onFailure(Call<List<PostUpvote>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.success(false));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Boolean>> checkBookmarked(String postId, String userId) {
        MutableLiveData<AuthRepository.Resource<Boolean>> result = new MutableLiveData<>();

        postApi.checkBookmarked("eq." + postId, "eq." + userId).enqueue(new Callback<List<Bookmark>>() {
            @Override
            public void onResponse(Call<List<Bookmark>> call, Response<List<Bookmark>> response) {
                boolean isBookmarked = response.isSuccessful() && response.body() != null && !response.body().isEmpty();
                result.setValue(AuthRepository.Resource.success(isBookmarked));
            }

            @Override
            public void onFailure(Call<List<Bookmark>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.success(false));
            }
        });

        return result;
    }

    private String uploadFile(File file, String bucket, String path, String contentType) throws IOException {
        String url = Constants.STORAGE_URL + "object/" + bucket + "/" + path;
        RequestBody body = RequestBody.create(file, MediaType.parse(contentType));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + sessionManager.getAccessToken())
                .addHeader("Content-Type", contentType)
                .addHeader("x-upsert", "true")
                .build();
        okhttp3.Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Upload failed: " + response.code());
        return Constants.STORAGE_URL + "object/public/" + bucket + "/" + path;
    }

    private String extractError(Response<?> response) {
        try {
            if (response.errorBody() == null) return "Failed to create post";
            String raw = response.errorBody().string();
            JsonObject obj = gson.fromJson(raw, JsonObject.class);
            if (obj != null && obj.has("message")) return obj.get("message").getAsString();
            if (obj != null && obj.has("msg")) return obj.get("msg").getAsString();
            return raw;
        } catch (Exception e) {
            return "Failed to create post";
        }
    }
}
