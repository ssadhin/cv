package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.UserTierManager.Tier;

public class AISelectionActivity extends AppCompatActivity {

    private UserTierManager tierManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_selection);

        tierManager = new UserTierManager(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.cardManualAI).setOnClickListener(v -> {
            // Manual AI is Free (Manual Input)
            startActivity(new Intent(this, ManualAIActivity.class));
            finish();
        });

        findViewById(R.id.cardAutomaticAI).setOnClickListener(v -> {
            // Automatic AI is Pro Only ($10)
            Tier currentTier = tierManager.getUserTier();
            if (currentTier.price >= Tier.PRO.price) {
                // User has Pro or higher - launch automated tool (TBD implementation)
                Toast.makeText(this, R.string.launching_automated_ai, Toast.LENGTH_SHORT).show();
                // For now, redirecting to AIActivity as placeholder or separate automated path
                // Intent intent = new Intent(this, AutomatedAIActivity.class);
                // startActivity(intent);
                startActivity(new Intent(this, AIActivity.class));
                finish();
            } else {
                // Show Subscription Prompt
                showUpgradeRequiredDialog();
            }
        });
    }

    private void showUpgradeRequiredDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_ai_upgrade, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnUpgradeNow).setOnClickListener(v -> {
            dialog.dismiss();
            // Directly to SubscriptionActivity
            startActivity(new Intent(this, SubscriptionActivity.class));
        });

        dialogView.findViewById(R.id.btnMaybeLater).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
