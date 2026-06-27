package com.lafid.rentaja.activities.renter;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.lafid.rentaja.databinding.ActivityContractBinding;
import com.lafid.rentaja.models.Booking;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.FormatUtils;
import com.lafid.rentaja.utils.ThemeHelper;

public class ContractActivity extends AppCompatActivity {

    private ActivityContractBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityContractBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String bookingId = getIntent().getStringExtra("bookingId");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadContract(bookingId);
    }

    private void loadContract(String bookingId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.db().collection(FirebaseHelper.COL_BOOKINGS).document(bookingId)
            .get()
            .addOnSuccessListener(snap -> {
                binding.progressBar.setVisibility(View.GONE);
                Booking b = snap.toObject(Booking.class);
                if (b == null) { finish(); return; }
                b.setId(snap.getId());
                bindContract(b);
            });
    }

    private void bindContract(Booking b) {
        if (b.getId() != null && b.getId().length() >= 10) {
            binding.tvContractId.setText("#" + b.getId().substring(0, 10).toUpperCase());
        }
        binding.tvVehicleName.setText(b.getVehicleName());
        binding.tvOwnerName.setText(b.getOwnerName());
        binding.tvRenterName.setText(b.getRenterName());
        binding.tvRenterPhone.setText(b.getRenterPhone());
        binding.tvAddress.setText(b.getAddress());
        binding.tvStartDate.setText(FormatUtils.formatDate(b.getStartDate()));
        binding.tvEndDate.setText(FormatUtils.formatDate(b.getEndDate()));
        binding.tvDays.setText(b.getDays() + " hari");
        binding.tvRentalCost.setText(FormatUtils.formatRupiah(b.getRentalCost()));
        binding.tvDeposit.setText(FormatUtils.formatRupiah(b.getDeposit()));
        binding.tvTotal.setText(FormatUtils.formatRupiah(b.getTotal()));

        String method = b.getPaymentMethod();
        String label = "transfer".equals(method) ? "Transfer Bank"
                     : "cash".equals(method)     ? "Cash di Tempat"
                     : "E-Wallet";
        binding.tvPaymentMethod.setText(label);
    }
}
