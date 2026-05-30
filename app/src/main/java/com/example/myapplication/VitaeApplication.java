package com.example.myapplication;

import android.app.Application;
import android.util.Log;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class VitaeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase App Check with Play Integrity
        // This ensures only genuine, unmodified installs from the Play Store
        // can access Firebase services (Firestore, Auth). Scripts and modified
        // APKs will be rejected with a cryptographic attestation failure.
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        );

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
