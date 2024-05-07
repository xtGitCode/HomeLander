package com.example.safetyapp.Activities;

import static com.example.safetyapp.Activities.AlertMode.REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import com.example.safetyapp.R;
import com.example.safetyapp.Services.WhatsappAccessibilityService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.telephony.SmsManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

public class EmergencyMode extends AppCompatActivity {

    private static MediaPlayer mediaPlayer;
    List<String> contactsList;
    String warning_message, startingLocationMessage, contactsString;
    float startingLatitude, startingLongitude;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_mode);

        contactsList = new ArrayList<>();
        EditText passwordEditText = findViewById(R.id.password);
        Button deactivate = findViewById(R.id.btn_enter);

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        String getPassword = preferences.getString("password", "");
        contactsString = preferences.getString("emergencyContacts", "");

        // Retrieve the starting location from SharedPreferences
        startingLatitude = preferences.getFloat("starting_latitude", 0.0f);
        startingLongitude = preferences.getFloat("starting_longitude", 0.0f);

        Location startingLocation = new Location("");
        startingLocation.setLatitude(startingLatitude);
        startingLocation.setLongitude(startingLongitude);
        startingLocationMessage = "Starting location: " + "https://maps.google.com/?q=" + startingLocation.getLatitude() + "," + startingLocation.getLongitude();

        // Play the alarm sound in a loop
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.emergencyalarm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Retrieve the current location directly in EmergencyMode
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

//        get location
        if (isInternetAvailable()){
                if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q){
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Create a Google Maps link with the current location
                        String mapsLink = "https://maps.google.com/?q=" + location.getLatitude() +
                                "," + location.getLongitude();

                        // Use the current location in your logic here
                        warning_message = "Help me! I'm in danger! " + startingLocationMessage +
                                " Last seen location: " + mapsLink;

                        if (!TextUtils.isEmpty(contactsString)) {
                            // Parse the emergency contacts string
                            String[] numbersArray = contactsString.split(",");

                            for (int j = 0; j < numbersArray.length; j++) {
                                String number = numbersArray[j];
                                Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + number + "&text=" + warning_message));
                                startActivity(whatsappIntent);

                                // Start the WhatsappAccessibilityService
                                startAccessibilityService();
                            }
                        }  else {
                            // Handle case where no emergency contacts are stored
                            Toast.makeText(EmergencyMode.this, "No emergency contacts available", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
                else{
                    getGpsLocation();
                }

        }
         else {
                    getGpsLocation();
                }


//        deactivate alarm
        deactivate.setOnClickListener(v -> {
            String enteredPassword = passwordEditText.getText().toString();
            if (enteredPassword.equals(getPassword)) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
            } else {
                // Password is incorrect, show a toast or other feedback
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getGpsLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                // Use requestSingleUpdate instead of requestLocationUpdates
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        // GPS location is obtained, save and use it in your logic
                        String mapsLink = location.getLatitude() +
                                "," + location.getLongitude();
                        warning_message = "Help me! I'm in danger! Starting Location: " + startingLatitude +","+ startingLongitude +
                                " Last seen location: " + mapsLink;

                        // Send SMS using the obtained location
                        sendSMSWithLocation(warning_message);

                        // Remove the listener as we only need the location once
                        locationManager.removeUpdates(this);
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        // Handle provider disabled
                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {
                        // Handle provider enabled
                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                }, null);
            } else {
                // GPS provider is not enabled, prompt the user to enable it
                Toast.makeText(EmergencyMode.this, "Please enable GPS to get accurate location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSMSWithLocation(String warningMessage) {

        if (!TextUtils.isEmpty(contactsString)) {
            // Parse the emergency contacts string
            String[] numbersArray = contactsString.split(",");

            for (int j = 0; j < numbersArray.length; j++) {
                String number = numbersArray[j];
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(number, null, warningMessage, null, null);
                    Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            // Handle case where no emergency contacts are stored
            Toast.makeText(this, "No emergency contacts available", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void startAccessibilityService() {
        Intent intent = new Intent(this, WhatsappAccessibilityService.class);
        startService(intent);
    }

//
    }