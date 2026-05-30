package com.example.myapplication;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ManualAIActivity extends AppCompatActivity {

    private View step1Container, step2Container, step3Container;
    private View progress1, progress2, progress3;
    private View btnNextStep1;
    private EditText etManualJsonInput;
    
    private int currentStep = 1;
    private boolean isPromptCopied = false;
    private View btnContinueToStep3;
    private ClipboardManager.OnPrimaryClipChangedListener clipListener;
    private InterstitialAd mInterstitialAd;
    private final android.os.Handler clipboardHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable clipboardRunnable;

    private List<TemplateItem> availableTemplates = new ArrayList<>();
    private TemplateItem selectedTemplateItem = null;
    
    private UserTierManager tierManager;

    private static class TemplateItem {
        String name;
        String path;
        boolean isAsset;
        Bitmap thumbnail;

        TemplateItem(String name, String path, boolean isAsset) {
            this.name = name;
            this.path = path;
            this.isAsset = isAsset;
        }
    }

    // MASTER_PROMPT is now externalized to R.string.master_prompt_simple

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        try {
            android.util.Log.d("MANUAL_AI_DEBUG", "Starting setContentView for activity_manual_ai");
            setContentView(R.layout.activity_manual_ai);
            android.util.Log.d("MANUAL_AI_DEBUG", "setContentView successful");
        } catch (Exception e) {
            android.util.Log.e("MANUAL_AI_DEBUG", "CRITICAL: Inflation failed for activity_manual_ai", e);
            if (e.getCause() != null) {
                android.util.Log.e("MANUAL_AI_DEBUG", "CAUSE 1: " + e.getCause().getMessage(), e.getCause());
                if (e.getCause().getCause() != null) {
                    android.util.Log.e("MANUAL_AI_DEBUG", "CAUSE 2: " + e.getCause().getCause().getMessage(), e.getCause().getCause());
                }
            }
            // Fallback to simpler layout if possible or rethrow to see crash
            throw e;
        }

        try {
            // Main Views from activity_manual_ai.xml
            progress1 = findViewById(R.id.progress1);
            progress2 = findViewById(R.id.progress2);
            progress3 = findViewById(R.id.progress3);

            // Scoped Finding from Includes
            View step1View = findViewById(R.id.includeStep1);
            View step2View = findViewById(R.id.includeStep2);
            View step3View = findViewById(R.id.includeStep3);

            if (step1View != null) {
                step1Container = step1View; // root is step1Container
                btnNextStep1 = step1View.findViewById(R.id.btnNextStep1);
                if (btnNextStep1 != null) {
                    btnNextStep1.setEnabled(false);
                    btnNextStep1.setAlpha(0.5f);
                    btnNextStep1.setOnClickListener(v -> goToStep(2));
                }
                
                View btnCopyPromptLarge = step1View.findViewById(R.id.btnCopyPromptLarge);
                if (btnCopyPromptLarge != null) {
                    btnCopyPromptLarge.setOnClickListener(v -> {
                        copyToClipboard(getString(R.string.master_prompt_advanced));
                        isPromptCopied = true;
                        if (btnNextStep1 != null) {
                            btnNextStep1.setEnabled(true);
                            btnNextStep1.setAlpha(1.0f);
                        }
                        showLargeToast(getString(R.string.manual_ai_prompt_copied_msg));
                    });
                }

                View btnSkipToStep3 = step1View.findViewById(R.id.btnSkipToStep3);
                if (btnSkipToStep3 != null) {
                    btnSkipToStep3.setOnClickListener(v -> goToStep(3));
                }
            }

            if (step2View != null) {
                step2Container = step2View;
                setupAIModelClickListeners(step2View);
                
                btnContinueToStep3 = step2View.findViewById(R.id.btnContinueToStep3);
                if (btnContinueToStep3 != null) {
                    btnContinueToStep3.setEnabled(false);
                    btnContinueToStep3.setAlpha(0.5f);
                    btnContinueToStep3.setOnClickListener(v -> goToStep(3));
                }

                View btnBackToStep1 = step2View.findViewById(R.id.btnBackToStep1);
                if (btnBackToStep1 != null) {
                    btnBackToStep1.setOnClickListener(v -> goToStep(1));
                }
            }

            // Setup Clipboard Listener for Step 2 -> 3
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) {
                clipListener = () -> {
                    if (currentStep == 2) checkClipboardForStep3();
                };
                cb.addPrimaryClipChangedListener(clipListener);
            }

            if (step3View != null) {
                step3Container = step3View;
                etManualJsonInput = step3View.findViewById(R.id.etManualJsonInput);
                View btnPaste = step3View.findViewById(R.id.btnPasteIntoInput);
                if (btnPaste != null) btnPaste.setOnClickListener(v -> pasteFromClipboard());
                View btnClear = step3View.findViewById(R.id.btnClearInput);
                if (btnClear != null) btnClear.setOnClickListener(v -> {
                    if (etManualJsonInput != null) etManualJsonInput.setText("");
                    showLargeToast(getString(R.string.cleared));
                });
                View btnGen = step3View.findViewById(R.id.btnGenerateFinal);
                if (btnGen != null) btnGen.setOnClickListener(v -> {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(this);
                        mInterstitialAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                generateCV();
                                loadInterstitialAd(); // Load next only after dismiss
                            }
                            @Override
                            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                generateCV();
                            }
                        });
                    } else {
                        generateCV();
                    }
                });
                View btnBack2 = step3View.findViewById(R.id.btnBackToStep2);
                if (btnBack2 != null) btnBack2.setOnClickListener(v -> goToStep(2));
            }

            View backBtn = findViewById(R.id.btnBackWizard);
            if (backBtn != null) backBtn.setOnClickListener(v -> handleBack());

            initMonetization();
            loadTemplates();
            updateStep1ButtonState();
            
            // Initialize Clipboard Polling Runnable
            clipboardRunnable = new Runnable() {
                @Override
                public void run() {
                    if (currentStep == 2) {
                        checkClipboardForStep3();
                        clipboardHandler.postDelayed(this, 1000); // Poll every 1 second
                    }
                }
            };

            goToStep(1);

        } catch (Exception e) {
            Log.e("ManualAI", "Crash in onCreate", e);
            Toast.makeText(this, R.string.module_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup Clipboard Listener
        if (clipListener != null) {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) cb.removePrimaryClipChangedListener(clipListener);
        }
        if (clipboardHandler != null && clipboardRunnable != null) {
            clipboardHandler.removeCallbacks(clipboardRunnable);
        }
    }


    private void launchAIModel(int cardId) {
        String primaryPackage = "";
        String secondaryPackage = "";
        String url = "";

        if (cardId == R.id.cardGemini) {
            primaryPackage = "com.google.android.apps.googleassistant";
            secondaryPackage = "com.google.android.apps.bard";
            url = getString(R.string.url_gemini);
        } else if (cardId == R.id.cardChatGPT) {
            primaryPackage = "com.openai.chatgpt";
            url = getString(R.string.url_chatgpt);
        } else if (cardId == R.id.cardGrok) {
            primaryPackage = "ai.x.grok";
            secondaryPackage = "com.twitter.android";
            url = getString(R.string.url_grok);
        } else if (cardId == R.id.cardDeepSeek) {
            primaryPackage = "com.deepseek.chat";
            url = getString(R.string.url_deepseek);
        }

        // 1. Show the instructional toast IMMEDIATELY (it stays for ~5s total)
        showLargeToast(getString(R.string.ai_paste_guide));
        
        // 2. Schedule the SECOND toast (5 seconds after the first ends, so ~10s from now)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            showLargeToast(getString(R.string.ai_copy_output_guide));
        }, 10000);

        // 3. Launch the AI (App-first fallback)
        boolean launched = false;
        if (!primaryPackage.isEmpty()) launched = tryLaunchApp(primaryPackage);
        if (!launched && !secondaryPackage.isEmpty()) launched = tryLaunchApp(secondaryPackage);

        if (!launched && !url.isEmpty()) {
            startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
        }
    }

    private void checkClipboardForStep3() {
        // Optimization: if already enabled, no need to check repeatedly
        if (btnContinueToStep3 != null && btnContinueToStep3.isEnabled()) return;
        
        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cb != null && cb.hasPrimaryClip()) {
            ClipData clip = cb.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    String content = text.toString().trim();
                    
                    // 1. Explicitly ignore the MASTER_PROMPT instructions
                    if (content.contains("You are a Resume Parser") || content.contains("STICK TO THE JSON FORMAT")) {
                        return;
                    }

                    // 2. Normalize: Remove markdown code blocks if present (```json ... ```)
                    String normalized = content;
                    if (normalized.contains("```")) {
                        normalized = normalized.replaceAll("```json|```JSON|```", "").trim();
                    }

                    // 3. Robust Key Detection
                    // We check for the core structural keys defined in the prompt
                    boolean hasHdr = normalized.contains("\"hdr\"") || normalized.contains("\"header\"");
                    boolean hasSum = normalized.contains("\"sum\"") || normalized.contains("\"summary\"");
                    boolean hasExp = normalized.contains("\"exp\"") || normalized.contains("\"experience\"");
                    boolean hasEdu = normalized.contains("\"edu\"") || normalized.contains("\"education\"");
                    boolean hasSkl = normalized.contains("\"skl\"") || normalized.contains("\"skills\"");
                    
                    // 4. Validate it's not the placeholder example
                    boolean isPlaceholder = normalized.contains("\"name\": \"Full Name\"");

                    // If we have at least Header and two other core sections, and it's not a placeholder
                    if (hasHdr && (hasExp || hasEdu || hasSkl) && !isPlaceholder) {
                        if (btnContinueToStep3 != null && !btnContinueToStep3.isEnabled()) {
                            btnContinueToStep3.setEnabled(true);
                            btnContinueToStep3.setAlpha(1.0f);
                            showLargeToast(getString(R.string.ai_output_detected));
                        }
                    }
                }
            }
        }
    }

    private boolean tryLaunchApp(String packageName) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            Log.e("ManualAI", "Launch failed for " + packageName, e);
        }
        return false;
    }



    private void setupAIModelClickListeners(View root) {
        int[] ids = {R.id.cardGemini, R.id.cardChatGPT, R.id.cardGrok, R.id.cardDeepSeek};
        for (int id : ids) {
            View v = root.findViewById(id);
            if (v != null) v.setOnClickListener(view -> launchAIModel(id));
        }
    }

    private void showLargeToast(String message) {
        try {
            // Using Activity context (this) is safer for custom views
            View layout = getLayoutInflater().inflate(R.layout.toast_custom, null);
            TextView text = layout.findViewById(R.id.toastText);
            if (text != null) text.setText(message);

            Toast toast = new Toast(this); 
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(android.view.Gravity.CENTER, 0, 0);
            toast.setView(layout);
            toast.show();
            
            // For longer duration, show a second one after a delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    toast.show(); 
                } catch (Exception ignore) {}
            }, 2000);
            
        } catch (Exception e) {
            Log.e("ManualAI", "Toast failed", e);
            // Fallback to standard toast which is more reliable in background
            Toast t = Toast.makeText(this, message, Toast.LENGTH_LONG);
            t.setGravity(android.view.Gravity.CENTER, 0, 0);
            t.show();
        }
    }

    private void updateStep1ButtonState() {
        if (btnNextStep1 != null) {
            btnNextStep1.setAlpha(isPromptCopied ? 1.0f : 0.4f);
        }
    }

    private void goToStep(int step) {
        currentStep = step;
        step1Container.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        step2Container.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        step3Container.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        if (step == 2) {
            checkClipboardForStep3(); // Check immediately when entering step 2
            clipboardHandler.removeCallbacks(clipboardRunnable); 
            clipboardHandler.post(clipboardRunnable); // Start polling
        } else {
            clipboardHandler.removeCallbacks(clipboardRunnable); // Stop polling in other steps
        }

        // Update Progress Bar
        if (progress1 == null) return; // safety
        
        int primary = androidx.core.content.ContextCompat.getColor(this, R.color.brand_primary);
        int inactive = 0xFFF1F5F9;

        progress1.setBackgroundColor(step >= 1 ? primary : inactive);
        progress2.setBackgroundColor(step >= 2 ? primary : inactive);
        progress3.setBackgroundColor(step >= 3 ? primary : inactive);
    }

    private void handleBack() {
        if (currentStep > 1) {
            goToStep(currentStep - 1);
        } else {
            finish();
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("AI Prompt", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            isPromptCopied = true;
            updateStep1ButtonState();
            // Toast.makeText(this, "Master Prompt copied!", Toast.LENGTH_SHORT).show(); 
            // Removed specific toast here since it's handled at call site
        }
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item != null && item.getText() != null) {
                etManualJsonInput.setText(item.getText());
                Toast.makeText(this, R.string.pasted_result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateCV() {
        String input = etManualJsonInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, R.string.please_paste_ai_output, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Final verification before template selection/launch
        showAdAndContinue(input);
    }

    private void showTemplateDialog(String input) {
        // Reuse original AIActivity template selection logic if possible or just use default
        // For simplicity and to match the "Wizard" feel, we'll use a dialog for now.
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_template_grid, null);
        androidx.recyclerview.widget.RecyclerView rv = dialogView.findViewById(R.id.rvTemplates);
        
        rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        
        // Simplified adapter for the dialog
        TemplateGridAdapter adapter = new TemplateGridAdapter();
        rv.setAdapter(adapter);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.generate, (dialog, which) -> {
                    showAdAndContinue(input);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAdAndContinue(String input) {
        if (tierManager.shouldShowAds() && mInterstitialAd != null) {
            mInterstitialAd.show(this);
            mInterstitialAd = null; // Used
        }
        processAndLaunch(input);
    }

    // --- Core Processing Logic Ported from AIActivity ---

    private void loadTemplates() {
        availableTemplates.clear();
        try {
            String[] assets = getAssets().list("default_templates");
            if (assets != null) {
                for (String assetName : assets) {
                    if (assetName.endsWith(".careercompass") || assetName.endsWith(".vitae")) {
                        String name = assetName.replace(".careercompass", "").replace(".vitae", "");
                        availableTemplates.add(new TemplateItem(name, "default_templates/" + assetName, true));
                    }
                }
            }
        } catch (Exception e) { Log.e("ManualAI", "Asset error", e); }

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
        if (availableTemplates.isEmpty()) availableTemplates.add(new TemplateItem(getString(R.string.template_standard), "default", true));
        
        for (TemplateItem item : availableTemplates) item.thumbnail = loadThumbnail(item);
        if (!availableTemplates.isEmpty()) selectedTemplateItem = availableTemplates.get(0);
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
                }
            } catch (Exception e) {}
        } else {
            File thumbFile = new File(item.path.replace(".json", ".png"));
            if (thumbFile.exists()) return BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
        }
        return null;
    }

    private void processAndLaunch(String input) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            // Launch as a new CV, bypassing the built-in template logic
            intent.putExtra("EXTRA_IS_NEW", true);
            // Send the raw AI output as a command to be handled surgically
            intent.putExtra("EXTRA_MANUAL_AI_COMMAND", input);
            
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.processing_failed_prefix) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String extractJson(String input) {
        if (input == null) return null;
        int start = input.indexOf('{');
        int end = input.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            String sub = input.substring(start, end + 1);
            try { new JSONObject(sub); return sub; } catch (Exception e) {}
        }
        return null;
    }

    private String loadVitaeAssetJson(String path) {
        try (InputStream is = getAssets().open(path);
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".json")) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024]; int l;
                    while ((l = zis.read(buf)) > 0) b.write(buf, 0, l);
                    return b.toString("UTF-8");
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private String loadFileJson(String path) {
        try {
            File file = new File(path);
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String l; while ((l = r.readLine()) != null) sb.append(l);
            }
            return sb.toString();
        } catch (Exception e) {}
        return null;
    }

    // --- Monetization ---

    private void initMonetization() {
        tierManager = new UserTierManager(this);
        if (tierManager.shouldShowAds()) {
            MobileAds.initialize(this, s -> {});
            loadInterstitialAd();
        }
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.ad_unit_id_interstitial), adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd ad) { mInterstitialAd = ad; }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError e) { mInterstitialAd = null; }
            });
    }

    // --- Template Adapter ---

    private class TemplateGridAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<TemplateGridAdapter.VH> {
        private int sel = 0;
        class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView iv; TextView tv; com.google.android.material.card.MaterialCardView card;
            VH(View v) { super(v); iv = v.findViewById(R.id.ivThumbnail); tv = v.findViewById(R.id.tvTemplateName); card = v.findViewById(R.id.cardTemplate); }
        }
        @Override public VH onCreateViewHolder(ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_template_grid, p, false));
        }
        @Override public void onBindViewHolder(VH h, int p) {
            TemplateItem item = availableTemplates.get(p);
            h.tv.setText(item.name);
            if (item.thumbnail != null) h.iv.setImageBitmap(item.thumbnail);
            h.card.setStrokeWidth(p == sel ? 8 : 0);
            h.card.setStrokeColor(androidx.core.content.ContextCompat.getColor(ManualAIActivity.this, R.color.brand_primary));
            h.itemView.setOnClickListener(v -> {
                int old = sel; sel = h.getAdapterPosition();
                selectedTemplateItem = availableTemplates.get(sel);
                notifyItemChanged(old); notifyItemChanged(sel);
            });
        }
        @Override public int getItemCount() { return availableTemplates.size(); }
    }
}
