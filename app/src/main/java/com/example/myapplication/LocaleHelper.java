package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "language_prefs";
    private static final String KEY_LANG = "app_lang";

    public static void setLocale(Context context, String languageCode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_LANG, languageCode).apply();

        applyLocale(context);
    }

    /**
     * Returns the actual resolved language code used for resources (e.g. "en" or "bn")
     */
    public static String getLanguage(Context context) {
        String stored = getLanguageCode(context);
        if (stored.equals("auto")) {
            String systemLang = Locale.getDefault().getLanguage();
            // Default to English if system is not specifically Bangla
            if (systemLang != null && systemLang.startsWith("bn")) return "bn";
            return "en";
        }
        return stored;
    }

    /**
     * Returns the stored value: "auto", "en", or "bn"
     */
    public static String getLanguageCode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_LANG, "auto"); 
    }

    public static void applyLocale(Context context) {
        updateResources(context, getLanguage(context));
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        context.createConfigurationContext(configuration);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
