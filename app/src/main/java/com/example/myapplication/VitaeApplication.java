package com.example.myapplication;

import android.app.Application;
import android.util.Log;
import com.google.android.gms.ads.MobileAds;

public class VitaeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Centralized AdMob Initialization
        com.google.android.gms.common.GoogleApiAvailability availability = com.google.android.gms.common.GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if (resultCode == com.google.android.gms.common.ConnectionResult.SUCCESS) {
            Log.d("AdMob", "Google Play Services is available. Initializing AdMob...");
            MobileAds.initialize(this, initializationStatus -> {
                Log.d("AdMob", "Application-level Initialization Complete");
            });
        } else {
            Log.e("AdMob", "Google Play Services NOT available. Result Code: " + resultCode);
        }
    }
}
