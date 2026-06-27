package com.lafid.rentaja.activities.renter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lafid.rentaja.adapters.VehicleAdapter;
import com.lafid.rentaja.databinding.FragmentRenterHomeBinding;
import com.lafid.rentaja.models.Vehicle;
import com.lafid.rentaja.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class RenterHomeFragment extends Fragment {

    private FragmentRenterHomeBinding binding;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> allVehicles = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRenterHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupGreeting();
        setupVehicleList();
        setupSearch();
        setupSwipeRefresh();
        
        loadVehicles(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update section title based on global selected category
        String category = RenterProfileFragment.selectedCategory;
        binding.tvSectionTitle.setText(category.equals("Semua") ? "Semua Kendaraan" : category);
        if (!allVehicles.isEmpty()) {
            filterVehicles(binding.etSearch.getText().toString(), category);
        }
    }

    private void setupGreeting() {
        if (FirebaseHelper.currentUser() != null) {
            String name = FirebaseHelper.currentUser().getDisplayName();
            String firstName = (name != null && !name.isEmpty()) ? name.split(" ")[0] : "Kamu";
            binding.tvGreeting.setText("Halo, " + firstName + " 👋");
        }
    }

    private void setupVehicleList() {
        vehicleAdapter = new VehicleAdapter(new ArrayList<>(), vehicle -> {
            Intent intent = new Intent(getContext(), VehicleDetailActivity.class);
            intent.putExtra("vehicleId", vehicle.getId());
            startActivity(intent);
        });
        binding.rvVehicles.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvVehicles.setAdapter(vehicleAdapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVehicles(s.toString(), RenterProfileFragment.selectedCategory);
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadVehicles(false));
        binding.swipeRefresh.setColorSchemeResources(com.lafid.rentaja.R.color.primary);
    }

    private void loadVehicles(boolean showFullLoading) {
        if (showFullLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvVehicles.setVisibility(View.GONE);
        }

        FirebaseHelper.db()
            .collection(FirebaseHelper.COL_VEHICLES)
            .whereEqualTo("status", "available")
            .get()
            .addOnSuccessListener(snapshot -> {
                allVehicles.clear();
                for (var doc : snapshot.getDocuments()) {
                    Vehicle v = doc.toObject(Vehicle.class);
                    if (v != null) {
                        v.setId(doc.getId());
                        allVehicles.add(v);
                    }
                }
                
                if (binding == null) return;

                binding.progressBar.setVisibility(View.GONE);
                binding.rvVehicles.setVisibility(View.VISIBLE);
                binding.swipeRefresh.setRefreshing(false);
                
                filterVehicles(binding.etSearch.getText().toString(), RenterProfileFragment.selectedCategory);
            })
            .addOnFailureListener(e -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            });
    }

    private void filterVehicles(String query, String category) {
        List<Vehicle> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        
        for (Vehicle v : allVehicles) {
            boolean matchQ = lowerQuery.isEmpty() || v.getName().toLowerCase().contains(lowerQuery);
            boolean matchCat = category.equals("Semua") || category.equalsIgnoreCase(v.getCategory());
            if (matchQ && matchCat) {
                filtered.add(v);
            }
        }
        
        vehicleAdapter.updateList(filtered);
        binding.emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
