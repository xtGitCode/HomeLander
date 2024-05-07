package com.example.safetyapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safetyapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private TextView usernameTxt;
    private Button logout, contacts;
    private ImageButton alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the user is logged in
        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        String username = preferences.getString("username", "");

        if (TextUtils.isEmpty(username)) {
            // User is not logged in, redirect to login activity
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        } else {
            // User is logged in, initialize UI elements
            initializeViews();
            usernameTxt.setText(username);
        }

        // Set up onClickListeners
        setupOnClickListeners();
    }

    private void initializeViews() {
        usernameTxt = findViewById(R.id.usernameDisplay);
        logout = findViewById(R.id.btn_logout);
        alert = findViewById(R.id.btn_alert);
        contacts = findViewById(R.id.btn_contacts);
        contacts.setPaintFlags(contacts.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void setupOnClickListeners() {
        // Logout Button Click Listener
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sign out the user
                FirebaseAuth.getInstance().signOut();

                // Redirect to the login screen or another appropriate activity
                startActivity(new Intent(MainActivity.this, Login.class));
                finish(); // Optional: Close the current activity to prevent the user from navigating back
            }
        });

        // Contacts Button Click Listener
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ContactsList.class);
                startActivity(intent);
            }
        });

        // Alert Button Click Listener
        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AlertMode.class);
                startActivity(intent);
            }
        });
    }
}
