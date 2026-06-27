package com.lafid.rentaja.activities.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.lafid.rentaja.adapters.VehicleAdapter;
import com.lafid.rentaja.databinding.FragmentOwnerVehiclesBinding;
import com.lafid.rentaja.models.Vehicle;
import com.lafid.rentaja.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class OwnerVehiclesFragment extends Fragment {
    private FragmentOwnerVehiclesBinding binding;
    private VehicleAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentOwnerVehiclesBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new VehicleAdapter(new ArrayList<>(), vehicle -> {
            // Click → edit vehicle
            Intent intent = new Intent(getContext(), AddVehicleActivity.class);
            intent.putExtra("vehicleId", vehicle.getId());
            startActivity(intent);
        });
        binding.rvVehicles.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvVehicles.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v ->
            startActivity(new Intent(getContext(), AddVehicleActivity.class)));

        loadVehicles();
    }

    public void loadVehicles() {
        if (FirebaseHelper.currentUser() == null) return;
        String uid = FirebaseHelper.currentUser().getUid();
        if (binding != null) binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.db().collection(FirebaseHelper.COL_VEHICLES)
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener(snap -> {
                if (binding == null) return;
                List<Vehicle> list = new ArrayList<>();
                for (var doc : snap.getDocuments()) {
                    Vehicle v = doc.toObject(Vehicle.class);
                    if (v != null) { v.setId(doc.getId()); list.add(v); }
                }
                adapter.updateList(list);
                binding.progressBar.setVisibility(View.GONE);
                binding.emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            });
    }

    @Override public void onResume() { super.onResume(); loadVehicles(); }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
