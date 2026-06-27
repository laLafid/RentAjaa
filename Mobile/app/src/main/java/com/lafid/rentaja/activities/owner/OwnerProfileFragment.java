package com.lafid.rentaja.activities.owner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lafid.rentaja.R;
import com.lafid.rentaja.databinding.FragmentOwnerProfileBinding;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.LocaleHelper;
import com.lafid.rentaja.utils.ThemeHelper;

public class OwnerProfileFragment extends Fragment {
    private FragmentOwnerProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentOwnerProfileBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        loadOwnerData();
        setupClickListeners();
        updateThemeUI();
    }

    private void loadOwnerData() {
        if (FirebaseHelper.currentUser() == null) return;
        String uid = FirebaseHelper.currentUser().getUid();

        // Set email dari Auth sebagai fallback
        binding.tvEmail.setText(FirebaseHelper.currentUser().getEmail());

        // Ambil Nama dari Firestore (Database)
        FirebaseHelper.db().collection(FirebaseHelper.COL_USERS).document(uid).get()
            .addOnSuccessListener(snap -> {
                if (binding == null) return;
                String name = snap.getString("name");
                if (name != null) binding.tvName.setText(name);
            });

        // Set Bahasa saat ini
        String lang = LocaleHelper.getLanguage(requireContext()).equals("in") ? "Bahasa Indonesia" : "English";
        binding.tvCurrentLanguage.setText(lang);
    }

    private void updateThemeUI() {
        if (binding == null) return;
        int currentTheme = ThemeHelper.getSavedTheme(requireContext());
        String[] themes = {getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system)};
        binding.tvCurrentTheme.setText(themes[currentTheme]);
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
        
        binding.btnTheme.setOnClickListener(v -> showThemeDialog());
        
        binding.btnLanguage.setOnClickListener(v -> showLanguageDialog());

        binding.btnBusinessInfo.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Fitur Informasi Bisnis segera hadir", Toast.LENGTH_SHORT).show());

        binding.btnWithdraw.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Fitur Penarikan Saldo segera hadir", Toast.LENGTH_SHORT).show());
    }

    private void showThemeDialog() {
        String[] items = {getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system)};
        new AlertDialog.Builder(getContext())
            .setTitle(R.string.label_theme)
            .setSingleChoiceItems(items, ThemeHelper.getSavedTheme(requireContext()), (d, w) -> {
                ThemeHelper.saveTheme(requireContext(), w);
                updateThemeUI();
                d.dismiss();
                restartActivity();
            }).show();
    }

    private void showLanguageDialog() {
        String[] l = {"Bahasa Indonesia", "English"};
        String[] codes = {"in", "en"};
        int currentIdx = LocaleHelper.getLanguage(requireContext()).equals("in") ? 0 : 1;

        new AlertDialog.Builder(getContext())
            .setTitle("Pilih Bahasa")
            .setSingleChoiceItems(l, currentIdx, (d, w) -> {
                LocaleHelper.setLocale(requireContext(), codes[w]);
                d.dismiss();
                restartActivity();
            }).show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
            .setTitle("Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun Pemilik?")
            .setPositiveButton("Ya, Keluar", (d, w) -> ((OwnerMainActivity) requireActivity()).logout())
            .setNegativeButton("Batal", null)
            .show();
    }

    private void restartActivity() {
        Intent intent = new Intent(requireContext(), OwnerMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        requireActivity().finish();
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
