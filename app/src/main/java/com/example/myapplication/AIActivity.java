package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.MobileAds;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayOutputStream;

public class AIActivity extends AppCompatActivity {

    private EditText etJsonInput;
    private View btnNext;
    private View btnBack;
    
    private List<TemplateItem> availableTemplates = new ArrayList<>();
    private TemplateItem selectedTemplateItem = null;
    
    private UserTierManager tierManager;
    private InterstitialAd mInterstitialAd;

    private static class TemplateItem {
        String name;
        String path; // asset path or absolute file path
        boolean isAsset;
        Bitmap thumbnail;

        TemplateItem(String name, String path, boolean isAsset) {
            this.name = name;
            this.path = path;
            this.isAsset = isAsset;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        etJsonInput = findViewById(R.id.etJsonInput);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        
        findViewById(R.id.btnCopyPrompt).setOnClickListener(v -> {
            copyToClipboard(getString(R.string.master_prompt_advanced));
            Toast.makeText(this, R.string.prompt_copied_msg, Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btnPaste).setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                if (item != null && item.getText() != null) {
                    etJsonInput.setText(item.getText());
                    Toast.makeText(this, R.string.pasted_from_clipboard, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            etJsonInput.setText("");
        });

        btnNext.setOnClickListener(v -> {
            if (!tierManager.canGenerateAI()) {
                Toast.makeText(this, R.string.ai_limit_reached, Toast.LENGTH_LONG).show();
                return;
            }
            String jsonInput = etJsonInput.getText().toString().trim();
            if (jsonInput.isEmpty()) {
                Toast.makeText(this, R.string.please_enter_data, Toast.LENGTH_SHORT).show();
                return;
            }
            loadTemplates(); // Scan before showing dialog
            showProcessAdAndContinue(jsonInput);
        });

        initMonetization();
    }

    private void loadTemplates() {
        availableTemplates.clear();
        // 1. Add Default Templates from Assets
        try {
            String[] assets = getAssets().list("default_templates");
            if (assets != null) {
                for (String assetName : assets) {
                    if (assetName.endsWith(".vitae")) {
                        String name = assetName.replace(".vitae", "");
                        availableTemplates.add(new TemplateItem(name, "default_templates/" + assetName, true));
                    }
                }
            }
        } catch (java.io.IOException e) {
            Log.e("AIActivity", "Error listing asset templates", e);
        }

        // 2. Add User-Saved Templates from Files
        File dir = new File(getFilesDir(), "user_templates");
        if (dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName().replace("Template_", "").replace(".json", "");
                    availableTemplates.add(new TemplateItem(name, f.getAbsolutePath(), false));
                }
            }
        }
        
        // Ensure we have at least one (fallback if needed)
        if (availableTemplates.isEmpty()) {
            availableTemplates.add(new TemplateItem(getString(R.string.template_standard_modern), "default", true));
        }

        // Load thumbnails for all
        for (TemplateItem item : availableTemplates) {
            item.thumbnail = loadThumbnail(item);
        }
        
        selectedTemplateItem = availableTemplates.get(0);
    }

