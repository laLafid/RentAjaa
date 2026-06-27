package com.lafid.rentaja.activities.owner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lafid.rentaja.R;
import com.lafid.rentaja.activities.auth.LoginActivity;
import com.lafid.rentaja.databinding.ActivityOwnerMainBinding;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.ThemeHelper;

public class OwnerMainActivity extends AppCompatActivity {

    private ActivityOwnerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityOwnerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav();
        if (savedInstanceState == null) loadFragment(new OwnerDashboardFragment());
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) { loadFragment(new OwnerDashboardFragment()); return true; }
            if (id == R.id.nav_vehicles)  { loadFragment(new OwnerVehiclesFragment());  return true; }
            if (id == R.id.nav_requests)  { loadFragment(new OwnerRequestsFragment());  return true; }
            if (id == R.id.nav_profile)   { loadFragment(new OwnerProfileFragment());   return true; }
            return false;
        });
    }

    private void loadFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, f).commit();
    }

    public void logout() {
        FirebaseHelper.auth().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
