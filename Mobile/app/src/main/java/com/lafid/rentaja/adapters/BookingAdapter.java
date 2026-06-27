package com.lafid.rentaja.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lafid.rentaja.R;
import com.lafid.rentaja.databinding.ItemBookingBinding;
import com.lafid.rentaja.models.Booking;
import com.lafid.rentaja.utils.FormatUtils;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnContractClickListener { void onClick(Booking booking); }
    public interface OnActionClickListener   { void onClick(Booking booking); }

    private List<Booking> bookings;
    private final OnContractClickListener contractListener;
    private final OnActionClickListener   actionListener;

    public BookingAdapter(List<Booking> bookings,
                          OnContractClickListener contractListener,
                          OnActionClickListener   actionListener) {
        this.bookings         = bookings;
        this.contractListener = contractListener;
        this.actionListener   = actionListener;
    }

    public void updateList(List<Booking> newList) {
        this.bookings = newList;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingBinding binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() { return bookings.size(); }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookingBinding binding;

        BookingViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Booking b) {
            binding.tvVehicleName.setText(b.getVehicleName());
            
            // Perbaikan: Jika actionListener null (berarti ini di sisi penyewa), tampilkan nama Pemilik.
            // Jika actionListener ada (sisi owner), tampilkan nama Penyewa.
            if (actionListener == null) {
                binding.tvRenterName.setText("Pemilik: " + b.getOwnerName());
            } else {
                binding.tvRenterName.setText("Penyewa: " + b.getRenterName());
            }

            binding.tvBookingId.setText("#" + (b.getId() != null && b.getId().length() > 8 ? b.getId().substring(0, 8).toUpperCase() : "ID-NEW"));
            
            binding.tvDates.setText(FormatUtils.formatDate(b.getStartDate()) + " - " + FormatUtils.formatDate(b.getEndDate()));
            binding.tvTotal.setText(FormatUtils.formatRupiah(b.getTotal()));

            // Status Badge Logic
            setupStatusBadge(b);

            // Button Logic for Owner/Renter
            setupActionButtons(b);
        }

        private void setupStatusBadge(Booking b) {
            String status = b.getStatus() != null ? b.getStatus() : "pending";
            binding.chipStatus.setText(b.getStatusLabel());
            
            int color;
            switch (status) {
                case "approved":  color = R.color.status_approved; break;
                case "rejected":  color = R.color.status_rejected; break;
                case "active":    color = R.color.status_active;   break;
                case "completed": color = R.color.status_completed;break;
                default:          color = R.color.status_pending;  break;
            }
            binding.chipStatus.setChipBackgroundColorResource(color);
        }

        private void setupActionButtons(Booking b) {
            // Tombol Kontrak (Hanya jika disetujui/aktif)
            if (contractListener != null && (b.isApproved() || b.isActive() || b.isCompleted())) {
                binding.btnContract.setVisibility(View.VISIBLE);
                binding.btnContract.setOnClickListener(v -> contractListener.onClick(b));
            } else {
                binding.btnContract.setVisibility(View.GONE);
            }

            // Tombol Aksi Owner
            if (actionListener != null) {
                if (b.isPending() || b.isApproved() || b.isActive()) {
                    binding.btnAction.setVisibility(View.VISIBLE);
                    binding.btnAction.setText("Kelola"); // Mengganti karakter Sunda/Aksara ke teks yang lebih umum atau sesuai konteks
                    binding.btnAction.setOnClickListener(v -> actionListener.onClick(b));
                } else {
                    binding.btnAction.setVisibility(View.GONE);
                }
            } else {
                binding.btnAction.setVisibility(View.GONE);
            }
        }
    }
}
