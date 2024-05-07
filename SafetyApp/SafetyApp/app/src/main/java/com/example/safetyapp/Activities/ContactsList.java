package com.example.safetyapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.safetyapp.Adapters.ContactAdapter;
import com.example.safetyapp.Models.Contacts;
import com.example.safetyapp.R;
import com.example.safetyapp.Utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ContactsList extends AppCompatActivity {

    // UI elements
    private Button createContact, home;
    private RecyclerView recyclerView;

    // Data
    private List<Contacts> contactsList;
    private ContactAdapter mAdapter;
    private FirebaseUser firebaseUser;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Initialize UI elements
        initializeUI();

        // Set up RecyclerView
        setupRecyclerView();

        // Set click listener for "Add Contacts" button
        createContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactsList.this, AddContacts.class));
            }
        });

        // Set click listener for "Go Home" button
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGoHomeClick();
            }
        });

        // Display contacts
        displayContacts();
    }

    private void initializeUI() {
        createContact = findViewById(R.id.btn_addContacts);
        createContact.setPaintFlags(createContact.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        home = findViewById(R.id.btn_goHome);
        home.setPaintFlags(home.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        firebaseHelper = new FirebaseHelper();
        contactsList = new ArrayList<>();
        mAdapter = new ContactAdapter(ContactsList.this, contactsList);
        recyclerView.setAdapter(mAdapter);
    }

    private void setupRecyclerView() {
        // Initialize contactsList and mAdapter
        mAdapter = new ContactAdapter(ContactsList.this, contactsList);
        recyclerView.setAdapter(mAdapter);
    }

    private void handleGoHomeClick() {
        // Retrieve and store emergency contacts from Firebase
        firebaseHelper.getEmergencyContacts(firebaseUser.getUid(), new FirebaseHelper.EmergencyContactsCallback() {
            @Override
            public void onEmergencyContactsReceived(List<Contacts> emergencyContacts) {
                // Store the new emergency contacts data in SharedPreferences
                updateEmergencyContactsInSharedPreferences(emergencyContacts);
            }

            @Override
            public void onEmergencyContactsError(String errorMessage) {
                // Handle error if needed
            }
        });

        startActivity(new Intent(ContactsList.this, MainActivity.class));
    }

    private void updateEmergencyContactsInSharedPreferences(List<Contacts> emergencyContacts) {
        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Remove the specific key ("emergencyContacts") from SharedPreferences
        editor.remove("emergencyContacts");

        // Convert the list of emergency contacts to a string and store it in SharedPreferences
        StringBuilder contactsStringBuilder = new StringBuilder();
        for (Contacts contact : emergencyContacts) {
            contactsStringBuilder.append(contact.getContactName()).append(":").append(contact.getContactNum()).append(",");
        }

        editor.putString("emergencyContacts", contactsStringBuilder.toString());
        editor.apply();
    }

    private void displayContacts() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("contacts").child(firebaseUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactsList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String uid = userSnapshot.getKey();
                    String contactName = userSnapshot.child("contactName").getValue(String.class);
                    String contactNum = userSnapshot.child("contactNum").getValue(String.class);

                    Contacts contacts = new Contacts(uid, contactName, contactNum);
                    contactsList.add(contacts);
                }

                // Notify the adapter that the data has changed
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }
}
