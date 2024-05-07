package com.example.safetyapp.Services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.safetyapp.Activities.CheckIn;
import com.example.safetyapp.Utils.DisableAlarm;
import com.example.safetyapp.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static MediaPlayer mediaPlayer;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarm received!");

        // Start the PasswordActivity to input the password
        Intent passwordIntent = new Intent(context, DisableAlarm.class);
        passwordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(passwordIntent);

        Intent i = new Intent(context, CheckIn.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Notify")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Check-In Alert")
                .setContentText("Hey, Let us know you are safe!!!")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManagerCompat.notify(200, builder.build());
        // Play the alarm sound in a loop
        mediaPlayer = MediaPlayer.create(context, R.raw.emergencyalarm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public static void stopAlarm(Context context) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}


