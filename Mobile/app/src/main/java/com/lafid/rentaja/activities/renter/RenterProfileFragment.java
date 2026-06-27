package com.lafid.rentaja.activities.renter;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseUser;
import com.lafid.rentaja.R;
import com.lafid.rentaja.activities.owner.OwnerMainActivity;
import com.lafid.rentaja.databinding.FragmentRenterProfileBinding;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.LocaleHelper;
import com.lafid.rentaja.utils.ThemeHelper;

public class RenterProfileFragment extends Fragment {
    private FragmentRenterProfileBinding binding;
    
    private static final String[] CATEGORIES = {
        "Semua", "Mobil", "Motor", "Bus", "Truk", "Kapal", "Alutsista"
    };
    
    public static String selectedCategory = "Semua";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentRenterProfileBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserData();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseHelper.currentUser();
        if (user != null) {
            // Set email from Firebase Auth directly
            binding.tvEmail.setText(user.getEmail());

            FirebaseHelper.db().collection(FirebaseHelper.COL_USERS)
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isAdded() && binding != null) {
                            String name = documentSnapshot.getString("name");
                            String photo = documentSnapshot.getString("photoUrl");
                            binding.tvName.setText(name != null ? name : user.getDisplayName());
                            if (photo != null && photo.length() > 100) {
                                decodeAndSetImage(photo);
                            } else if (user.getPhotoUrl() != null) {
                                binding.ivProfile.setPadding(0, 0, 0, 0);
                                binding.ivProfile.setImageTintList(null);
                                Glide.with(this).load(user.getPhotoUrl()).into(binding.ivProfile);
                            } else {
                                setPlaceholderImage();
                            }
                        }
                    });
        }

        binding.tvCurrentLanguage.setText(LocaleHelper.getLanguage(requireContext()).equals("in") ? "Bahasa Indonesia" : "English");
        binding.tvActiveCategory.setText(selectedCategory);
        
        // Load Theme Settings
        updateThemeUI();
    }

    private void updateThemeUI() {
        if (!isAdded() || binding == null) return;

        // Theme Text
        int currentTheme = ThemeHelper.getSavedTheme(requireContext());
        String[] themes = {getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system)};
        binding.tvCurrentTheme.setText(themes[currentTheme]);

        // Accent Color Text
        int currentAccent = ThemeHelper.getAccentColor(requireContext());
        String[] accents = {getString(R.string.accent_green), getString(R.string.accent_blue), getString(R.string.accent_red), getString(R.string.accent_orange)};
        binding.tvCurrentAccent.setText(accents[currentAccent]);
        
        // Disable accent color selection if dynamic color is enabled
        binding.btnAccentColor.setEnabled(!ThemeHelper.isDynamicColorEnabled(requireContext()));
        binding.btnAccentColor.setAlpha(ThemeHelper.isDynamicColorEnabled(requireContext()) ? 0.5f : 1.0f);
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
        binding.btnFilterCategory.setOnClickListener(v -> showCategoryDialog());
        binding.btnLanguage.setOnClickListener(v -> showLanguageDialog());
        
        // Theme Settings Listeners
        binding.btnTheme.setOnClickListener(v -> showThemeDialog());
        binding.btnAccentColor.setOnClickListener(v -> showAccentDialog());


        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Keluar")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya", (d, w) -> {
                    if (requireActivity() instanceof RenterMainActivity) {
                        ((RenterMainActivity) requireActivity()).logout();
                    } else if (requireActivity() instanceof OwnerMainActivity) {
                        ((OwnerMainActivity) requireActivity()).logout();
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
        });
    }

    private void showThemeDialog() {
        String[] items = {getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system)};
        new AlertDialog.Builder(getContext()).setTitle(R.string.label_theme).setSingleChoiceItems(items, ThemeHelper.getSavedTheme(requireContext()), (d, w) -> {
            ThemeHelper.saveTheme(requireContext(), w);
            updateThemeUI();
            d.dismiss();
            showRestartDialog();
        }).show();
    }

    private void showAccentDialog() {
        String[] items = {getString(R.string.accent_green), getString(R.string.accent_blue), getString(R.string.accent_red), getString(R.string.accent_orange)};
        new AlertDialog.Builder(getContext()).setTitle(R.string.label_accent_color).setSingleChoiceItems(items, ThemeHelper.getAccentColor(requireContext()), (d, w) -> {
            ThemeHelper.setAccentColor(requireContext(), w);
            updateThemeUI();
            d.dismiss();
            showRestartDialog();
        }).show();
    }

    private void showRestartDialog() {
        new AlertDialog.Builder(getContext())
            .setTitle("Perlu Restart")
            .setMessage("Beberapa pengaturan tema memerlukan restart aplikasi untuk diterapkan.")
            .setPositiveButton("Restart Sekarang", (d, w) -> restartActivity())
            .setNegativeButton("Nanti", null)
            .show();
    }

    private void restartActivity() {
        if (!isAdded()) return;
        if (requireActivity() instanceof RenterMainActivity) {
            ((RenterMainActivity) requireActivity()).restartActivity();
        } else if (requireActivity() instanceof OwnerMainActivity) {
            // Re-triggering the activity restart for Owner side
            Intent intent = new Intent(requireContext(), OwnerMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            requireActivity().finish();
            startActivity(intent);
        } else {
            requireActivity().recreate();
        }
    }

    private void decodeAndSetImage(String b64) {
        try {
            byte[] b = Base64.decode(b64, Base64.DEFAULT);
            Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);
            binding.ivProfile.setPadding(0, 0, 0, 0);
            binding.ivProfile.setImageTintList(null);
            binding.ivProfile.setImageBitmap(bm);
        } catch (Exception e) { setPlaceholderImage(); }
    }

    private void setPlaceholderImage() {
        if (binding == null) return;
        int p = getResources().getDimensionPixelSize(R.dimen.profile_image_padding);
        binding.ivProfile.setPadding(p, p, p, p);
        binding.ivProfile.setImageResource(R.drawable.ic_person);

        // BENAR-BENAR diperbaiki: Menggunakan R.color.primary yang ada di colors.xml Anda
        int color = ContextCompat.getColor(requireContext(), R.color.primary);
        binding.ivProfile.setImageTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void showCategoryDialog() {
        int idx = 0;
        for (int i = 0; i < CATEGORIES.length; i++) if (CATEGORIES[i].equals(selectedCategory)) idx = i;
        new AlertDialog.Builder(getContext()).setTitle("Kategori Default").setSingleChoiceItems(CATEGORIES, idx, (d, w) -> {
            selectedCategory = CATEGORIES[w];
            binding.tvActiveCategory.setText(selectedCategory);
            d.dismiss();
        }).show();
    }

    private void showLanguageDialog() {
        String[] l = {"Bahasa Indonesia", "English"}; String[] c = {"in", "en"};
        int idx = LocaleHelper.getLanguage(requireContext()).equals("in") ? 0 : 1;
        new AlertDialog.Builder(getContext()).setTitle("Pilih Bahasa").setSingleChoiceItems(l, idx, (d, w) -> {
            LocaleHelper.setLocale(requireContext(), c[w]);
            d.dismiss();
            restartActivity();
        }).show();
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
