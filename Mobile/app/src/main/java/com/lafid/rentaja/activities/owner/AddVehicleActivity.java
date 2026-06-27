package com.lafid.rentaja.activities.owner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.StorageReference;
import com.lafid.rentaja.databinding.ActivityAddVehicleBinding;
import com.lafid.rentaja.models.Vehicle;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.ImgbbHelper;
import com.lafid.rentaja.utils.ThemeHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddVehicleActivity extends AppCompatActivity {

    private ActivityAddVehicleBinding binding;
    private String editingVehicleId = null;
    private Uri imageUri = null;
    private String base64ImageToUpload = null;
    private String existingImageUrl = null;


    private static final String[] CATEGORIES = {
        "Mobil", "Motor", "Kapal", "Alutsista", "Truk", "Bus"
    };

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        processImageToBase64(selectedUri);
                    }
                }
            }
    );

    private void processImageToBase64(Uri uri) {
        try {
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            // Resize ke 640x480 (proporsi standar lanskap mobil agar tidak pecah & hemat data)
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 640, 480, true);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Kualitas 70% sudah sangat oke
            byte[] bytes = baos.toByteArray();

            // Ini string yang siap ditembak ke ImgBB
            base64ImageToUpload = Base64.encodeToString(bytes, Base64.DEFAULT);

            // Tampilkan preview ke layout
            binding.ivVehiclePreview.setVisibility(View.VISIBLE);
            binding.layoutPickHint.setVisibility(View.GONE);
            binding.ivVehiclePreview.setImageBitmap(resized);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memproses gambar kendaraan", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityAddVehicleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editingVehicleId = getIntent().getStringExtra("vehicleId");
        boolean isEdit = editingVehicleId != null;

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEdit ? "Edit Unit" : "Tambah Unit Baru");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.actvCategory.setAdapter(catAdapter);

        binding.cardPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        if (isEdit) {
            loadVehicleForEdit();
            binding.btnDelete.setVisibility(View.VISIBLE);
        }

        binding.btnSave.setText(isEdit ? "Simpan Perubahan" : "Posting Unit");
        binding.btnSave.setOnClickListener(v -> saveVehicle());

        binding.btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void executeSaveWorkflow(String name, String category, long price, long deposit, String desc, String specsRaw, String ownerName) {
        // KONDISI A: User memilih gambar baru, upload ke ImgBB terlebih dahulu
        if (base64ImageToUpload != null) {
            ImgbbHelper.uploadBase64(base64ImageToUpload, new ImgbbHelper.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    // Jalankan di UI Thread karena response dari callback berjalan di Background Thread
                    runOnUiThread(() -> saveToFirestore(name, category, price, deposit, desc, specsRaw, imageUrl, ownerName));
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(AddVehicleActivity.this, "Gagal upload gambar ke ImgBB: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
        // KONDISI B: User tidak mengganti gambar (hanya edit teks) atau gambar kosong
        else {
            saveToFirestore(name, category, price, deposit, desc, specsRaw, existingImageUrl, ownerName);
        }
    }

    private void loadVehicleForEdit() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.db().collection(FirebaseHelper.COL_VEHICLES)
            .document(editingVehicleId)
            .get()
            .addOnSuccessListener(snap -> {
                binding.progressBar.setVisibility(View.GONE);
                Vehicle v = snap.toObject(Vehicle.class);
                if (v == null) { finish(); return; }
                
                binding.etName.setText(v.getName());
                binding.actvCategory.setText(v.getCategory(), false);
                binding.etPrice.setText(String.valueOf(v.getPricePerDay()));
                binding.etDeposit.setText(String.valueOf(v.getDeposit()));
                binding.etDesc.setText(v.getDesc());
                if (v.getSpecs() != null)
                    binding.etSpecs.setText(String.join(", ", v.getSpecs()));
                
                existingImageUrl = v.getImageUrl();
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    binding.ivVehiclePreview.setVisibility(View.VISIBLE);
                    binding.layoutPickHint.setVisibility(View.GONE);
                    Glide.with(this).load(existingImageUrl).into(binding.ivVehiclePreview);
                }
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            });
    }

    private void saveVehicle() {
        String name     = binding.etName.getText().toString().trim();
        String category = binding.actvCategory.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        String depStr   = binding.etDeposit.getText().toString().trim();
        String desc     = binding.etDesc.getText().toString().trim();
        String specsRaw = binding.etSpecs.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty() || depStr.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua field bertanda bintang!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        // Ambil nama pemilik dari Firestore
        FirebaseHelper.db().collection(FirebaseHelper.COL_USERS)
                .document(FirebaseHelper.currentUser().getUid())
                .get()
                .addOnSuccessListener(userSnap -> {
                    String ownerName = userSnap.getString("name");
                    if (ownerName == null) ownerName = FirebaseHelper.currentUser().getEmail();

                    // Eksekusi upload atau langsung simpan
                    executeSaveWorkflow(name, category, Long.parseLong(priceStr), Long.parseLong(depStr), desc, specsRaw, ownerName);
                })
                .addOnFailureListener(e -> {
                    String ownerName = FirebaseHelper.currentUser().getDisplayName();
                    if (ownerName == null) ownerName = "Owner";

                    executeSaveWorkflow(name, category, Long.parseLong(priceStr), Long.parseLong(depStr), desc, specsRaw, ownerName);
                });
    }

    private void uploadImageAndSave(String name, String category, long price, long deposit, String desc, String specsRaw, String ownerName) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = FirebaseHelper.storage().getReference().child("vehicles/" + fileName);

        ref.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                saveToFirestore(name, category, price, deposit, desc, specsRaw, uri.toString(), ownerName);
            }))
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(this, "Gagal upload gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void saveToFirestore(String name, String category, long price, long deposit, String desc, String specsRaw, String imageUrl, String ownerName) {
        List<String> specs = specsRaw.isEmpty()
            ? List.of()
            : Arrays.asList(specsRaw.split("\\s*,\\s*"));

        Map<String, Object> data = new HashMap<>();
        data.put("name",        name);
        data.put("category",    category);
        data.put("pricePerDay", price);
        data.put("deposit",     deposit);
        data.put("desc",        desc);
        data.put("specs",       specs);
        data.put("imageUrl",    imageUrl);
        data.put("ownerId",     FirebaseHelper.currentUser().getUid());
        data.put("ownerName",   ownerName);
        data.put("status",      "available");

        if (editingVehicleId != null) {
            FirebaseHelper.db().collection(FirebaseHelper.COL_VEHICLES)
                .document(editingVehicleId)
                .update(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Unit berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> handleError(e));
        } else {
            data.put("createdAt", FieldValue.serverTimestamp());
            FirebaseHelper.db().collection(FirebaseHelper.COL_VEHICLES)
                .add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Unit berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> handleError(e));
        }
    }

    private void handleError(Exception e) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSave.setEnabled(true);
        Toast.makeText(this, "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void confirmDelete() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Unit")
            .setMessage("Apakah Anda yakin ingin menghapus unit ini secara permanen?")
            .setPositiveButton("Hapus", (d, w) -> deleteVehicle())
            .setNegativeButton("Batal", null)
            .show();
    }

    private void deleteVehicle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.db().collection(FirebaseHelper.COL_VEHICLES)
            .document(editingVehicleId)
            .delete()
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "Unit telah dihapus.", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Gagal menghapus unit", Toast.LENGTH_SHORT).show();
            });
    }
}
