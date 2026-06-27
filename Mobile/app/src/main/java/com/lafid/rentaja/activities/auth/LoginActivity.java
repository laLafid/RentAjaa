package com.lafid.rentaja.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.lafid.rentaja.R;
import com.lafid.rentaja.activities.owner.OwnerMainActivity;
import com.lafid.rentaja.activities.renter.RenterMainActivity;
import com.lafid.rentaja.databinding.ActivityLoginBinding;
import com.lafid.rentaja.utils.FirebaseHelper;
import com.lafid.rentaja.utils.ThemeHelper;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupTabs();
        setupClickListeners();
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.loginForm.setVisibility(View.VISIBLE);
                    binding.registerForm.setVisibility(View.GONE);
                } else {
                    binding.loginForm.setVisibility(View.GONE);
                    binding.registerForm.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> doLogin());
        binding.btnRegister.setOnClickListener(v -> {
            String role = binding.btnRoleOwner.isChecked() ? "owner" : "renter";
            doRegister(role);
        });
    }

    private void doLogin() {
        String email = binding.etLoginEmail.getText().toString().trim();
        String pass  = binding.etLoginPass.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_login), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);
        FirebaseHelper.auth()
            .signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();
                FirebaseHelper.db()
                    .collection(FirebaseHelper.COL_USERS)
                    .document(uid)
                    .get()
                    .addOnSuccessListener(this::routeByRole)
                    .addOnFailureListener(e -> routeByRole(null));
            })
            .addOnFailureListener(e -> {
                binding.btnLogin.setEnabled(true);
                Toast.makeText(this, getString(R.string.msg_login_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            });
    }

    private void doRegister(String role) {
        String name  = binding.etRegName.getText().toString().trim();
        String email = binding.etRegEmail.getText().toString().trim();
        String pass  = binding.etRegPass.getText().toString();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnRegister.setEnabled(false);
        FirebaseHelper.auth()
            .createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();
                java.util.Map<String, Object> user = new java.util.HashMap<>();
                user.put("uid",   uid);
                user.put("name",  name);
                user.put("email", email);
                user.put("role",  role);
                user.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                FirebaseHelper.db()
                    .collection(FirebaseHelper.COL_USERS)
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show();
                        Intent intent = "owner".equals(role)
                                ? new Intent(this, OwnerMainActivity.class)
                                : new Intent(this, RenterMainActivity.class);
                        startActivity(intent);
                        finish();
                    });
            })
            .addOnFailureListener(e -> {
                binding.btnRegister.setEnabled(true);
                Toast.makeText(this, getString(R.string.msg_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            });
    }

    private void routeByRole(DocumentSnapshot snap) {
        String role = (snap != null && snap.exists()) ? snap.getString("role") : "renter";
        Intent intent = "owner".equals(role)
                ? new Intent(this, OwnerMainActivity.class)
                : new Intent(this, RenterMainActivity.class);
        startActivity(intent);
        finish();
    }
}
