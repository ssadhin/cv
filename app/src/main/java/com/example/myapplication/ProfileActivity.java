package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView ivBigProfile;
    private EditText etProfileName;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful() && result.getUriContent() != null) {
                    selectedImageUri = result.getUriContent();
                    ivBigProfile.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        ivBigProfile = findViewById(R.id.ivBigProfile);
        etProfileName = findViewById(R.id.etProfileName);
        Button btnChangePhoto = findViewById(R.id.btnChangePhoto);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnBack = findViewById(R.id.btnBack);

        // Set name from Firebase or fallback
        String name = user.getDisplayName();
        etProfileName.setText(name != null ? name : getString(R.string.guest_user));

        // Load profile picture from local file first
        loadLocalProfilePicture();

        btnChangePhoto.setOnClickListener(v -> {
            CropImageOptions options = new CropImageOptions();
            options.cropShape = CropImageView.CropShape.OVAL;
            options.aspectRatioX = 1;
            options.aspectRatioY = 1;
            options.fixAspectRatio = true;
            cropImage.launch(new CropImageContractOptions(null, options));
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile(user));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            clearLocalProfileData();
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLocalProfilePicture() {
        File profilePic = new File(getFilesDir(), "profile_pic.jpg");
        if (profilePic.exists()) {
            Glide.with(this).load(profilePic).into(ivBigProfile);
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(ivBigProfile);
            } else {
                ivBigProfile.setImageResource(R.drawable.cv);
            }
        }
    }

    private void saveProfile(FirebaseUser user) {
        String newName = etProfileName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, R.string.name_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            saveImageLocally();
        }

        updateFirebaseName(user, newName);
    }

    private void saveImageLocally() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
            File profilePic = new File(getFilesDir(), "profile_pic.jpg");
            FileOutputStream fos = new FileOutputStream(profilePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (IOException e) {
            Toast.makeText(this, R.string.failed_to_save_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFirebaseName(FirebaseUser user, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.profile_updated_locally, Toast.LENGTH_SHORT).show();
                        // Sync the updated name/profile to Cloudflare Registry
                        new UserTierManager(this).syncUserToCloud(name, user.getEmail());
                        finish();
                    } else {
                        Toast.makeText(this, R.string.name_update_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearLocalProfileData() {
        File profilePic = new File(getFilesDir(), "profile_pic.jpg");
        if (profilePic.exists()) {
            profilePic.delete();
        }
    }
}

