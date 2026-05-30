package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.UserTierManager.Tier;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AISelectionActivity extends AppCompatActivity {

    private UserTierManager tierManager;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(chain -> {
                okhttp3.Request original = chain.request();
                try {
                    com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        com.google.android.gms.tasks.Task<com.google.firebase.auth.GetTokenResult> task = user.getIdToken(false);
                        com.google.firebase.auth.GetTokenResult result = com.google.android.gms.tasks.Tasks.await(task, 10, java.util.concurrent.TimeUnit.SECONDS);
                        if (result != null && result.getToken() != null) {
                            return chain.proceed(original.newBuilder()
                                .header("Authorization", "Bearer " + result.getToken())
                                .build());
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("AuthInterceptor", "Failed to get token", e);
                }
                return chain.proceed(original);
            })
            .build();
    private static final String API_BASE_URL = "https://vitae-backend.asanistudiobangladesh.workers.dev";

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

        // --- Demo Logic ---
        android.view.View btnTryDemo = dialogView.findViewById(R.id.btnTryDemo);
        android.view.View layoutDemoInput = dialogView.findViewById(R.id.layoutDemoInput);
        android.widget.EditText etDemoPrompt = dialogView.findViewById(R.id.etDemoPrompt);
        android.view.View btnDemoSend = dialogView.findViewById(R.id.btnDemoSend);
        android.widget.ProgressBar pbDemoThinking = dialogView.findViewById(R.id.pbDemoThinking);
        android.widget.TextView tvDemoResult = dialogView.findViewById(R.id.tvDemoResult);

        btnTryDemo.setOnClickListener(v -> {
            btnTryDemo.setVisibility(android.view.View.GONE);
            layoutDemoInput.setVisibility(android.view.View.VISIBLE);
        });

        btnDemoSend.setOnClickListener(v -> {
            String prompt = etDemoPrompt.getText().toString().trim();
            if (prompt.isEmpty()) return;

            // Show thinking state
            pbDemoThinking.setVisibility(android.view.View.VISIBLE);
            btnDemoSend.setEnabled(false);
            btnDemoSend.setVisibility(android.view.View.INVISIBLE);
            tvDemoResult.setVisibility(android.view.View.GONE);

            // Construct Demo Request (Simple context)
            String systemPrompt = "You are a helpful AI Resume Assistant. " +
                    "This is a DEMO restricted to text information only. " +
                    "Explain your answer but keep it concise and professional. " +
                    "If the user asks to add something, describe HOW you would add it to their CV.\n\n" +
                    "USER DEMO REQUEST: " + prompt;

            JSONObject body = new JSONObject();
            try {
                body.put("prompt", systemPrompt);
                body.put("model", "gemini-1.5-flash"); // Use stable flash for demo
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                body.toString()
            );

            Request request = new Request.Builder()
                .url(API_BASE_URL + "/api/ai/chat")
                .post(requestBody)
                .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        pbDemoThinking.setVisibility(android.view.View.GONE);
                        btnDemoSend.setEnabled(true);
                        btnDemoSend.setVisibility(android.view.View.VISIBLE);
                        tvDemoResult.setText("Error: " + e.getMessage());
                        tvDemoResult.setVisibility(android.view.View.VISIBLE);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        pbDemoThinking.setVisibility(android.view.View.GONE);
                        btnDemoSend.setEnabled(true);
                        btnDemoSend.setVisibility(android.view.View.VISIBLE);
                        
                        try {
                            if (response.isSuccessful()) {
                                JSONObject json = new JSONObject(responseData);
                                if (json.has("candidates")) {
                                    String aiText = json.getJSONArray("candidates")
                                        .getJSONObject(0)
                                        .getJSONObject("content")
                                        .getJSONArray("parts")
                                        .getJSONObject(0)
                                        .getString("text");
                                    
                                    tvDemoResult.setText(aiText.trim());
                                    tvDemoResult.setVisibility(android.view.View.VISIBLE);
                                } else {
                                    tvDemoResult.setText("Error: Unexpected AI response format.");
                                    tvDemoResult.setVisibility(android.view.View.VISIBLE);
                                }
                            } else {
                                tvDemoResult.setText("Error: Server returned " + response.code());
                                tvDemoResult.setVisibility(android.view.View.VISIBLE);
                            }
                        } catch (Exception e) {
                            tvDemoResult.setText("Error: " + e.getMessage());
                            tvDemoResult.setVisibility(android.view.View.VISIBLE);
                        }
                    });
                }
            });
        });

        dialog.show();
    }
}
