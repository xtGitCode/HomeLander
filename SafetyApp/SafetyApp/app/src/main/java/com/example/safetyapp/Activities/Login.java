package com.example.safetyapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.safetyapp.Models.Contacts;
import com.example.safetyapp.R;
import com.example.safetyapp.Utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class Login extends AppCompatActivity {

    // UI elements
    Button buttonCreateAccount, buttonLogin;
    TextInputEditText phone, password;
    FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        buttonCreateAccount = findViewById(R.id.btn_goToReg);
        buttonCreateAccount.setPaintFlags(buttonCreateAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        buttonLogin = findViewById(R.id.btn_login);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);

        // Initialize FirebaseHelper
        firebaseHelper = new FirebaseHelper();

        // Set onClickListeners for buttons
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegister();
            }
        });
    }

    // Method to perform the login logic
    protected void performLogin() {
        String phoneNum, passwordTxt;
        phoneNum = String.valueOf(phone.getText());
        passwordTxt = String.valueOf(password.getText());

        // Validate phone number
        if (TextUtils.isEmpty(phoneNum)) {
            phone.setError("Phone required");
            phone.requestFocus();
            return;
        } else {
            // Attempt login using FirebaseHelper
            firebaseHelper.loginUser(phoneNum, passwordTxt, new FirebaseHelper.LoginCallback() {
                @Override
                public void onSuccess(String uid, String email, String password, String phone, String username) {
                    handleLoginSuccess(uid, email, password, phone, username);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Method to handle the success of the login process
    protected void handleLoginSuccess(String uid, String email, String password, String phone, String username) {
        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();

        // Save user information in SharedPreferences
        saveUserPreferences(uid, email, password, phone, username);

        // Retrieve and store emergency contacts
        retrieveAndStoreEmergencyContacts(uid);

        // Open home page
        startActivity(new Intent(Login.this, MainActivity.class));
        finish();
    }

    // Method to save user information in SharedPreferences
    private void saveUserPreferences(String uid, String email, String password, String phone, String username) {
        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("uid", uid);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("phone", phone);
        editor.putString("username", username);
        editor.apply();
    }

    // Method to retrieve and store emergency contacts
    private void retrieveAndStoreEmergencyContacts(String uid) {
        firebaseHelper.getEmergencyContacts(uid, new FirebaseHelper.EmergencyContactsCallback() {
            @Override
            public void onEmergencyContactsReceived(List<Contacts> emergencyContacts) {
                // Convert the list of emergency contacts to a string and store it in SharedPreferences
                storeEmergencyContactsInPreferences(emergencyContacts);
            }

            @Override
            public void onEmergencyContactsError(String errorMessage) {
                // Handle error retrieving emergency contacts if needed
                Toast.makeText(Login.this, "Error retrieving emergency contacts: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to store emergency contacts in SharedPreferences
    private void storeEmergencyContactsInPreferences(List<Contacts> emergencyContacts) {
        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        StringBuilder contactsStringBuilder = new StringBuilder();
        for (Contacts contact : emergencyContacts) {
            contactsStringBuilder.append(contact.getContactName()).append(":").append(contact.getContactNum()).append(",");
        }

        Log.v("contacts", contactsStringBuilder.toString());
        editor.putString("emergencyContacts", contactsStringBuilder.toString());
        editor.apply();
    }

    // Method to navigate to the registration screen
    private void navigateToRegister() {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }
}
