package com.lafid.rentaja.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lafid.rentaja.R;
import com.lafid.rentaja.databinding.ItemVehicleBinding;
import com.lafid.rentaja.models.Vehicle;
import com.lafid.rentaja.utils.FormatUtils;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
    }

    private List<Vehicle> vehicles;
    private final OnVehicleClickListener listener;

    public VehicleAdapter(List<Vehicle> vehicles, OnVehicleClickListener listener) {
        this.vehicles = vehicles;
        this.listener = listener;
    }

    public void updateList(List<Vehicle> newList) {
        this.vehicles = newList;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleBinding binding = ItemVehicleBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new VehicleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        holder.bind(vehicles.get(position));
    }

    @Override
    public int getItemCount() { return vehicles.size(); }

    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private final ItemVehicleBinding binding;

        VehicleViewHolder(ItemVehicleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Vehicle v) {
            binding.tvName.setText(v.getName());
            binding.tvCategory.setText(v.getCategory());
            
            // Image handling
            if (v.getImageUrl() != null && !v.getImageUrl().isEmpty()) {
                binding.ivVehicle.setVisibility(View.VISIBLE);
                binding.tvEmoji.setVisibility(View.GONE);
                Glide.with(itemView.getContext())
                    .load(v.getImageUrl())
                    .centerCrop()
                    .placeholder(R.color.bg_grey)
                    .into(binding.ivVehicle);
            } else {
                binding.ivVehicle.setVisibility(View.GONE);
                binding.tvEmoji.setVisibility(View.VISIBLE);
                binding.tvEmoji.setText(v.getCategoryEmoji());
            }
            
            // Set price
            binding.tvPrice.setText(FormatUtils.formatRupiah(v.getPricePerDay()));
            
            // Set deposit using string resource "Deposit %1$s"
            String depositText = itemView.getContext().getString(
                    R.string.label_deposit_small, 
                    FormatUtils.formatRupiah(v.getDeposit()));
            binding.tvDeposit.setText(depositText);

            // Status chip
            boolean available = v.isAvailable();
            binding.chipStatus.setText(available ? "Tersedia" : "Disewa");
            binding.chipStatus.setChipBackgroundColorResource(
                available ? R.color.chip_available_bg
                          : R.color.chip_rented_bg);

            binding.getRoot().setOnClickListener(view -> {
                if (listener != null) listener.onVehicleClick(v);
            });
        }
    }
}
