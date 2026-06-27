package com.lafid.rentaja.activities.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lafid.rentaja.databinding.FragmentOwnerDashboardBinding;
import com.lafid.rentaja.utils.FirebaseHelper;

public class OwnerDashboardFragment extends Fragment {
    private FragmentOwnerDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentOwnerDashboardBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnQuickAdd.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddVehicleActivity.class));
        });

        loadDashboardData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (FirebaseHelper.currentUser() == null) return;
        String uid = FirebaseHelper.currentUser().getUid();

        binding.progressBar.setVisibility(View.VISIBLE);

        // Load Welcome Name
        FirebaseHelper.db().collection(FirebaseHelper.COL_USERS).document(uid).get()
            .addOnSuccessListener(snap -> {
                if (binding == null) return;
                String name = snap.getString("name");
                if (name != null) binding.tvWelcome.setText("Halo, " + name.split(" ")[0] + " 👋");
            });

        // Count vehicles
        FirebaseHelper.db().collection(FirebaseHelper.COL_VEHICLES)
            .whereEqualTo("ownerId", uid).get()
            .addOnSuccessListener(snap -> {
                if (binding == null) return;
                binding.tvTotalVehicles.setText(String.valueOf(snap.size()));
            });

        // Count pending bookings
        FirebaseHelper.db().collection(FirebaseHelper.COL_BOOKINGS)
            .whereEqualTo("ownerId", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener(snap -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.tvNewRequests.setText(String.valueOf(snap.size()));
            })
            .addOnFailureListener(e -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
