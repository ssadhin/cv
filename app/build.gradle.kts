 import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.asanistudiobd.vitae"
        minSdk = 24
        targetSdk = 36

        versionCode = 8
        versionName = "b2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val signingPropsFile = file("signing.properties")
    val signingProps = Properties()
    if (signingPropsFile.exists()) {
        signingProps.load(FileInputStream(signingPropsFile))
    }

    signingConfigs {
        create("release") {
            if (signingPropsFile.exists()) {
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
                storeFile = file(signingProps.getProperty("storeFile"))
                storePassword = signingProps.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            // Uses default debug keystore — no release password needed
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.vanniktech:android-image-cropper:4.5.0")
    implementation("com.jaredrummler:colorpicker:1.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.recaptcha)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.ads)
    implementation(libs.billing)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation("com.github.bumptech.glide:glide:4.16.0")
}

// Workaround for Android Studio cross-drive path issue ("'other' has different root")
tasks.matching { it.name.startsWith("produce") && it.name.endsWith("BundleIdeListingFile") }.configureEach {
    enabled = false
}
