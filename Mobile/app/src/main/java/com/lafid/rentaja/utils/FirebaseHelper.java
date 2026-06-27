package com.lafid.rentaja.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Singleton helper — call FirebaseHelper.auth(), FirebaseHelper.db(), or FirebaseHelper.storage()
 * anywhere in the app instead of re-initializing every time.
 */
public class FirebaseHelper {

    private static FirebaseAuth     mAuth;
    private static FirebaseFirestore mDb;
    private static FirebaseStorage   mStorage;

    public static FirebaseAuth auth() {
        if (mAuth == null) mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }

    public static FirebaseFirestore db() {
        if (mDb == null) mDb = FirebaseFirestore.getInstance();
        return mDb;
    }

    public static FirebaseStorage storage() {
        if (mStorage == null) {
            mStorage = FirebaseStorage.getInstance("gs://managmn-app.firebasestorage.app");
        }
        return mStorage;
    }

    public static FirebaseUser currentUser() {
        return auth().getCurrentUser();
    }

    public static boolean isLoggedIn() {
        return currentUser() != null;
    }

    // Firestore collection names
    public static final String COL_USERS    = "users";
    public static final String COL_VEHICLES = "vehicles";
    public static final String COL_BOOKINGS = "bookings";
}
