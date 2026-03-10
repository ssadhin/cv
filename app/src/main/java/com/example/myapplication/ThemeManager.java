package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_DARK_MODE = "is_dark_mode";

    public static void setTheme(Context context, boolean isDarkMode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_DARK_MODE, false); // Default to light mode (false)
    }

    public static void applyTheme(Activity activity) {
        if (isDarkMode(activity)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        LocaleHelper.applyLocale(activity);
    }
}
