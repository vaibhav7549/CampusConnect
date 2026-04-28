package com.vaibhav.campusserviceapp.ui.feed;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.vaibhav.campusserviceapp.R;
import com.vaibhav.campusserviceapp.models.Post;
import com.vaibhav.campusserviceapp.repositories.AuthRepository;
import com.vaibhav.campusserviceapp.utils.SessionManager;
import com.vaibhav.campusserviceapp.viewmodels.FeedViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.widget.ImageView;

public class CreatePostDialog extends DialogFragment {
    private OnPostCreatedListener listener;
    private Uri selectedImageUri;
    private ImageView previewImage;
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                selectedImageUri = uri;
                if (previewImage != null) {
                    Glide.with(this).load(uri).centerCrop().into(previewImage);
                }
            });

    public interface OnPostCreatedListener {
        void onPostCreated();
    }

    public void setOnPostCreatedListener(OnPostCreatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_post, null);
        TextInputEditText etContent = view.findViewById(R.id.etContent);
        TextInputEditText etSubject = view.findViewById(R.id.etSubject);
        previewImage = view.findViewById(R.id.ivPreview);
        view.findViewById(R.id.btnPickImage).setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        selectedImageUri = null;

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.create_post)
                .setView(view)
                .setPositiveButton(R.string.post, (dialog, which) -> {
                    String content = etContent.getText().toString().trim();
                    if (content.isEmpty()) {
                        Toast.makeText(requireContext(), "Content is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SessionManager session = new SessionManager(requireContext());
                    Post post = new Post();
                    post.setAuthorUid(session.getUserId());
                    post.setContent(content);
                    String subject = etSubject.getText().toString().trim();
                    if (!subject.isEmpty()) post.setSubject(subject);
                    File imageFile = selectedImageUri != null ? copyUriToTempFile(selectedImageUri) : null;

                    FeedViewModel vm = new ViewModelProvider(requireParentFragment()).get(FeedViewModel.class);
                    vm.createPost(post, imageFile, null).observe(requireParentFragment().getViewLifecycleOwner(), resource -> {
                        if (resource.status == AuthRepository.Resource.Status.SUCCESS && listener != null) {
                            listener.onPostCreated();
                        } else if (resource.status == AuthRepository.Resource.Status.ERROR) {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    private File copyUriToTempFile(Uri uri) {
        try {
            File temp = File.createTempFile("post_", ".jpg", requireContext().getCacheDir());
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(temp);
            byte[] buffer = new byte[4096];
            int len;
            while (is != null && (len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            if (is != null) is.close();
            return temp;
        } catch (Exception e) {
            return null;
        }
    }
}
