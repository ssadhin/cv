package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.example.myapplication.UserTierManager.Tier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionActivity extends AppCompatActivity {

    private UserTierManager tierManager;
    private TextView tvFreeCurrent;
    private android.widget.EditText etCouponCode;
    private Button btnSelectAdFree, btnSelectPlus, btnUpgradePro, btnJoinElite, btnApplyCoupon;
    private BillingClient billingClient;
    private Map<String, ProductDetails> productDetailsMap = new HashMap<>();
    
    private boolean isYearly = true; // Default to Yearly
    private TextView btnMonthly, btnYearly;
    private TextView tvElitePrice, tvEliteYearlyFee;
    private TextView tvProPrice, tvProYearlyFee;
    private TextView tvPlusPrice, tvPlusYearlyFee;
    private TextView tvAdFreePrice, tvAdFreeYearlyFee;
    
    // Layout containers for accordion
    private View layoutEliteCollapsed, layoutEliteExpanded;
    private View layoutProCollapsed, layoutProExpanded;
    private View layoutPlusCollapsed, layoutPlusExpanded;
    private View layoutAdFreeCollapsed, layoutAdFreeExpanded;
    private View layoutFreeCollapsed, layoutFreeExpanded;

    private final android.os.Handler breakTimerHandler = new android.os.Handler();
    private Runnable breakTimerRunnable;
    private View breakOverlay;
    private View btnCloseBreak;
    private View tvAdBlockMsg;
    private com.google.android.gms.ads.interstitial.InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        tierManager = new UserTierManager(this);
        initBilling();

        tvFreeCurrent = findViewById(R.id.tvFreeCurrent);
        btnSelectAdFree = findViewById(R.id.btnSelectAdFree);
        btnSelectPlus = findViewById(R.id.btnSelectPlus);
        btnUpgradePro = findViewById(R.id.btnUpgradePro);
        btnJoinElite = findViewById(R.id.btnJoinElite);
        etCouponCode = findViewById(R.id.etCouponCode);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);

        // Toggle Buttons
        btnMonthly = findViewById(R.id.btnMonthly);
        btnYearly = findViewById(R.id.btnYearly);
        
        // Price TextViews
        tvElitePrice = findViewById(R.id.tvElitePrice);
        tvEliteYearlyFee = findViewById(R.id.tvEliteYearlyFee);
        tvProPrice = findViewById(R.id.tvProPrice);
        tvProYearlyFee = findViewById(R.id.tvProYearlyFee);
        tvPlusPrice = findViewById(R.id.tvPlusPrice);
        tvPlusYearlyFee = findViewById(R.id.tvPlusYearlyFee);
        tvAdFreePrice = findViewById(R.id.tvAdFreePrice);
        tvAdFreeYearlyFee = findViewById(R.id.tvAdFreeYearlyFee);

        // Accordion layouts
        layoutEliteCollapsed = findViewById(R.id.layoutEliteCollapsed);
        layoutEliteExpanded = findViewById(R.id.layoutEliteExpanded);
        layoutProCollapsed = findViewById(R.id.layoutProCollapsed);
        layoutProExpanded = findViewById(R.id.layoutProExpanded);
        layoutPlusCollapsed = findViewById(R.id.layoutPlusCollapsed);
        layoutPlusExpanded = findViewById(R.id.layoutPlusExpanded);
        layoutAdFreeCollapsed = findViewById(R.id.layoutAdFreeCollapsed);
        layoutAdFreeExpanded = findViewById(R.id.layoutAdFreeExpanded);
        layoutFreeCollapsed = findViewById(R.id.layoutFreeCollapsed);
        layoutFreeExpanded = findViewById(R.id.layoutFreeExpanded);

        breakOverlay = findViewById(R.id.break_overlay);
        btnCloseBreak = findViewById(R.id.btn_close_break);
        tvAdBlockMsg = findViewById(R.id.tv_ad_block_msg);
        
        initMonetization();
        checkCurrentTier();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        setupToggle();

        // Setup Selection Logic (Accordion Toggles)
        findViewById(R.id.cardElite).setOnClickListener(v -> expandCard(Tier.ELITE));
        findViewById(R.id.cardPro).setOnClickListener(v -> expandCard(Tier.PRO));
        findViewById(R.id.cardPlus).setOnClickListener(v -> expandCard(Tier.PLUS));
        findViewById(R.id.cardAdFree).setOnClickListener(v -> expandCard(Tier.AD_FREE));
        findViewById(R.id.cardFree).setOnClickListener(v -> expandCard(Tier.FREE));
        
        // Button actions
        btnSelectAdFree.setOnClickListener(v -> handleSubscriptionSelection(Tier.AD_FREE));
        btnSelectPlus.setOnClickListener(v -> handleSubscriptionSelection(Tier.PLUS));
        btnUpgradePro.setOnClickListener(v -> handleSubscriptionSelection(Tier.PRO));
        btnJoinElite.setOnClickListener(v -> handleSubscriptionSelection(Tier.ELITE));
        
        btnApplyCoupon.setOnClickListener(v -> applyCoupon());

        updateUI();
        updatePricingUI(); // Initial pricing state
        
        // Style the Title "Elevate your Vitae"
        TextView tvSubTitle = findViewById(R.id.tvSubTitle);
        tvSubTitle.setText(android.text.Html.fromHtml(getString(R.string.sub_title_html), android.text.Html.FROM_HTML_MODE_LEGACY));
    }

    private void setBillingCycle(boolean yearly) {
        if (this.isYearly == yearly) return;
        this.isYearly = yearly;
        updatePricingUI();
    }

    private void updatePricingUI() {
        if (isYearly) {
            btnYearly.setBackgroundResource(R.drawable.toggle_selected_bg);
            btnYearly.setTextColor(Color.BLACK);
            btnMonthly.setBackground(null);
            btnMonthly.setTextColor(Color.GRAY);

            tvElitePrice.setText(R.string.elite_price_yearly);
            tvEliteYearlyFee.setVisibility(View.VISIBLE);
            tvProPrice.setText(R.string.pro_price_yearly);
            tvProYearlyFee.setVisibility(View.VISIBLE);
            tvPlusPrice.setText(R.string.plus_price_yearly);
            tvPlusYearlyFee.setVisibility(View.VISIBLE);
            tvAdFreePrice.setText(R.string.ad_free_price_yearly);
            tvAdFreeYearlyFee.setVisibility(View.VISIBLE);
        } else {
            btnMonthly.setBackgroundResource(R.drawable.toggle_selected_bg);
            btnMonthly.setTextColor(Color.BLACK);
            btnYearly.setBackground(null);
            btnYearly.setTextColor(Color.GRAY);

            tvElitePrice.setText(R.string.elite_price_monthly);
            tvEliteYearlyFee.setVisibility(View.GONE);
            tvProPrice.setText(R.string.pro_price_monthly);
            tvProYearlyFee.setVisibility(View.GONE);
            tvPlusPrice.setText(R.string.plus_price_monthly);
            tvPlusYearlyFee.setVisibility(View.GONE);
            tvAdFreePrice.setText(R.string.ad_free_price_monthly);
            tvAdFreeYearlyFee.setVisibility(View.GONE);
        }
    }

    private void expandCard(Tier tier) {
        // Reset all
        layoutEliteCollapsed.setVisibility(View.VISIBLE);
        layoutEliteExpanded.setVisibility(View.GONE);
        layoutProCollapsed.setVisibility(View.VISIBLE);
        layoutProExpanded.setVisibility(View.GONE);
        layoutPlusCollapsed.setVisibility(View.VISIBLE);
        layoutPlusExpanded.setVisibility(View.GONE);
        layoutAdFreeCollapsed.setVisibility(View.VISIBLE);
        layoutAdFreeExpanded.setVisibility(View.GONE);
        layoutFreeCollapsed.setVisibility(View.VISIBLE);
        layoutFreeExpanded.setVisibility(View.GONE);

        // Expand target
        switch (tier) {
            case ELITE:
                layoutEliteCollapsed.setVisibility(View.GONE);
                layoutEliteExpanded.setVisibility(View.VISIBLE);
                break;
            case PRO:
                layoutProCollapsed.setVisibility(View.GONE);
                layoutProExpanded.setVisibility(View.VISIBLE);
                break;
            case PLUS:
                layoutPlusCollapsed.setVisibility(View.GONE);
                layoutPlusExpanded.setVisibility(View.VISIBLE);
                break;
            case AD_FREE:
                layoutAdFreeCollapsed.setVisibility(View.GONE);
                layoutAdFreeExpanded.setVisibility(View.VISIBLE);
                break;
            case FREE:
                layoutFreeCollapsed.setVisibility(View.GONE);
                layoutFreeExpanded.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void initBilling() {
        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle cancellation
            }
        };

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        connectToBilling();
    }

    private void connectToBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Attempt to reconnect in some production logic here
            }
        });
    }

    private void queryProducts() {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        // Monthly
        productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId("ad_free_monthly").setProductType(BillingClient.ProductType.SUBS).build());
        productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId("plus_monthly").setProductType(BillingClient.ProductType.SUBS).build());
        // Yearly
        productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId("ad_free_yearly").setProductType(BillingClient.ProductType.SUBS).build());
        productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId("plus_yearly").setProductType(BillingClient.ProductType.SUBS).build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList != null) {
                    for (ProductDetails details : productDetailsList) {
                        productDetailsMap.put(details.getProductId(), details);
                    }
                }
            } else {
                Log.e("Billing", "Query failed: " + billingResult.getDebugMessage());
            }
        });
    }

    private void handleSubscriptionSelection(Tier tier) {
        if (tier == Tier.PRO || tier == Tier.ELITE) {
            Toast.makeText(this, R.string.unavailable_for_now, Toast.LENGTH_SHORT).show();
            return;
        }

        if (tier == Tier.AD_FREE || tier == Tier.PLUS) {
            launchBillingFlow(tier);
        } else {
            updateTier(tier);
        }
    }

    private void launchBillingFlow(Tier tier) {
        String productId;
        if (tier == Tier.AD_FREE) {
            productId = isYearly ? "ad_free_yearly" : "ad_free_monthly";
        } else {
            productId = isYearly ? "plus_yearly" : "plus_monthly";
        }

        ProductDetails productDetails = productDetailsMap.get(productId);

        if (productDetails != null) {
            String offerToken = "";
            if (productDetails.getSubscriptionOfferDetails() != null && !productDetails.getSubscriptionOfferDetails().isEmpty()) {
                offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
            }

            BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build();

            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(Collections.singletonList(productDetailsParams))
                    .build();

            billingClient.launchBillingFlow(this, flowParams);
        } else {
            String errorMsg = getString(R.string.billing_unavailable);
            if (productDetailsMap.isEmpty()) {
                errorMsg += "Check your Play Store connection or Console setup.";
            } else {
                errorMsg += getString(R.string.product_not_found_prefix, productId);
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            Log.e("Billing", "Cannot launch flow. Product missing: " + productId);
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        runOnUiThread(() -> applyPurchaseEffect(purchase));
                    } else {
                        runOnUiThread(() -> Toast.makeText(SubscriptionActivity.this, getString(R.string.purchase_ack_failed_prefix) + billingResult.getDebugMessage(), Toast.LENGTH_LONG).show());
                    }
                });
            } else {
                applyPurchaseEffect(purchase);
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Toast.makeText(this, R.string.purchase_pending_msg, Toast.LENGTH_LONG).show();
        }
    }

    private void applyPurchaseEffect(Purchase purchase) {
        String productId = purchase.getProducts().get(0);
        if (productId.equals("ad_free_monthly") || productId.equals("ad_free_yearly")) {
            tierManager.setTier(Tier.AD_FREE);
        } else if (productId.equals("plus_monthly") || productId.equals("plus_yearly")) {
            tierManager.setTier(Tier.PLUS);
        }
        updateUI();
        expandCard(tierManager.getUserTier());
        Toast.makeText(this, R.string.subscription_activated, Toast.LENGTH_LONG).show();
    }


    private void updateTier(Tier tier) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            Toast.makeText(this, R.string.login_to_subscribe, Toast.LENGTH_LONG).show();
            return;
        }

        tierManager.setTier(tier);
        Toast.makeText(this, getString(R.string.plan_updated_prefix) + tier.level, Toast.LENGTH_SHORT).show();
        setupToggle();
        initMonetization();
        checkCurrentTier();
    }

    private void initMonetization() {
        AdView adViewSub = findViewById(R.id.adViewSub);
        if (adViewSub != null) {
            if (tierManager.shouldShowAds()) {
                MobileAds.initialize(this, status -> {
                    Log.d("AdMob", "Sub Init Status: " + status.toString());
                    runOnUiThread(() -> {
                        adViewSub.setVisibility(View.VISIBLE);
                        
                        adViewSub.setAdListener(new com.google.android.gms.ads.AdListener() {
                            @Override
                            public void onAdLoaded() {
                                super.onAdLoaded();
                                Log.d("AdMob", "Subscription Banner Loaded Successfully");
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError adError) {
                                super.onAdFailedToLoad(adError);
                                String detailedError = "!!! SUB AD FAIL !!!\n" +
                                        "Code: " + adError.getCode() + "\n" +
                                        "Message: " + adError.getMessage();
                                Log.e("AdMob", detailedError);
                                // Toast.makeText(SubscriptionActivity.this, detailedError, Toast.LENGTH_SHORT).show();
                            }
                        });

                        AdRequest adRequest = new AdRequest.Builder().build();
                        adViewSub.loadAd(adRequest);
                    });
                });
            } else {
                adViewSub.setVisibility(View.GONE);
            }
        }

        // New Ad Break Logic (Interstitial)
        if (tierManager.shouldShowAds()) {
            loadInterstitialAd();
            startBreakTimer(); // Ensure the timer starts
        }
    }

    private void setupToggle() {
        if (btnMonthly != null) btnMonthly.setOnClickListener(v -> setBillingCycle(false));
        if (btnYearly != null) btnYearly.setOnClickListener(v -> setBillingCycle(true));
    }

    private void checkCurrentTier() {
        updateUI();
        expandCard(tierManager.getUserTier());
    }

    private void updateUI() {
        Tier current = tierManager.getUserTier();

        // Reset all
        tvFreeCurrent.setVisibility(View.GONE);
        btnSelectAdFree.setText(R.string.sub_select);
        btnSelectAdFree.setEnabled(true);
        btnSelectPlus.setText(R.string.sub_select);
        btnSelectPlus.setEnabled(true);
        btnUpgradePro.setText(R.string.sub_pro_btn);
        btnUpgradePro.setEnabled(true);
        btnJoinElite.setText(R.string.sub_elite_btn);
        btnJoinElite.setEnabled(true);

        // Highlight Current
        switch (current) {
            case FREE:
                tvFreeCurrent.setVisibility(View.VISIBLE);
                break;
            case AD_FREE:
                btnSelectAdFree.setText(R.string.current);
                btnSelectAdFree.setEnabled(false);
                break;
            case PLUS:
                btnSelectPlus.setText(R.string.current);
                btnSelectPlus.setEnabled(false);
                break;
            case PRO:
                btnUpgradePro.setText(R.string.current);
                btnUpgradePro.setEnabled(false);
                break;
            case ELITE:
                btnJoinElite.setText(R.string.current);
                btnJoinElite.setEnabled(false);
                break;
        }
    }

    private void applyCoupon() {
        String code = etCouponCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) return;

        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            Toast.makeText(this, "Please create an account to use coupons.", Toast.LENGTH_LONG).show();
            return;
        }

        String uid = user.getUid();
        btnApplyCoupon.setEnabled(false);
        btnApplyCoupon.setText(R.string.verifying);

        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://vitae-backend.asanistudiobangladesh.workers.dev/api/coupons/verify");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                org.json.JSONObject json = new org.json.JSONObject();
                json.put("uid", uid);
                json.put("code", code);

                java.io.OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                final int finalResponseCode = conn.getResponseCode();
                java.io.InputStream is = (finalResponseCode >= 200 && finalResponseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                String tempResult = "";
                if (is != null) {
                    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                    tempResult = s.hasNext() ? s.next() : "";
                }
                final String finalResult = tempResult;
                org.json.JSONObject tempJson;
                try {
                    tempJson = (!finalResult.isEmpty()) ? new org.json.JSONObject(finalResult) : new org.json.JSONObject();
                } catch (org.json.JSONException e) {
                    tempJson = new org.json.JSONObject();
                    try { tempJson.put("error", "Server returned non-JSON response. Check if backend is deployed."); } catch (Exception ignored) {}
                }
                final org.json.JSONObject responseJson = tempJson;

                runOnUiThread(() -> {
                    btnApplyCoupon.setEnabled(true);
                    btnApplyCoupon.setText(R.string.coupon_apply);
                    if (finalResponseCode == 200 && responseJson.optBoolean("success")) {
                        String newTierStr = responseJson.optString("tier", "FREE");
                        Tier newTier = Tier.valueOf(newTierStr);
                        tierManager.setTier(newTier);
                        
                        Toast.makeText(this, R.string.coupon_applied_success, Toast.LENGTH_LONG).show();
                        etCouponCode.setText("");
                        updateUI();
                        expandCard(newTier);
                        initMonetization();
                    } else {
                        String error = responseJson.optString("error", "Error " + finalResponseCode + ": Invalid Code or Link");
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        Log.e("Coupon", "Server Error: " + finalResponseCode + " - " + finalResult);
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnApplyCoupon.setEnabled(true);
                    btnApplyCoupon.setText(R.string.coupon_apply);
                    Toast.makeText(this, getString(R.string.connection_error_prefix) + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                Log.e("Coupon", "Verify Error", e);
            }
        }).start();
    }

    private void startBreakTimer() {
        if (!tierManager.shouldShowAds()) return;
        if (breakTimerRunnable != null) breakTimerHandler.removeCallbacks(breakTimerRunnable);
        
        breakTimerRunnable = () -> showBreakInterruption();
        // 3m = 180s, 5m = 300s
        long delay = UserTierManager.isFirstAdShownInSession ? 300000 : 180000;
        breakTimerHandler.postDelayed(breakTimerRunnable, delay);
    }

    private void showBreakInterruption() {
        if (isFinishing() || isDestroyed()) return;
        
        UserTierManager.isFirstAdShownInSession = true; // Flag as shown
        
        if (tvAdBlockMsg != null) tvAdBlockMsg.setVisibility(View.GONE);
        checkAdBlocker();

        if (breakOverlay != null) {
            breakOverlay.setVisibility(View.VISIBLE);
            if (mInterstitialAd != null) {
                mInterstitialAd.show(this);
                mInterstitialAd = null;
                loadInterstitialAd();
                breakOverlay.postDelayed(() -> {
                    if (btnCloseBreak != null) btnCloseBreak.setVisibility(View.VISIBLE);
                }, 3000);
            } else {
                breakOverlay.postDelayed(() -> {
                    if (btnCloseBreak != null) btnCloseBreak.setVisibility(View.VISIBLE);
                }, 5000);
            }
            if (btnCloseBreak != null) {
                btnCloseBreak.setOnClickListener(v -> {
                    breakOverlay.setVisibility(View.GONE);
                    btnCloseBreak.setVisibility(View.INVISIBLE);
                    startBreakTimer();
                });
            }
        }
    }

    private void loadInterstitialAd() {
        if (!tierManager.shouldShowAds()) return;
        AdRequest adRequest = new AdRequest.Builder().build();
        com.google.android.gms.ads.interstitial.InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
            new com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                }
                @Override
                public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError loadAdError) {
                    mInterstitialAd = null;
                }
            });
    }

    private void checkAdBlocker() {
        new Thread(() -> {
            try {
                java.net.InetAddress address = java.net.InetAddress.getByName("googleads.g.doubleclick.net");
                boolean isBlocked = address.getHostAddress().equals("127.0.0.1") || address.getHostAddress().equals("0.0.0.0");
                if (isBlocked) {
                    runOnUiThread(() -> {
                        if (tvAdBlockMsg != null) tvAdBlockMsg.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (tvAdBlockMsg != null) tvAdBlockMsg.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }
}
