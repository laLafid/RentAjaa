package com.lafid.rentaja.activities.renter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.lafid.rentaja.R;
import com.lafid.rentaja.databinding.ActivityEditProfileBinding;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.ThemeHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private String base64Image = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupImagePicker();
        loadUserData();
        setupClickListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processImage(uri);
                    }
                }
        );
    }

    private void processImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            
            // Resize gambar agar hemat memori & tidak melebihi limit Firestore (1MB)
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 480, 480, true);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();
            
            base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

            // Tampilkan preview foto
            binding.ivProfile.setPadding(0, 0, 0, 0);
            binding.ivProfile.setImageTintList(null);
            binding.ivProfile.setImageBitmap(resized);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseHelper.currentUser();
        if (user != null) {
            binding.etEmail.setText(user.getEmail());
            
            // Ambil data terbaru dari Firestore (Nama & Foto Base64)
            FirebaseHelper.db().collection(FirebaseHelper.COL_USERS)
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String name = documentSnapshot.getString("name");
                        String photo = documentSnapshot.getString("photoUrl");
                        
                        binding.etName.setText(name != null ? name : user.getDisplayName());
                        
                        if (photo != null && photo.length() > 100) {
                            decodeAndSetImage(photo);
                        } else if (user.getPhotoUrl() != null) {
                            binding.ivProfile.setPadding(0, 0, 0, 0);
                            binding.ivProfile.setImageTintList(null);
                            Glide.with(this).load(user.getPhotoUrl()).into(binding.ivProfile);
                        }
                    });
        }
    }

    private void decodeAndSetImage(String base64Str) {
        try {
            byte[] decodedString = Base64.decode(base64Str, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            binding.ivProfile.setPadding(0, 0, 0, 0);
            binding.ivProfile.setImageTintList(null);
            binding.ivProfile.setImageBitmap(decodedByte);
        } catch (Exception ignored) {}
    }

    private void setupClickListeners() {
        binding.btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.ivProfile.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String newName = binding.etName.getText().toString().trim();
        if (newName.isEmpty()) {
            binding.etName.setError("Nama tidak boleh kosong");
            return;
        }

        setLoading(true);
        FirebaseUser user = FirebaseHelper.currentUser();
        if (user == null) return;

        // Update di Auth (untuk Session)
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update di Firestore (Database Utama)
                updateFirestore(user.getUid(), newName, base64Image);
            } else {
                setLoading(false);
                Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.btnSave.setEnabled(!isLoading);
        binding.btnSave.setText(isLoading ? "Menyimpan..." : "Simpan");
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void updateFirestore(String uid, String name, String photoBase64) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        if (photoBase64 != null) {
            updates.put("photoUrl", photoBase64);
        }

        FirebaseHelper.db().collection(FirebaseHelper.COL_USERS)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Gagal simpan database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
