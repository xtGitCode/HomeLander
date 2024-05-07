package com.example.safetyapp.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.safetyapp.R;
import com.example.safetyapp.Services.WhatsappAccessibilityService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class AlertMode extends FragmentActivity implements OnMapReadyCallback {

    private Button emergencyMode, setCheckin, deactivate;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ProgressBar progressBar;
    static final int REQUEST_CODE = 101;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_alert_mode);

        // Initialize the progressBar
        progressBar = findViewById(R.id.progressBar);

        emergencyMode = findViewById(R.id.btn_emergency);
        setCheckin = findViewById(R.id.btn_checkin);
        deactivate = findViewById(R.id.btn_deactivate);

        deactivate.setPaintFlags(deactivate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();

        requestSmsPermission();

        checkAccessibilityService();

        displayCheckinTime();

        setButtonClickListeners();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(AlertMode.this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);
    }

    private void checkAccessibilityService() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
            if (!isAccessibilityServiceEnabled(getApplicationContext(), WhatsappAccessibilityService.class.getName())) {
                // Service not enabled, prompt the user to enable it
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    private void displayCheckinTime() {
        // Retrieve check-in time from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        int checkinHour = preferences.getInt("checkin_hour", -1);
        int checkinMinute = preferences.getInt("checkin_minute", -1);

        if (checkinHour != -1 && checkinMinute != -1) {
            // Display check-in time in a TextView (assuming you have a TextView with id 'textViewCheckinTime')
            TextView textViewCheckinTime = findViewById(R.id.CheckInTime);
            textViewCheckinTime.setText(checkinHour + " : " + String.format("%02d", checkinMinute));
        }
    }

    private void setButtonClickListeners() {
        emergencyMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), EmergencyMode.class));
            }
        });

        deactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), MainActivity.class));
            }
        });

        setCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), CheckIn.class));
            }
        });
    }

    private void fetchLocation() {
        if (checkLocationPermission()) {
            if (isInternetAvailable()) {
                fetchLastKnownLocation();
            } else {
                getGpsLocation();
            }
        }
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void fetchLastKnownLocation() {
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
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    saveStartingLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                    showLocationOnMap();
                }
            }
        });
    }

    private void getGpsLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                requestSingleLocationUpdate(locationManager);
            } else {
                // GPS provider is not enabled, prompt the user to enable it
                Toast.makeText(AlertMode.this, "Please enable GPS to get accurate location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestSingleLocationUpdate(LocationManager locationManager) {
        if (checkLocationPermission()) {
            progressBar.setVisibility(View.VISIBLE);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    progressBar.setVisibility(View.GONE);
                    currentLocation = location;
                    saveStartingLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                    showLocationOnMap();
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, null);
        }
    }

    private void showLocationOnMap() {
        Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMap);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(AlertMode.this);
    }

    private void saveStartingLocation(double latitude, double longitude) {
        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("starting_latitude", (float) latitude);
        editor.putFloat("starting_longitude", (float) longitude);
        editor.apply();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        googleMap.addMarker(markerOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }
                break;
        }
    }

    public static boolean isAccessibilityServiceEnabled(Context context, String service) {
        int accessibilityEnabled = 0;
        final String serviceId = context.getPackageName() + "/" + service;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                colonSplitter.setString(settingValue);
                while (colonSplitter.hasNext()) {
                    String accessibilityService = colonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(serviceId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
