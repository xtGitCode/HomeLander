package com.example.safetyapp.Utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.safetyapp.Models.Contacts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;

    public FirebaseHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public interface RegistrationCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public interface LoginCallback {
        void onSuccess(String uid, String email, String password, String phone, String username);

        void onFailure(String errorMessage);
    }

    public interface EmergencyContactsCallback {
        void onEmergencyContactsReceived(List<Contacts> emergencyContacts);

        void onEmergencyContactsError(String errorMessage);
    }

    public void registerUser(String email, String password, String username, String phone,
                             RegistrationCallback callback) {
        // Check if any of the fields are empty
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username) || TextUtils.isEmpty(phone)) {
            callback.onFailure("All fields are required");
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            String userUid = firebaseUser.getUid();

                            // Store additional fields to the database
                            DatabaseReference userRef = databaseReference.child("users").child(userUid);
                            userRef.child("username").setValue(username);
                            userRef.child("email").setValue(email);
                            userRef.child("password").setValue(password);
                            userRef.child("phone").setValue(phone);

                            callback.onSuccess();
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void loginUser(String phone, String password, LoginCallback callback) {
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if any user has the given phone number
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String getPhone = userSnapshot.child("phone").getValue(String.class);

                    if (getPhone != null && getPhone.equals(phone)) {
                        String getPassword = userSnapshot.child("password").getValue(String.class);
                        String getEmail = userSnapshot.child("email").getValue(String.class);
                        String getUsername = userSnapshot.child("username").getValue(String.class);
                        String getUid = userSnapshot.getKey();

                        if (getPassword != null && getPassword.equals(password)) {
                            // Password matches
                            // Sign in the user with Firebase Authentication
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(getEmail, password);
                            callback.onSuccess(getUid, getEmail, getPassword, getPhone, getUsername);
                        } else {
                            callback.onFailure("Wrong Password");
                        }
                        return; // Break out of the loop since we found a matching phone number
                    }
                }

                // If we reach this point, the phone number is not registered
                callback.onFailure("Phone number not registered");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Method to get emergency contacts for a specific user
    public void getEmergencyContacts(String userId, EmergencyContactsCallback callback) {
        DatabaseReference contactsReference = databaseReference.child("contacts").child(userId);
        contactsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Contacts> emergencyContacts = new ArrayList<>();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    String uid = snapshot.getKey();
                    String contactName = contactSnapshot.child("contactName").getValue(String.class);
                    String contactNum = contactSnapshot.child("contactNum").getValue(String.class);
                    emergencyContacts.add(new Contacts(uid, contactName, contactNum));
                }
                callback.onEmergencyContactsReceived(emergencyContacts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onEmergencyContactsError(error.getMessage());
            }
        });
    }

}

