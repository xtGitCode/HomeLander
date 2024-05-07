package com.example.safetyapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.safetyapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddContacts extends AppCompatActivity {

    // UI elements
    private TextInputEditText nameTxt, phoneNumTxt;
    private Button addNum;

    // Data
    private String name, phoneNum;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        // Initialize UI elements
        initializeViews();

        // Set click listener for "Add Emergency Contact" button
        addNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEmergencyContactClick();
            }
        });
    }

    private void initializeViews() {
        nameTxt = findViewById(R.id.emergencyName);
        phoneNumTxt = findViewById(R.id.emergencyNum);
        addNum = findViewById(R.id.btn_addEm);
    }

    private void handleAddEmergencyContactClick() {
        name = nameTxt.getText().toString();
        phoneNum = phoneNumTxt.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phoneNum)) {
            // Show a toast message if name or phone number is empty
            Toast.makeText(AddContacts.this, "Enter name and contact number", Toast.LENGTH_SHORT).show();
        } else {
            // Add the contact to the database
            addContact(name, phoneNum);
        }
    }

    private void addContact(String name, String phoneNum) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String userUid = firebaseUser.getUid();

        // Generate a unique identifier for the contact
        String contactUid = databaseReference.child("contacts").child(userUid).push().getKey();

        // Store contacts to the database under the new structure
        DatabaseReference contactsRef = databaseReference.child("contacts").child(userUid).child(contactUid);
        contactsRef.child("contactName").setValue(name);
        contactsRef.child("contactNum").setValue(phoneNum);

        // Show a toast message indicating that the contact was added successfully
        Toast.makeText(AddContacts.this, "Contact added successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to the ContactsList activity
        startActivity(new Intent(AddContacts.this, ContactsList.class));
        finish(); // Optional: Close the current activity to prevent the user from navigating back
    }
}
