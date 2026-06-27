package com.lafid.rentaja.activities.renter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.lafid.rentaja.R;
import com.lafid.rentaja.databinding.ActivityVehicleDetailBinding;
import com.lafid.rentaja.models.Vehicle;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.FormatUtils;
import com.lafid.rentaja.utils.ThemeHelper;

public class VehicleDetailActivity extends AppCompatActivity {

    private ActivityVehicleDetailBinding binding;
    private Vehicle vehicle;
    private String vehicleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Terapkan tema sebelum super.onCreate
        ThemeHelper.applyTheme(this);
        
        super.onCreate(savedInstanceState);

        // 1. Setup Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);

        binding = ActivityVehicleDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle Status Bar Insets (Top)
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Handle Toolbar Horizontal Insets (Safe area for back icon)
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            int sideMargin = (int) (24 * getResources().getDisplayMetrics().density);
            v.setPadding(bars.left + sideMargin, 0, bars.right + sideMargin, 0);
            return insets;
        });

        vehicleId = getIntent().getStringExtra("vehicleId");
        if (vehicleId == null) {
            finish();
            return;
        }

        // 2. Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadVehicle();

        binding.btnBook.setOnClickListener(v -> {
            if (!FirebaseHelper.isLoggedIn()) {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("vehicleId", vehicleId);
            startActivity(intent);
        });
    }

    private void loadVehicle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.db()
                .collection(FirebaseHelper.COL_VEHICLES)
                .document(vehicleId)
                .get()
                .addOnSuccessListener(snap -> {
                    binding.progressBar.setVisibility(View.GONE);
                    vehicle = snap.toObject(Vehicle.class);
                    if (vehicle == null) {
                        Toast.makeText(this, "Data kendaraan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    vehicle.setId(snap.getId());
                    bindVehicle();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void bindVehicle() {
        binding.tvName.setText(vehicle.getName());
        binding.tvPrice.setText(FormatUtils.formatRupiah(vehicle.getPricePerDay()));
        binding.tvDepositVal.setText(FormatUtils.formatRupiah(vehicle.getDeposit()));
        binding.tvDesc.setText(vehicle.getDesc());
        binding.tvOwner.setText(vehicle.getOwnerName());
        binding.tvCategory.setText(vehicle.getCategory());

        // Set Owner Initial
        if (vehicle.getOwnerName() != null && !vehicle.getOwnerName().isEmpty()) {
            binding.tvOwnerAvatar.setText(vehicle.getOwnerName().substring(0, 1).toUpperCase());
        }

        binding.chipGroupSpecs.removeAllViews();
        if (vehicle.getSpecs() != null) {
            for (String spec : vehicle.getSpecs()) {
                Chip chip = new Chip(this);
                chip.setText(spec);
                chip.setChipBackgroundColorResource(android.R.color.transparent);
                chip.setChipStrokeColorResource(R.color.divider);
                chip.setChipStrokeWidth(1.0f);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                binding.chipGroupSpecs.addView(chip);
            }
        }

        if (vehicle.getImageUrl() != null && !vehicle.getImageUrl().isEmpty()) {
            binding.ivVehicle.setVisibility(View.VISIBLE);
            binding.tvEmoji.setVisibility(View.GONE);
            Glide.with(this)
                    .load(vehicle.getImageUrl())
                    .placeholder(R.color.primary_light)
                    .centerCrop()
                    .into(binding.ivVehicle);
        } else {
            binding.ivVehicle.setVisibility(View.GONE);
            binding.tvEmoji.setVisibility(View.VISIBLE);
            binding.tvEmoji.setText(vehicle.getCategoryEmoji());
        }

        boolean avail = vehicle.isAvailable();
        binding.btnBook.setEnabled(avail);
        binding.btnBook.setText(avail ? "Ajukan Sewa" : "Tidak Tersedia");

        binding.chipStatus.setText(avail ? "Tersedia" : "Disewa");
        // Status chips often have specific colors that don't change with accent, 
        // but let's make them at least readable in dark mode by adjusting background if needed.
        // For now keep them as they are or use specific status colors.
    }
}
