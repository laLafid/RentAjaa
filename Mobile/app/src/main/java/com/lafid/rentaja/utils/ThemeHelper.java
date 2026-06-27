package com.lafid.rentaja.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.lafid.rentaja.R;

public class ThemeHelper {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME = "current_theme";
    private static final String KEY_DYNAMIC_COLOR = "dynamic_color";
    private static final String KEY_BLACK_MODE = "black_mode";
    private static final String KEY_ACCENT_COLOR = "accent_color";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    public static void applyTheme(Activity activity) {
        int accent = getAccentColor(activity);
        switch (accent) {
            case 1: activity.setTheme(R.style.Theme_RentHub_Blue); break;
            case 2: activity.setTheme(R.style.Theme_RentHub_Red); break;
            case 3: activity.setTheme(R.style.Theme_RentHub_Orange); break;
            default: activity.setTheme(R.style.Theme_RentHub); break;
        }

        if (isBlackModeEnabled(activity) && isDarkMode(activity)) {
            activity.getTheme().applyStyle(R.style.Theme_RentHub_Black, true);
        }

        if (isDynamicColorEnabled(activity)) {
            com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(activity);
        }
    }

    /**
     * Khusus untuk SplashActivity agar windowBackground saat startup juga berubah
     */
    public static void applySplashTheme(Activity activity) {
        int accent = getAccentColor(activity);
        switch (accent) {
            case 1: activity.setTheme(R.style.Theme_RentHub_Splash_Blue); break;
            case 2: activity.setTheme(R.style.Theme_RentHub_Splash_Red); break;
            case 3: activity.setTheme(R.style.Theme_RentHub_Splash_Orange); break;
            default: activity.setTheme(R.style.Theme_RentHub_Splash); break;
        }

        if (isBlackModeEnabled(activity) && isDarkMode(activity)) {
            activity.getTheme().applyStyle(R.style.Theme_RentHub_Black, true);
        }
    }

    public static boolean isDarkMode(Context context) {
        int nightMode = getSavedTheme(context);
        if (nightMode == THEME_DARK) return true;
        if (nightMode == THEME_SYSTEM) {
            int currentNightMode = context.getResources().getConfiguration().uiMode 
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }

    public static void applyThemeMode(int theme) {
        int mode;
        switch (theme) {
            case THEME_LIGHT: mode = AppCompatDelegate.MODE_NIGHT_NO; break;
            case THEME_DARK: mode = AppCompatDelegate.MODE_NIGHT_YES; break;
            default: mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; break;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void saveTheme(Context context, int theme) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putInt(KEY_THEME, theme).apply();
        applyThemeMode(theme);
    }

    public static int getSavedTheme(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_THEME, THEME_SYSTEM);
    }

    public static boolean isDynamicColorEnabled(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_DYNAMIC_COLOR, false);
    }

    public static void setDynamicColor(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply();
    }

    public static boolean isBlackModeEnabled(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_BLACK_MODE, false);
    }

    public static void setBlackMode(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_BLACK_MODE, enabled).apply();
    }
    
    public static int getAccentColor(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_ACCENT_COLOR, 0);
    }

    public static void setAccentColor(Context context, int colorIndex) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_ACCENT_COLOR, colorIndex).apply();
    }
}
