package com.lafid.rentaja.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lafid.rentaja.R;
import com.lafid.rentaja.databinding.ItemCategoryBinding;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    private final List<String> categories;
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public CategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String category, boolean isSelected) {
            binding.tvCategoryName.setText(category);
            
            if (isSelected) {
                binding.cardCategory.setCardBackgroundColor(binding.getRoot().getContext().getColor(R.color.white));
                binding.tvCategoryName.setTextColor(binding.getRoot().getContext().getColor(R.color.primary));
            } else {
                binding.cardCategory.setCardBackgroundColor(binding.getRoot().getContext().getColor(R.color.primary_light));
                binding.tvCategoryName.setTextColor(binding.getRoot().getContext().getColor(R.color.primary));
            }

            binding.getRoot().setOnClickListener(v -> {
                int oldPosition = selectedPosition;
                selectedPosition = getAdapterPosition();
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                listener.onCategoryClick(category);
            });
        }
    }
}
