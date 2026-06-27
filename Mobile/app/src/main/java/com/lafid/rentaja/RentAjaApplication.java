package com.lafid.rentaja;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import com.lafid.rentaja.utils.ThemeHelper;

public class RentAjaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Inisialisasi tema global
        ThemeHelper.applyThemeMode(ThemeHelper.getSavedTheme(this));
        
        // Inisialisasi Dynamic Colors (Material You) jika diaktifkan
//        if (ThemeHelper.isDynamicColorEnabled(this)) {
//            DynamicColors.applyToActivitiesIfAvailable(this);
//        }
    }
}
