package com.lafid.rentaja.activities.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.lafid.rentaja.R;
import com.lafid.rentaja.activities.owner.OwnerMainActivity;
import com.lafid.rentaja.activities.renter.RenterMainActivity;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.LocaleHelper;
import com.lafid.rentaja.utils.ThemeHelper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySplashTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Even if permission is denied, we proceed to auth check after a delay
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                new Handler().postDelayed(this::checkAuthAndRoute, 1500);
            }
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(this::checkAuthAndRoute, 1500);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateLocationAndLanguage(location);
            } else {
                // If location is null, proceed anyway
                new Handler().postDelayed(this::checkAuthAndRoute, 1500);
            }
        }).addOnFailureListener(e -> {
            new Handler().postDelayed(this::checkAuthAndRoute, 1500);
        });
    }

    private void updateLocationAndLanguage(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String countryCode = address.getCountryCode(); // e.g., "ID", "US"
                String countryName = address.getCountryName();
                
                // Get flag emoji
                String flag = countryCodeToEmoji(countryCode);
                
                // Display Toast
                Toast.makeText(this, flag + " " + countryName, Toast.LENGTH_LONG).show();

                // Change Language based on Country Code
                // Simple mapping: ID -> in, others -> en (as fallback)
                String lang = countryCode.equalsIgnoreCase("ID") ? "in" : "en";
                LocaleHelper.setLocale(this, lang);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Proceed after showing toast for a bit
        new Handler().postDelayed(this::checkAuthAndRoute, 1500);
    }

    private String countryCodeToEmoji(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) return "";
        int firstLetter = Character.codePointAt(countryCode.toUpperCase(), 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCode.toUpperCase(), 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }

    private void checkAuthAndRoute() {
        if (!FirebaseHelper.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = FirebaseHelper.currentUser().getUid();
        FirebaseHelper.db()
            .collection(FirebaseHelper.COL_USERS)
            .document(uid)
            .get()
            .addOnSuccessListener(this::routeByRole)
            .addOnFailureListener(e -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
    }

    private void routeByRole(DocumentSnapshot snap) {
        String role = (snap != null && snap.exists()) ? snap.getString("role") : "renter";
        Intent intent = "owner".equals(role)
                ? new Intent(this, OwnerMainActivity.class)
                : new Intent(this, RenterMainActivity.class);
        startActivity(intent);
        finish();
    }
}
