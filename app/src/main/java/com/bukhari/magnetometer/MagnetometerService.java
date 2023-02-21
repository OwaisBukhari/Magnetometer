package com.bukhari.magnetometer;

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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MagnetometerService extends Service implements SensorEventListener {
    private static final String TAG = "MagnetometerService";
    private static final long INTERVAL = 1000; // update interval in milliseconds
    private static final String CSV_FILE_NAME = "/magnetometerdata.csv";
    private static final String CSV_HEADER = "Latitude,Longitude,MagX,MagY,MagZ,TimeStamp,NetField";
    private static final String CHANNEL_ID = "ForegroundServiceforMagnetometer";


    private SensorManager sensorManager;
    private Sensor sensor;
    private LocationManager locationManager;
    private Location location;
    private DecimalFormat decimalFormatter;
    FileWriter writer = null;


    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        createNotificationChannel();
        Intent stopServiceIntent = new Intent(this, MagnetometerService.class);
        stopServiceIntent.setAction("STOP_SERVICE");
        PendingIntent stopServicePendingIntent = PendingIntent.getService(
                this,
                0,
                stopServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );





        Notification notification = new NotificationCompat.Builder(this,TAG)
                .setContentTitle("Magnetometer Service")
                .setContentText("Recording Magnetometer Data...Click to stop")
                .setContentIntent(stopServicePendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background)


                .build();

        // define decimal formatter
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        decimalFormatter = new DecimalFormat("#.000", symbols);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        startForeground(1,notification);


    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    TAG,
                    TAG,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction()!=null && intent.getAction().equals("STOP_SERVICE")){
            stopSelf();
            return START_NOT_STICKY;
        }
        // create notification for foreground service

        try {
            writer = new FileWriter(Environment.getExternalStorageDirectory() + CSV_FILE_NAME, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (writer != null) {
            try {
                writer.append(CSV_HEADER);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                writer.append('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // start service in the foreground
//        startForeground(1, notification);

        // register sensor listener
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // unregister sensor listener
        sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // get values for each axes X,Y,Z
            float magX = event.values[0];
            float magY = event.values[1];
            float magZ = event.values[2];
            double magnitude = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));
            System.out.println("Bhai yeee chalraha SERVICE walaa");

            // get GPS location
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            System.out.println(location.getLatitude()+"sssssssss");


            // check if location is not null before accessing it
//            if (location != null) {
                // set value on the screen
//                String text = "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude() + ", MagX: " + decimalFormatter.format(magX) + ", MagY: " + decimalFormatter.format(magY) + ", MagZ: " + decimalFormatter.format(magZ) + ", Magnitude: " + decimalFormatter.format(magnitude) + " \u00B5Tesla";
//                Log.d(TAG, text);
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String dateString = dateFormat.format(new Date());
            System.out.println(dateString);


            // write values to CSV file
                String csvRow =
                                location.getLongitude() + "," +
                                location.getLatitude() + "," +
                                decimalFormatter.format(magX) + "," +
                        decimalFormatter.format(magY) + "," +
                        decimalFormatter.format(magZ) + ","+
//                                String.valueOf(System.currentTimeMillis()) + ","+
                                        dateString + ","+

                                        decimalFormatter.format(magnitude);

//                try {
//                writer = new FileWriter(Environment.getExternalStorageDirectory()+CSV_FILE_NAME, true);
//                if(writer!=null){
//                    writer.append(CSV_HEADER);
//                    writer.append('\n');
////                }
            try {
                writer.append(csvRow);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            System.out.println("Bhai writer chala"+csvRow);
            try {
                writer.append('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//                } catch (IOException e) {
//                    e.printStackTrace();}
//                } finally {
//                    try {
////                    writer.flush();
////                        writer.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        }

//        public void onAccuracyChanged(Sensor sensor, int i) {

//    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

