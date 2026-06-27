package com.lafid.rentaja.activities.renter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lafid.rentaja.R;
import com.lafid.rentaja.activities.auth.LoginActivity;
import com.lafid.rentaja.databinding.ActivityRenterMainBinding;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.LocaleHelper;
import com.lafid.rentaja.utils.ThemeHelper;

public class RenterMainActivity extends AppCompatActivity {

    private ActivityRenterMainBinding binding;

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Terapkan tema sebelum super.onCreate
        ThemeHelper.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        binding = ActivityRenterMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new RenterHomeFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new RenterHomeFragment());
                return true;
            } else if (id == R.id.nav_rentals) {
                loadFragment(new MyRentalsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new RenterProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    public void logout() {
        FirebaseHelper.auth().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    
    public void restartActivity() {
        Intent intent = new Intent(this, RenterMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
