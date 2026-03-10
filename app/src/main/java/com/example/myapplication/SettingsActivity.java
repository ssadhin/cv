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

public class SettingsActivity extends AppCompatActivity {

    private UserTierManager tierManager;
    private TextView tvSettingsName, tvSettingsTier, tvSettingsSubDesc;
    private ImageView ivSettingsProfile;
    private android.widget.LinearLayout btnAuthAction;
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
        
        // Theme toggle logic
        androidx.appcompat.widget.SwitchCompat switchTheme = findViewById(R.id.switchTheme);
        switchTheme.setChecked(ThemeManager.isDarkMode(this));
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setTheme(this, isChecked);
            // Restart the app from HomeActivity to apply theme globally
            android.content.Intent intent = new android.content.Intent(this, HomeActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadUserProfile();
        
        TextView tvLangValue = findViewById(R.id.tvLangValue);
        if (LocaleHelper.getLanguage(this).equals("bn")) {
            tvLangValue.setText(getString(R.string.lang_bangla));
        } else {
            tvLangValue.setText(getString(R.string.lang_english));
        }
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isAnonymous()) {
            tvSettingsName.setText(user.getDisplayName() != null ? user.getDisplayName() : getString(R.string.vitae_user));
            
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

        } else {
            // Guest State
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
        String[] languages = {getString(R.string.english), getString(R.string.bangla)};
        String[] codes = {"en", "bn"};
        
        int checkedItem = LocaleHelper.getLanguage(this).equals("bn") ? 1 : 0;
        
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

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_vitae)
                .setMessage(R.string.about_vitae_msg)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
