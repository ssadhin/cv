package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.myapplication.UserTierManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import android.net.Uri;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import android.content.Intent;

public class SettingsActivity extends AppCompatActivity {

    private UserTierManager tierManager;
    private TextView tvSettingsName, tvSettingsTier, tvSettingsSubDesc;
    private ImageView ivSettingsProfile;
    private android.widget.LinearLayout btnAuthAction, btnDeleteAccount;
    private TextView tvAuthText;
    private ImageView ivAuthIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tierManager = new UserTierManager(this);
        
        tvSettingsName = findViewById(R.id.tvSettingsName);
        tvSettingsTier = findViewById(R.id.tvSettingsTier);
        tvSettingsSubDesc = findViewById(R.id.tvSettingsSubDesc);
        ivSettingsProfile = findViewById(R.id.ivSettingsProfile);
        btnAuthAction = findViewById(R.id.btnAuthAction);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        tvAuthText = findViewById(R.id.tvAuthText);
        ivAuthIcon = findViewById(R.id.ivAuthIcon);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnMenu).setOnClickListener(v -> Toast.makeText(this, R.string.menu_options_msg, Toast.LENGTH_SHORT).show());

        // Open ProfileActivity when clicking profile info
        View.OnClickListener openProfile = v -> {
            startActivity(new android.content.Intent(this, ProfileActivity.class));
        };
        ivSettingsProfile.setOnClickListener(openProfile);
        tvSettingsName.setOnClickListener(openProfile);
        
        findViewById(R.id.itemSubscription).setOnClickListener(v -> showSubscriptionTiers());
        findViewById(R.id.itemLanguage).setOnClickListener(v -> showLanguageDialog());
        
        findViewById(R.id.itemAbout).setOnClickListener(v -> showAboutDialog());
        
        // Theme selection logic
        TextView tvThemeValue = findViewById(R.id.tvThemeValue);
        int currentMode = ThemeManager.getThemeMode(this);
        if (currentMode == ThemeManager.MODE_DARK) tvThemeValue.setText(R.string.theme_dark);
        else if (currentMode == ThemeManager.MODE_LIGHT) tvThemeValue.setText(R.string.theme_light);
        else tvThemeValue.setText(R.string.theme_system);

        findViewById(R.id.itemTheme).setOnClickListener(v -> showThemeDialog());

        loadUserProfile();
        
        TextView tvLangValue = findViewById(R.id.tvLangValue);
        String langCode = LocaleHelper.getLanguageCode(this);
        if (langCode.equals("bn")) {
            tvLangValue.setText(R.string.lang_bangla);
        } else if (langCode.equals("en")) {
            tvLangValue.setText(R.string.lang_english);
        } else {
            tvLangValue.setText(R.string.lang_system);
        }

        // Auto-trigger deletion dialog if returning from re-auth
        if (getIntent().getBooleanExtra("reauth_for_deletion", false)) {
            showDeleteAccountDialog();
        }
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isAnonymous()) {
            tvSettingsName.setText(user.getDisplayName() != null ? user.getDisplayName() : getString(R.string.career_compass_user));
            
            // Load local profile pic
            File file = new File(getFilesDir(), "profile_pic.jpg");
            if (file.exists()) {
                Glide.with(this).load(file).into(ivSettingsProfile);
            }
            
            UserTierManager.Tier tier = tierManager.getUserTier();
            tvSettingsTier.setText(getString(R.string.tier_member, tier.level));
            tvSettingsSubDesc.setText(getString(R.string.plan_active_msg, tier.level));
            tvSettingsTier.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(this, R.color.tier_bg)));
            tvSettingsTier.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.tier_text));

            // Setup Logout Button
            tvAuthText.setText(getString(R.string.log_out));
            tvAuthText.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.accent_red));
            btnAuthAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(this, R.color.accent_red_bg)));
            ivAuthIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            ivAuthIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.accent_red));
            
            btnAuthAction.setOnClickListener(v -> handleLogout());
            
            btnDeleteAccount.setVisibility(View.VISIBLE);
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        } else {
            // Guest State
            btnDeleteAccount.setVisibility(View.GONE);
            tvSettingsName.setText(R.string.guest_user);
            tvSettingsTier.setText(R.string.not_logged_in);
            tvSettingsSubDesc.setText(R.string.not_active);
            ivSettingsProfile.setImageResource(R.drawable.cv);
            tvSettingsTier.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F3F4F6")));
            tvSettingsTier.setTextColor(android.graphics.Color.parseColor("#6B7280"));

            // Setup Login Button
            tvAuthText.setText(R.string.log_in);
            tvAuthText.setTextColor(android.graphics.Color.parseColor("#3B82F6")); // Blue
            btnAuthAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EFF6FF")));
            ivAuthIcon.setImageResource(android.R.drawable.ic_menu_edit); // Placeholder icon for Login
            ivAuthIcon.setColorFilter(android.graphics.Color.parseColor("#3B82F6"));
            
            btnAuthAction.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, LoginActivity.class));
            });
        }
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.log_out)
            .setMessage(R.string.logout_confirm_msg)
            .setPositiveButton(R.string.log_out, (dialog, which) -> {
                FirebaseAuth.getInstance().signOut();
                
                // Clear locally saved profile picture
                File localFile = new File(getFilesDir(), "profile_pic.jpg");
                if (localFile.exists()) {
                    localFile.delete();
                }
                
                // Restart app or return to Home
                android.content.Intent intent = new android.content.Intent(this, HomeActivity.class);
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showSubscriptionTiers() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.login_to_manage_sub, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new android.content.Intent(this, SubscriptionActivity.class));
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.lang_english), getString(R.string.lang_bangla), getString(R.string.lang_system)};
        String[] codes = {"en", "bn", "auto"};
        
        String currentCode = LocaleHelper.getLanguageCode(this);
        int checkedItem = 2; // Default to System
        if (currentCode.equals("en")) checkedItem = 0;
        else if (currentCode.equals("bn")) checkedItem = 1;
        
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    LocaleHelper.setLocale(this, codes[which]);
                    dialog.dismiss();
                    // Restart app
                    android.content.Intent intent = new android.content.Intent(this, HomeActivity.class);
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showThemeDialog() {
        String[] themes = {getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system)};
        int[] modes = {ThemeManager.MODE_LIGHT, ThemeManager.MODE_DARK, ThemeManager.MODE_SYSTEM};
        
        int currentMode = ThemeManager.getThemeMode(this);
        int checkedItem = 2; // Default to System
        if (currentMode == ThemeManager.MODE_LIGHT) checkedItem = 0;
        else if (currentMode == ThemeManager.MODE_DARK) checkedItem = 1;

        new AlertDialog.Builder(this)
                .setTitle(R.string.theme)
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    ThemeManager.setThemeMode(this, modes[which]);
                    dialog.dismiss();
                    // Restart app
                    android.content.Intent intent = new android.content.Intent(this, HomeActivity.class);
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_career_compass)
                .setMessage(R.string.about_career_compass_msg)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showDeleteAccountDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_deletion, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.CheckBox cbTemplates = dialogView.findViewById(R.id.cbDeleteTemplates);
        android.widget.CheckBox cbShared = dialogView.findViewById(R.id.cbDeleteShared);
        android.widget.CheckBox cbAccount = dialogView.findViewById(R.id.cbDeleteAccount);
        android.widget.CheckBox cbConfirm = dialogView.findViewById(R.id.cbUnderstandPermanent);
        com.google.android.material.button.MaterialButton btnDelete = dialogView.findViewById(R.id.btnDeleteConfirm);
        android.view.View btnCancel = dialogView.findViewById(R.id.btnCancel);

        cbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnDelete.setEnabled(isChecked);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            boolean[] checkedItems = new boolean[]{
                cbTemplates.isChecked(),
                cbShared.isChecked(),
                cbAccount.isChecked()
            };
            performAccountDeletion(checkedItems);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performAccountDeletion(boolean[] selectedData) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // 1. Backend Deletion (Best effort)
        // types: templates, shared, account
        List<String> typesToDelete = new ArrayList<>();
        if (selectedData[0]) typesToDelete.add("templates");
        if (selectedData[1]) typesToDelete.add("shared");
        if (selectedData[2]) typesToDelete.add("account");

        // Use UserTierManager to sync deletion request to cloud
        tierManager.requestDataDeletion(typesToDelete, new UserTierManager.DeletionCallback() {
            @Override
            public void onProcessed(boolean success, String message) {
                runOnUiThread(() -> {
                    if (success) {
                        finalizeFirebaseAccountDeletion(user);
                    } else {
                        // Fallback to Email if automatic fails or user wants
                        showDeletionErrorFallback(user, message);
                    }
                });
            }
        });
    }

    private void finalizeFirebaseAccountDeletion(FirebaseUser user) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String email = user.getEmail();
                if (email != null) {
                    getSharedPreferences("VitaeMonetizationPrefs", MODE_PRIVATE)
                        .edit().putString("last_deleted_email", email).apply();
                }
                
                // Absolute Integrity: Clear usage data & tier info locally
                tierManager.clearLocalData();
                
                Toast.makeText(this, R.string.deletion_requested, Toast.LENGTH_LONG).show();
                handleLogoutLogic(); // Reuse logout flow to clean up
            } else {
                if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                    showReauthRequiredDialog();
                } else {
                    Toast.makeText(this, "Deletion failed: " + task.getException().getMessage() + ". Please try again later or contact support.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showReauthRequiredDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Security Confirmation Required")
            .setMessage("For your security, you must have logged in recently to delete your account. Please log in again to confirm your identity.")
            .setPositiveButton("Verify Identity", (dialog, which) -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("reauth_for_deletion", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showDeletionErrorFallback(FirebaseUser user, String errorMessage) {
        new AlertDialog.Builder(this)
            .setTitle("Deletion Problem")
            .setMessage(getString(R.string.deletion_failed_email_msg) + "\n\nError: " + errorMessage)
            .setPositiveButton("Send Email", (dialog, which) -> {
                sendDeletionEmail(user);
                finalizeFirebaseAccountDeletion(user); // Still try to delete auth at least
            })
            .setNegativeButton(R.string.cancel, (dialog, which) -> finalizeFirebaseAccountDeletion(user))
            .show();
    }

    private void sendDeletionEmail(FirebaseUser user) {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"asanistudiobangladesh@gmail.com"});
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Account Deletion Request - " + user.getUid());
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Hello,\n\nI would like to request deletion of my data and account.\n\nUser ID: " + user.getUid() + "\nEmail: " + user.getEmail() + "\n\nPlease delete all my data from your records.");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void handleLogoutLogic() {
        FirebaseAuth.getInstance().signOut();
        File localFile = new File(getFilesDir(), "profile_pic.jpg");
        if (localFile.exists()) localFile.delete();
        android.content.Intent intent = new android.content.Intent(this, HomeActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
