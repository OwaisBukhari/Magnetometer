package com.bukhari.magnetometer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("user_id", ""); // Replace with your actual key
            if (!userId.isEmpty()) {

            Intent serviceIntent = new Intent(context, MagnetometerService.class);
            serviceIntent.putExtra("userid", userId);
            context.startService(serviceIntent);
            }

        }
    }
}
