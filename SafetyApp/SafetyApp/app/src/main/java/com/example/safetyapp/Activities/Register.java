package com.example.safetyapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.safetyapp.R;
import com.example.safetyapp.Utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class Register extends AppCompatActivity {

    // UI elements
    TextInputEditText editTextPhone, editTextPassword, editTextUsername, editTextEmail;
    Button buttonRegister, buttonGoToLogin;

    // Firebase helper for user registration
    FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI elements
        editTextPhone = findViewById(R.id.phone);
        editTextPassword = findViewById(R.id.password);
        editTextUsername = findViewById(R.id.username);
        editTextEmail = findViewById(R.id.email);
        buttonRegister = findViewById(R.id.btn_register);
        buttonGoToLogin = findViewById(R.id.btn_goToLog);

        // Initialize FirebaseHelper
        firebaseHelper = new FirebaseHelper();

        // Add underline to the registration button
        buttonRegister.setPaintFlags(buttonRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Set onClickListeners for buttons
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        buttonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    // Method to handle user registration
    void registerUser() {
        String phone, password, username, email;
        phone = String.valueOf(editTextPhone.getText());
        password = String.valueOf(editTextPassword.getText());
        username = String.valueOf(editTextUsername.getText());
        email = String.valueOf(editTextEmail.getText());

        firebaseHelper.registerUser(email, password, username, phone, new FirebaseHelper.RegistrationCallback() {
            @Override
            public void onSuccess() {
                // Registration successful
                Toast.makeText(Register.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Registration failed
                Toast.makeText(Register.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Method to navigate to the login screen
    void navigateToLogin() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
    }
}
