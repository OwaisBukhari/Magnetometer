package com.bukhari.magnetometer;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FileUploadScheduler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Schedule the FileUploadService to run every 10 minutes
        Intent serviceIntent = new Intent(context, FileUploadService.class);
        context.startService(serviceIntent);

        // Reschedule this broadcast receiver to run again after 10 minutes
        scheduleNextRun(context);
    }

    public static void scheduleNextRun(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, FileUploadScheduler.class), 0);

        long intervalMillis = 200 * 1000; // 10 minutes in milliseconds
        long currentTime = System.currentTimeMillis();

        // Schedule the next run after 10 minutes from the current time
        alarmManager.set(AlarmManager.RTC, currentTime + intervalMillis, pendingIntent);
    }
}
