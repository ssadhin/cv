package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode_int";

    public static final int MODE_LIGHT = 0;
    public static final int MODE_DARK = 1;
    public static final int MODE_SYSTEM = 2;

    public static void setThemeMode(Context context, int mode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public static int getThemeMode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Default to MODE_LIGHT (0) or check old boolean for backward compatibility
        if (!pref.contains(KEY_THEME_MODE) && pref.contains("is_dark_mode")) {
            boolean wasDark = pref.getBoolean("is_dark_mode", false);
            int migrated = wasDark ? MODE_DARK : MODE_LIGHT;
            setThemeMode(context, migrated);
            return migrated;
        }
        return pref.getInt(KEY_THEME_MODE, MODE_SYSTEM); // Default to System now
    }

    public static void applyTheme(Activity activity) {
        int mode = getThemeMode(activity);
        switch (mode) {
            case MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        LocaleHelper.applyLocale(activity);
    }
}
