package com.example.safetyapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.safetyapp.R;
import com.example.safetyapp.Services.AlarmReceiver;

import java.util.Calendar;
import java.util.Date;

public class CheckIn extends AppCompatActivity{
    Button setTime;
    TimePicker timePicker;
    int hour, min;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        timePicker = findViewById(R.id.timepicker);
        setTime = findViewById(R.id.btn_picktime);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hour = hourOfDay;
                min = minute;
            }
        });
        
        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CheckIn.this, "Set Check In Time" + hour + " : " + String.format("%02d", min), Toast.LENGTH_SHORT).show();
                setAlarm();
                notification();
                Intent intent = new Intent(v.getContext(), AlertMode.class);
                startActivity(intent);
            }
        });
    }

    private void setAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Date date = new Date();

        Calendar cal_alarm = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();

        cal_now.setTime(date);
        cal_alarm.setTime(date);

        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);
        cal_alarm.set(Calendar.MINUTE, min);
        cal_alarm.set(Calendar.SECOND, 0);

        if(cal_alarm.before(cal_now)){
            cal_alarm.add(Calendar.DATE, 1);
        }

        Intent i = new Intent(CheckIn.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(CheckIn.this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(),pendingIntent);

        // Store the selected check-in time in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("checkin_hour", hour);
        editor.putInt("checkin_minute", min);
        editor.apply();

    }

    private void notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Checking In";
            String description = "Hey, Let us know you are safe";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("Notify", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}