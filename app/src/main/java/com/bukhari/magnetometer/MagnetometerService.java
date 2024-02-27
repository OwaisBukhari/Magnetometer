package com.bukhari.magnetometer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bukhari.magnetometer.FileUploadService;
import com.bukhari.magnetometer.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MagnetometerService extends Service implements SensorEventListener {
    private static final String TAG = "MagnetometerService";
//    private static final long INTERVAL = 1000; // update interval in milliseconds
    private static final String CSV_FILE_NAME = "/magnetometerdata2.csv";
    private static String userid ;
    private static final String CSV_HEADER = "UserID,Latitude,Longitude,Altitude,MagX,MagY,MagZ,NetField,TimeStamp";
    private static final String CHANNEL_ID = "ForegroundServiceforMagnetometer";

    private SensorManager sensorManager;
    private Sensor sensor;
    private LocationManager locationManager;
    private Location location;
    private DecimalFormat decimalFormatter;
    private BufferedWriter writer = null;
    private double magX, magY, magZ, magnitude;
    private Timer timer;
    private final long TIMER_INTERVAL = 1*10*1000; // 5 seconds interval

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createNotificationChannel();



        // initialize writer when the service is created
        try {
            writer = new BufferedWriter(new FileWriter(getFile(), true));
            writer.write(CSV_HEADER);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize the ScheduledExecutorService
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // Your code to write data to CSV here
                writeDataToCSV();
            }
        }, 0, TIMER_INTERVAL, TimeUnit.MILLISECONDS);


//        // Initialize the timer
//        timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                // Your code to write data to CSV here
//                writeDataToCSV();
//            }
//        }, 0, TIMER_INTERVAL);
    }


    private File getFile() {

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q) {


            return new File(Environment.getExternalStorageDirectory() + CSV_FILE_NAME);

        }else {
            return new File(getExternalFilesDir(null) + CSV_FILE_NAME);
        }}



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("STOP_SERVICE")) {
//            stopSelf();
            return START_NOT_STICKY;
        }
         userid =intent.getStringExtra("userid");
        System.out.println("userid in service"+userid);


////        Intent serviceIntent = new Intent(this, FileUploadService.class);
////        serviceIntent.setAction("STOP_SERVICE");
//        PendingIntent pendingIntent = PendingIntent.getService(
//                this,
//                0,
//                serviceIntent,
//                PendingIntent.FLAG_IMMUTABLE
//        );

        Intent stopServiceIntent = new Intent(this, MagnetometerService.class);
        stopServiceIntent.setAction("STOP_SERVICE");

        PendingIntent stopServicePendingIntent = PendingIntent.getService(
                this,
                0,
                stopServiceIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Magnetometer Service")
                .setContentText("Recording Magnetometer Data...")

                ///nitifcation k sath stop button de do ?//
//                .addAction(R.drawable, "Stop", stopServicePendingIntent)





//                .setContentIntent(stopServicePendingIntent)
                .setSmallIcon(R.mipmap.magnetometericon)
                .build();
//        set pendig intent to open app
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
//        notification.contentIntent = pendingIntent;





        // define decimal formatter
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        decimalFormatter = new DecimalFormat("#.000", symbols);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_NOT_STICKY;
        }

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
        }

        startForeground(1, notification);

        // register sensor listener
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister sensor listener
        sensorManager.unregisterListener(this);

        // Stop the timer when the service is destroyed
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        // close the BufferedWriter when the service is destroyed
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magX = event.values[0];
            magY = event.values[1];
            magZ = event.values[2];
            magnitude = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));

            // get GPS location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
            }

            // write values to CSV file (moved to the timer)
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not needed for this example
    }

    private void writeDataToCSV() {
        // write values to CSV file
        if (location != null) {
            String csvRow = userid+","+
                    location.getLatitude() + "," +
                    location.getLongitude() + "," +
                    location.getAltitude() + "," +
                    decimalFormatter.format(magX) + "," +
                    decimalFormatter.format(magY) + "," +
                    decimalFormatter.format(magZ) + "," +
                    decimalFormatter.format(magnitude) + "," +
                    new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).format(new Date());

            try {
                if ((writer != null) && (getFile().exists())) {
                    writer.write(csvRow);
                    writer.newLine();
                    writer.flush();
                }else
                {
                    writer = new BufferedWriter(new FileWriter(getFile(), true));
                    writer.write(CSV_HEADER);
                    writer.newLine();
                    writer.write(csvRow);

                    writer.newLine();
                    writer.flush();}



                } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_ID,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
