package com.lafid.rentaja.activities.renter;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.lafid.rentaja.R;
import com.lafid.rentaja.databinding.ActivityBookingBinding;
import com.lafid.rentaja.models.Vehicle;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.FormatUtils;
import com.lafid.rentaja.utils.ThemeHelper;

import java.util.HashMap;
import java.util.Map;

import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private ActivityBookingBinding binding;
    private Vehicle vehicle;
    private String vehicleId;
    private String selectedPayment = "transfer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vehicleId = getIntent().getStringExtra("vehicleId");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Pre-fill dates
        binding.etStartDate.setText(FormatUtils.today());
        binding.etEndDate.setText(FormatUtils.tomorrow());

        // Pre-fill name
        if (FirebaseHelper.currentUser() != null) {
            String name = FirebaseHelper.currentUser().getDisplayName();
            binding.etRenterName.setText(name != null ? name : "");
        }

        loadVehicle();

        // Payment chip group
        binding.togglePayment.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnTransfer) {
                selectedPayment = "transfer";
            } else if (checkedId == R.id.btnCash) {
                selectedPayment = "cash";
            } else if (checkedId == R.id.btnEwallet) {
                selectedPayment = "ewallet";
            }
        });

        // Set default checked
        binding.btnTransfer.setChecked(true);

        binding.etStartDate.setOnClickListener(v -> showDatePicker(true));
        binding.etEndDate.setOnClickListener(v -> showDatePicker(false));
        binding.btnSubmit.setOnClickListener(v -> submitBooking());
    }

    private void loadVehicle() {
        FirebaseHelper.db().collection(FirebaseHelper.COL_VEHICLES).document(vehicleId)
            .get()
            .addOnSuccessListener(snap -> {
                vehicle = snap.toObject(Vehicle.class);
                if (vehicle == null) { finish(); return; }
                vehicle.setId(snap.getId());
                binding.tvVehicleName.setText(vehicle.getName());
                binding.tvVehiclePrice.setText(FormatUtils.formatRupiah(vehicle.getPricePerDay()) + " / hari");
                updateSummary();
            });
    }

    private void updateSummary() {
        if (vehicle == null) return;
        String start = binding.etStartDate.getText().toString();
        String end   = binding.etEndDate.getText().toString();
        if (start.isEmpty() || end.isEmpty()) return;

        int  days    = FormatUtils.daysBetween(start, end);
        long rental  = (long) days * vehicle.getPricePerDay();
        long total   = rental + vehicle.getDeposit();

        binding.tvSummaryDays.setText(days + " hari");
        binding.tvSummaryRental.setText(FormatUtils.formatRupiah(rental));
        binding.tvSummaryDeposit.setText(FormatUtils.formatRupiah(vehicle.getDeposit()));
        binding.tvSummaryTotal.setText(FormatUtils.formatRupiah(total));
    }

    private void showDatePicker(boolean isStart) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(isStart ? "Pilih Tanggal Mulai" : "Pilih Tanggal Selesai")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            // Convert milliseconds → "yyyy-MM-dd"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sdf.format(new Date(selection));

            if (isStart) {
                binding.etStartDate.setText(date);
            } else {
                binding.etEndDate.setText(date);
            }
            updateSummary(); // recalculate harga
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void submitBooking() {
        String start   = binding.etStartDate.getText().toString().trim();
        String end     = binding.etEndDate.getText().toString().trim();
        String name    = binding.etRenterName.getText().toString().trim();
        String phone   = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

        if (start.isEmpty() || end.isEmpty() || name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }

        int  days   = FormatUtils.daysBetween(start, end);
        long rental = (long) days * vehicle.getPricePerDay();
        long total  = rental + vehicle.getDeposit();

        Map<String, Object> booking = new HashMap<>();
        booking.put("vehicleId",      vehicle.getId());
        booking.put("vehicleName",    vehicle.getName());
        booking.put("vehicleCategory",vehicle.getCategory());
        booking.put("ownerId",        vehicle.getOwnerId());
        booking.put("ownerName",      vehicle.getOwnerName());
        booking.put("renterId",       FirebaseHelper.currentUser().getUid());
        booking.put("renterName",     name);
        booking.put("renterEmail",    FirebaseHelper.currentUser().getEmail());
        booking.put("renterPhone",    phone);
        booking.put("address",        address);
        booking.put("startDate",      start);
        booking.put("endDate",        end);
        booking.put("days",           days);
        booking.put("rentalCost",     rental);
        booking.put("deposit",        vehicle.getDeposit());
        booking.put("total",          total);
        booking.put("paymentMethod",  selectedPayment);
        booking.put("status",         "pending");
        booking.put("createdAt",      FieldValue.serverTimestamp());

        binding.btnSubmit.setEnabled(false);
        FirebaseHelper.db().collection(FirebaseHelper.COL_BOOKINGS)
            .add(booking)
            .addOnSuccessListener(ref -> {
                Toast.makeText(this, "Pengajuan terkirim!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                binding.btnSubmit.setEnabled(true);
                Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
}
