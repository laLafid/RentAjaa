package com.lafid.rentaja.activities.renter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lafid.rentaja.adapters.BookingAdapter;
import com.lafid.rentaja.databinding.FragmentMyRentalsBinding;
import com.lafid.rentaja.models.Booking;
import com.lafid.rentaja.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MyRentalsFragment extends Fragment {
    private FragmentMyRentalsBinding binding;
    private BookingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentMyRentalsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi adapter dengan listener untuk melihat kontrak
        adapter = new BookingAdapter(new ArrayList<>(), 
            booking -> {
                // Dibuka saat user klik tombol 'Kontrak' (muncul jika status 'approved')
                Intent intent = new Intent(getContext(), ContractActivity.class);
                intent.putExtra("bookingId", booking.getId());
                startActivity(intent);
            }, 
            null // Owner action tidak digunakan di sisi penyewa
        );

        binding.rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBookings.setAdapter(adapter);

        loadMyBookings();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyBookings(); // Refresh data saat kembali ke fragment ini
    }

    private void loadMyBookings() {
        if (FirebaseHelper.currentUser() == null) return;
        
        if (binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);
        }

        String uid = FirebaseHelper.currentUser().getUid();

        // Query tanpa orderBy untuk menghindari masalah index Firestore
        FirebaseHelper.db().collection(FirebaseHelper.COL_BOOKINGS)
            .whereEqualTo("renterId", uid)
            .get()
            .addOnSuccessListener(snap -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);

                List<Booking> list = new ArrayList<>();
                for (var doc : snap.getDocuments()) {
                    try {
                        Booking b = doc.toObject(Booking.class);
                        if (b != null) {
                            b.setId(doc.getId());
                            list.add(b);
                        }
                    } catch (Exception e) {
                        Log.e("MyRentals", "Error parsing booking", e);
                    }
                }

                // Urutkan secara manual: terbaru di atas
                list.sort((b1, b2) -> {
                    if (b1.getCreatedAt() == null && b2.getCreatedAt() == null) return 0;
                    if (b1.getCreatedAt() == null) return 1;  // Taruh data tanpa timestamp di posisi paling bawah
                    if (b2.getCreatedAt() == null) return -1; // Taruh data tanpa timestamp di posisi paling bawah
                    return b2.getCreatedAt().compareTo(b1.getCreatedAt());
                });

                adapter.updateList(list);
                binding.emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MyRentals", "Firestore Error", e);
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
