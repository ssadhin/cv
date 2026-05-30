package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserTierManager {
    private static final String TAG = "UserTierManager";
    private static final String API_BASE_URL = "https://vitae-backend.asanistudiobangladesh.workers.dev";
    
    public interface DeletionCallback {
        void onProcessed(boolean success, String message);
    }
    private static final String PREFS_NAME = "VitaeMonetizationPrefs";
    private static final String KEY_TRIAL_START = "trial_start_date";
    private static final String KEY_EXPORT_COUNT = "export_count_month";
    private static final String KEY_LAST_EXPORT_RESET = "last_export_reset_date";
    private static final String KEY_USER_TIER = "user_tier";
    private static final String KEY_CACHED_IP = "cached_ip_address";
    private static final String KEY_AI_COUNT = "ai_generation_count_month";
    private static final String KEY_TEMPLATE_COUNT = "template_download_count_month";
    private static final String KEY_LAST_LIMIT_RESET = "last_limit_reset_date";

    public static boolean isFirstAdShownInSession = false; // Track for scaling (3m -> 5m)

    public enum Tier {
        FREE(0, "Basic"),
        AD_FREE(1, "Ad-Free"),
        PLUS(5, "Plus"),
        PRO(10, "Pro"),
        ELITE(15, "Elite");

        public final int price;
        public final String level;

        Tier(int price, String level) {
            this.price = price;
            this.level = level;
        }
    }

    private final Context context;
    private final SharedPreferences prefs;
    private final OkHttpClient httpClient = new OkHttpClient();

    public UserTierManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ensureUserAuthenticated();
        syncTrialState();
    }

    private void ensureUserAuthenticated() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Anonymous auth successful");
                    syncUserToCloud();
                } else {
                    Log.e(TAG, "Anonymous auth failed", task.getException());
                    // Even if auth fails, we'll try to sync with Android ID as fallback
                    syncUserToCloud();
                }
            });
        } else {
            syncUserToCloud();
        }
    }

    /**
     * Checks if the user is currently in the 3-day premium-lite intro trial.
     */
    public boolean isInTrialPeriod() {
        if (getUserTier() != Tier.FREE) return false;
        return prefs.getBoolean("is_in_trial", false);
    }

    public Tier getUserTier() {
        String level = prefs.getString(KEY_USER_TIER, Tier.FREE.name());
        try {
            return Tier.valueOf(level);
        } catch (Exception e) {
            return Tier.FREE;
        }
    }

    public void setTier(Tier tier) {
        prefs.edit().putString(KEY_USER_TIER, tier.name()).apply();
        syncUserToCloud();
    }

    /**
     * Sets tier locally from a server-verified response.
     * Does NOT trigger cloud sync to avoid infinite loop.
     */
    public void setTierFromServer(Tier tier) {
        prefs.edit().putString(KEY_USER_TIER, tier.name()).apply();
    }

    /**
     * Syncs current user info to Firestore for the admin dashboard.
     */
    public void syncUserToCloud() {
        syncUserToCloud(null, null);
    }

    public void syncUserToCloud(String explicitName, String explicitEmail) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        // If user is null or anonymous, we use a guest ID based on Android ID to avoid duplicates
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        boolean isAnonymous = user == null || user.isAnonymous();
        String uid = isAnonymous ? "guest_" + androidId : user.getUid();
        
        // Device Metadata
        String deviceModel = android.os.Build.MODEL;
        String androidVersion = android.os.Build.VERSION.RELEASE;
        String ipAddress = prefs.getString(KEY_CACHED_IP, "Unknown");
        
        // Prefer explicit values, then Firebase values, then placeholders
        String email = explicitEmail;
        if (email == null) {
            email = (user != null && user.getEmail() != null) ? user.getEmail() : "Guest User";
        }
        
        String name = explicitName;
        if (name == null) {
            name = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Guest User";
        }

        long now = System.currentTimeMillis();
        long joinedDate = now;
        if (user != null && user.getMetadata() != null) {
            joinedDate = user.getMetadata().getCreationTimestamp();
        }

        Tier currentTier = getUserTier();
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("tier", currentTier.name());
        userData.put("tierPrice", currentTier.price);
        userData.put("lastUpdate", now);
        userData.put("lastSeen", now);
        userData.put("joinedDate", joinedDate);
        userData.put("isGuest", user == null || user.isAnonymous());
        userData.put("ip", ipAddress);
        userData.put("deviceModel", deviceModel);
        userData.put("androidVersion", androidVersion);
        userData.put("androidId", androidId);

        // 1. Primary Sync to Cloudflare User Registry (Independent of Firestore)
        syncToCloudflare(userData);

        // 2. Secondary/Legacy Sync to Firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User sync successful: " + uid);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "User sync failed: " + uid, e);
                    Toast.makeText(context, "Sync Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void syncToCloudflare(Map<String, Object> userData) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://vitae-backend.asanistudiobangladesh.workers.dev/api/users/sync");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                org.json.JSONObject json = new org.json.JSONObject();
                json.put("uid", userData.get("uid"));
                json.put("name", userData.get("name"));
                json.put("email", userData.get("email"));
                json.put("ip", userData.get("ip"));
                json.put("device_model", userData.get("deviceModel"));
                json.put("android_version", userData.get("androidVersion"));
                // NOTE: tier is NOT sent — server is authoritative for tier

                java.io.OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Cloudflare Sync Response: " + responseCode);

                if (responseCode == 200) {
                    java.io.InputStream is = conn.getInputStream();
                    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                    String responseStr = s.hasNext() ? s.next() : "";
                    
                    if (!responseStr.isEmpty()) {
                        JSONObject responseJson = new JSONObject(responseStr);
                        if (responseJson.has("tier")) {
                            String cloudTier = responseJson.getString("tier");
                            if (responseJson.has("ai_count")) {
                                prefs.edit().putInt(KEY_AI_COUNT, responseJson.getInt("ai_count")).apply();
                            }
                            if (responseJson.has("template_count")) {
                                prefs.edit().putInt(KEY_TEMPLATE_COUNT, responseJson.getInt("template_count")).apply();
                            }
                            if (responseJson.has("is_in_trial")) {
                                prefs.edit().putBoolean("is_in_trial", responseJson.getBoolean("is_in_trial")).apply();
                            }
                            Log.d(TAG, "Authoritative tier from server: " + cloudTier);
                            
                            // Server is authoritative — always update local tier to match
                            String currentLocalTier = prefs.getString(KEY_USER_TIER, Tier.FREE.name());
                                if (!currentLocalTier.equals(cloudTier)) {
                                    prefs.edit().putString(KEY_USER_TIER, cloudTier).apply();
                                    Log.i(TAG, "Tier synced from server: " + cloudTier);
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                                        Toast.makeText(context, "Tier Updated: " + cloudTier, Toast.LENGTH_SHORT).show());
                                }
                        }
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Cloudflare Sync Error", e);
            }
        }).start();
    }

    /**
     * Checks if the user passed the "True New User" check (Account, Device, IP).
     * If so, starts the 3-day ad-free trial.
     */
    private void syncTrialState() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // If trial already started, don't restart it
        if (prefs.getLong(KEY_TRIAL_START, -1) != -1) return;

        // Check Account Age (Trial only for accounts < 1 day old)
        long creationTimestamp = 0;
        if (user.getMetadata() != null) {
            creationTimestamp = user.getMetadata().getCreationTimestamp();
        }
        long now = System.currentTimeMillis();
        boolean isNewAccount = (now - creationTimestamp) < (24 * 60 * 60 * 1000);

        if (isNewAccount) {
            // Check IP and Device ID (simplified for now, ideally server-side)
            fetchIPAndStartTrial();
        }
    }

    private void fetchIPAndStartTrial() {
        Request request = new Request.Builder()
                .url("https://api.ipify.org?format=json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch IP", e);
                // Safe default: don't start trial if we can't verify uniqueness? 
                // Or be generous and start it anyway. Let's be generous for UX.
                startTrial();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        String ip = obj.getString("ip");
                        
                        String lastIp = prefs.getString(KEY_CACHED_IP, "");
                        if (lastIp.isEmpty() || !lastIp.equals(ip)) {
                            prefs.edit().putString(KEY_CACHED_IP, ip).apply();
                            startTrial();
                        }
                    } catch (Exception e) {
                        startTrial();
                    }
                } else {
                    startTrial();
                }
            }
        });
    }

    private void startTrial() {
        Log.d(TAG, "Starting 3-day welcome trial!");
        prefs.edit().putLong(KEY_TRIAL_START, System.currentTimeMillis()).apply();
    }

    public int getAICount() {
        return prefs.getInt(KEY_AI_COUNT, 0);
    }

    public void incrementAICount() {
        int current = prefs.getInt(KEY_AI_COUNT, 0);
        prefs.edit().putInt(KEY_AI_COUNT, current + 1).apply();
    }

    public int getTemplateCount() {
        return prefs.getInt(KEY_TEMPLATE_COUNT, 0);
    }

    public void incrementTemplateCount() {
        int current = prefs.getInt(KEY_TEMPLATE_COUNT, 0);
        prefs.edit().putInt(KEY_TEMPLATE_COUNT, current + 1).apply();
        
        // Notify server
        new Thread(() -> {
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    com.google.android.gms.tasks.Task<com.google.firebase.auth.GetTokenResult> task = user.getIdToken(false);
                    com.google.firebase.auth.GetTokenResult result = com.google.android.gms.tasks.Tasks.await(task, 10, java.util.concurrent.TimeUnit.SECONDS);
                    if (result != null && result.getToken() != null) {
                        java.net.URL url = new java.net.URL("https://vitae-backend.asanistudiobangladesh.workers.dev/api/users/usage");
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Authorization", "Bearer " + result.getToken());
                        conn.setDoOutput(true);
                        org.json.JSONObject json = new org.json.JSONObject();
                        json.put("uid", user.getUid());
                        java.io.OutputStream os = conn.getOutputStream();
                        os.write(json.toString().getBytes("UTF-8"));
                        os.close();
                        conn.getResponseCode();
                        conn.disconnect();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to increment template count on server", e);
            }
        }).start();
    }

    public boolean canExport() {
        return true;
    }

    public boolean canGenerateAI() {
        Tier tier = getUserTier();
        if (tier == Tier.ELITE || tier == Tier.PRO || tier == Tier.PLUS) return true;
        
        int limit = 3; // FREE
        if (tier == Tier.AD_FREE) limit = 5; // $1 tier
        
        return getAICount() < limit;
    }

    public boolean canDownloadTemplate() {
        Tier tier = getUserTier();
        if (tier == Tier.ELITE || tier == Tier.PRO || tier == Tier.PLUS) return true;
        
        int limit = 3; // FREE
        if (tier == Tier.AD_FREE) limit = 5; // $1 tier
        
        return getTemplateCount() < limit;
    }

    public boolean shouldShowAds() {
        return getUserTier() == Tier.FREE;
    }

    public void requestDataDeletion(List<String> types, DeletionCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onProcessed(false, "No authenticated user.");
            return;
        }

        String uid = user.getUid();
        user.getIdToken(false).addOnCompleteListener(task -> {
            String token = task.isSuccessful() && task.getResult() != null ? task.getResult().getToken() : "";
            
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(API_BASE_URL + "/api/user/delete-data");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    if (!token.isEmpty()) {
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                    }
                    conn.setDoOutput(true);

                    org.json.JSONObject json = new org.json.JSONObject();
                    json.put("uid", uid);
                    json.put("types", new org.json.JSONArray(types));

                    java.io.OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes("UTF-8"));
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        callback.onProcessed(true, "Data deletion successful.");
                    } else {
                        callback.onProcessed(false, "Server error: " + responseCode);
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    callback.onProcessed(false, e.getMessage());
                }
            }).start();
        });
    }

    public String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return (user == null || user.isAnonymous()) ? "guest_" + androidId : user.getUid();
    }

    public String getUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }
        return "Guest User";
    }

    public String getUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            return user.getEmail();
        }
        return "Guest User";
    }

    public void clearLocalData() {
        prefs.edit().clear().apply();
        Log.d(TAG, "Local data cleared successfully.");
    }
}