    private Bitmap loadThumbnail(TemplateItem item) {
        if (item.path.equals("default")) return null;
        
        if (item.isAsset) {
            try (InputStream is = getAssets().open(item.path);
                 ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().toLowerCase().endsWith(".png")) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                        byte[] bytes = baos.toByteArray();
                        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                    zis.closeEntry();
                }
            } catch (Exception e) {
                Log.e("AIActivity", "Error loading asset thumbnail: " + item.path, e);
            }
        } else {
            File thumbFile = new File(item.path.replace(".json", ".png"));
            if (thumbFile.exists()) {
                return BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
            }
        }
        return null;
    }

    private boolean isValidJson(String test) {
        try {
            new JSONObject(test);
            return true;
        } catch (JSONException ex) {
            try {
                new org.json.JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    // RecyclerView adapter for grid-based template selection
    private class TemplateGridAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<TemplateGridAdapter.ViewHolder> {
        private int selectedPosition = 0;

        class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView ivThumbnail;
            TextView tvName;
            com.google.android.material.card.MaterialCardView card;

            ViewHolder(View itemView) {
                super(itemView);
                ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
                tvName = itemView.findViewById(R.id.tvTemplateName);
                card = itemView.findViewById(R.id.cardTemplate);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template_grid, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TemplateItem item = availableTemplates.get(position);
            holder.tvName.setText(item.name);
            
            if (item.thumbnail != null) {
                holder.ivThumbnail.setImageBitmap(item.thumbnail);
            } else {
                holder.ivThumbnail.setImageResource(R.drawable.ic_description);
            }

            // Selection indicator
            if (position == selectedPosition) {
                holder.card.setStrokeColor(0xFF1a237e);
                holder.card.setStrokeWidth(4);
            } else {
                holder.card.setStrokeColor(0x00000000);
                holder.card.setStrokeWidth(0);
            }

            holder.itemView.setOnClickListener(v -> {
                int old = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                selectedTemplateItem = availableTemplates.get(selectedPosition);
                notifyItemChanged(old);
                notifyItemChanged(selectedPosition);
            });
        }

        @Override
        public int getItemCount() {
            return availableTemplates.size();
        }
    }

    private void showTemplateSelectionDialog(final String jsonInput) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_template_grid, null);
        androidx.recyclerview.widget.RecyclerView rv = dialogView.findViewById(R.id.rvTemplates);
        
        rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        TemplateGridAdapter adapter = new TemplateGridAdapter();
        rv.setAdapter(adapter);
        
        // Set default selection
        if (!availableTemplates.isEmpty()) {
            selectedTemplateItem = availableTemplates.get(0);
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.generate_cv, (dialog, which) -> {
                     showProcessAdAndContinue(jsonInput);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void processAndLaunch(String input) {
        try {
            List<ResumeDataManager.SectionModel> sections;
            JSONObject originalData = null;

            Log.d("AIActivity", "Input starts with: " + input.substring(0, Math.min(100, input.length())));

            String jsonInput = extractJson(input);

            if (jsonInput != null) {
                // 1. Parse JSON Input
                sections = ResumeDataManager.parseStructuredJson(jsonInput);
                try {
                    originalData = new JSONObject(jsonInput);
                } catch (JSONException e) {
                    Log.e("AIActivity", "Failed to parse extracted JSON as JSONObject", e);
                    originalData = new JSONObject();
                }
            } else {
                // 2. Parse Smart Text (Fallback)
                sections = ResumeDataManager.parseSmartText(input);
                originalData = new JSONObject(); // Empty fallback
            }
            
            Log.d("AIActivity", "Parsed " + sections.size() + " sections");
            for (ResumeDataManager.SectionModel s : sections) {
                Log.d("AIActivity", "  Section: " + s.id + " (" + s.name + ") with " + s.items.size() + " items");
            }
            
            // 2. Generate State JSON (HTML + Metadata)
            JSONObject finalState = ResumeDataManager.generateStateJson(sections, originalData);
            Log.d("AIActivity", "Generated state HTML length: " + (finalState.has("html") ? finalState.getString("html").length() : 0));
            
            // 3. Prepare Template JSON
            String templateJson = null;
            if (selectedTemplateItem != null) {
                if (selectedTemplateItem.isAsset && !selectedTemplateItem.path.equals("default")) {
                    templateJson = loadVitaeAssetJson(selectedTemplateItem.path);
                } else if (!selectedTemplateItem.isAsset) {
                    templateJson = loadFileJson(selectedTemplateItem.path);
                }
            }

            // 4. Launch Editor
            tierManager.incrementAICount();
            launchEditor(finalState, selectedTemplateItem != null ? selectedTemplateItem.path : "default", templateJson);
            
        } catch (Exception e) {
            Log.e("AIActivity", "Error processing data", e);
            Toast.makeText(this, getString(R.string.error_generating_cv_prefix) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String extractJson(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        
        // Try to find the first '{' and last '}'
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        
        if (start != -1 && end != -1 && end > start) {
            String extracted = trimmed.substring(start, end + 1);
            if (isValidJson(extracted)) {
                return extracted;
            }
        }
        
        // If not found or invalid, try markdown code blocks
        if (trimmed.contains("```")) {
            String[] parts = trimmed.split("```");
            for (String part : parts) {
                // Skip the "json" marker if present
                String potential = part.trim();
                if (potential.startsWith("json")) {
                    potential = potential.substring(4).trim();
                }
                if (potential.startsWith("{") && potential.endsWith("}") && isValidJson(potential)) {
                    return potential;
                }
            }
        }
        
        // Fallback to original if it's already a valid JSON
        if (isValidJson(trimmed)) return trimmed;
        
        return null;
    }

    private void launchEditor(JSONObject state, String templateId, String templateJson) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("EXTRA_FROM_STEP_BY_STEP", true);
        intent.putExtra("EXTRA_TARGET_LAYOUT", templateId);
        intent.putExtra("EXTRA_STEP_BY_STEP_DATA", state.toString());
        if (templateJson != null) {
            intent.putExtra("EXTRA_TEMPLATE_JSON", templateJson);
        }
        
        // Generate a filename
        String autoPath = generateAutoFilename(state);
        if (autoPath != null) {
            intent.putExtra("EXTRA_GENERATED_FILEPATH", autoPath);
        }
        
        startActivity(intent);
        finish();
    }

    private String loadVitaeAssetJson(String assetPath) {
        try (java.io.InputStream is = getAssets().open(assetPath);
             java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(is)) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".json")) {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                    return baos.toString("UTF-8");
                }
                zis.closeEntry();
            }
        } catch (java.io.IOException e) {
            Log.e("AIActivity", "Error reading .vitae asset: " + assetPath, e);
        }
        return null;
    }

    private String loadFileJson(String path) {
        try {
            java.io.File file = new java.io.File(path);
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            return sb.toString();
        } catch (java.io.IOException e) {
            Log.e("AIActivity", "Error reading template file: " + path, e);
        }
        return null;
    }

    private String generateAutoFilename(JSONObject state) {
        try {
            String name = "User";
            if (state.has("header")) {
                name = state.getJSONObject("header").optString("name", "User");
            }
            // Sanitize
            String lastName = name.trim();
            if (lastName.contains(" ")) {
                lastName = lastName.substring(lastName.lastIndexOf(" ") + 1);
            }
            lastName = lastName.replaceAll("[^a-zA-Z0-9]", "");
            if (lastName.isEmpty()) lastName = "User";
            
            String purpose = "AI_Generated";
            
            File dir = new File(getFilesDir(), "resumes");
            if (!dir.exists()) dir.mkdirs();
            
            int count = 1;
            File file;
            do {
                String fname = String.format("%s %s CV %d.json", lastName, purpose, count);
                file = new File(dir, fname);
                count++;
            } while (file.exists());
            
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("AI Prompt", text);
        clipboard.setPrimaryClip(clip);
    }
    
    // MASTER_PROMPT is now externalized to R.string.master_prompt_advanced
    private void initMonetization() {
        tierManager = new UserTierManager(this);
        // Manual AI shows ads for free users. PRO users never see ads.
        if (tierManager.shouldShowAds()) {
            MobileAds.initialize(this, status -> {});
            loadAndShowEntryAd();
        }
    }

    private void loadAndShowEntryAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.ad_unit_id_interstitial), adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                    mInterstitialAd.show(AIActivity.this);
                    // Pre-load the second one for processing
                    loadInterstitialAd();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    mInterstitialAd = null;
                    loadInterstitialAd(); // Still try to load the second one
                }
            });
    }

    private void loadInterstitialAd() {
        if (!tierManager.shouldShowAds()) return;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.ad_unit_id_interstitial), adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                }
            });
    }

    private void showProcessAdAndContinue(String input) {
        if (tierManager.shouldShowAds() && mInterstitialAd != null) {
            mInterstitialAd.show(this);
            mInterstitialAd = null; // Used
        }
        processAndLaunch(input);
    }
}
