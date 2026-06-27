package com.lafid.rentaja.activities.owner;

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
import com.lafid.rentaja.databinding.FragmentOwnerRequestsBinding;
import com.lafid.rentaja.models.Booking;
import com.lafid.rentaja.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class OwnerRequestsFragment extends Fragment {
    private FragmentOwnerRequestsBinding binding;
    private BookingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentOwnerRequestsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new BookingAdapter(new ArrayList<>(),
            null, // Renter contract view tidak di sini
            booking -> showApproveRejectDialog(booking) // Owner action
        );

        binding.rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRequests.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        if (FirebaseHelper.currentUser() == null) return;
        String uid = FirebaseHelper.currentUser().getUid();

        if (binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);
        }

        // Ambil semua booking untuk unit milik owner ini
        FirebaseHelper.db().collection(FirebaseHelper.COL_BOOKINGS)
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener(snap -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);

                List<Booking> list = new ArrayList<>();
                for (var doc : snap.getDocuments()) {
                    Booking b = doc.toObject(Booking.class);
                    if (b != null) {
                        b.setId(doc.getId());
                        list.add(b);
                    }
                }

                // Urutkan manual: yang terbaru (pending) di atas
                list.sort((b1, b2) -> {
                    if (b1.getCreatedAt() == null || b2.getCreatedAt() == null) return 0;
                    return b2.getCreatedAt().compareTo(b1.getCreatedAt());
                });

                adapter.updateList(list);
                binding.emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Log.e("OwnerRequests", "Firestore Error", e);
            });
    }

    private void showApproveRejectDialog(Booking booking) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Sewa")
            .setMessage("Terima permintaan sewa " + booking.getVehicleName() + " dari " + booking.getRenterName() + "?")
            .setPositiveButton("Setujui", (d, w) -> updateStatus(booking.getId(), "approved"))
            .setNegativeButton("Tolak", (d, w) -> updateStatus(booking.getId(), "rejected"))
            .setNeutralButton("Nanti", null)
            .show();
    }

    private void updateStatus(String bookingId, String status) {
        if (binding != null) binding.progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.db().collection(FirebaseHelper.COL_BOOKINGS)
            .document(bookingId)
            .update("status", status)
            .addOnSuccessListener(unused -> {
                Toast.makeText(getContext(), "Status berhasil diperbarui", Toast.LENGTH_SHORT).show();
                loadRequests();
            })
            .addOnFailureListener(e -> {
                if (binding != null) binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRequests();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
