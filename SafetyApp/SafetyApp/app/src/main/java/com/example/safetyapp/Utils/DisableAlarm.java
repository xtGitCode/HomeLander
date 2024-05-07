package com.example.safetyapp.Utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.safetyapp.Activities.EmergencyMode;
import com.example.safetyapp.R;
import com.example.safetyapp.Services.AlarmReceiver;

public class DisableAlarm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disable_alarm);

        // Retrieve the starting location from the Intent
        Location startingLocation = getIntent().getParcelableExtra("startingLocation");

        EditText passwordEditText = findViewById(R.id.passwordDisable);
        TextView countdown = findViewById(R.id.countdownTimer);

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        String getPassword = preferences.getString("password", "");

        // Set the countdown timer for 20 seconds
        CountDownTimer countDownTimer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the countdown TextView with the remaining time
                countdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                AlarmReceiver.stopAlarm(getApplicationContext());
                Intent emergencyIntent = new Intent(DisableAlarm.this, EmergencyMode.class);
                emergencyIntent.putExtra("startingLocation", startingLocation);
                startActivity(emergencyIntent);
                finish();
            }
        };

        countDownTimer.start();

            // Check the entered password when a button is clicked
        findViewById(R.id.btn_enter).setOnClickListener(v -> {
            String enteredPassword = passwordEditText.getText().toString();

            if (enteredPassword.equals(getPassword)) {
                // Password is correct, stop the alarm and finish the activity
                countDownTimer.cancel();
                AlarmReceiver.stopAlarm(getApplicationContext());
                finish();
            } else {
                // Password is incorrect, show a toast or other feedback
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
