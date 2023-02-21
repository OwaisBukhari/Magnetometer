package com.bukhari.magnetometer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView value;
    private SensorManager sensorManager;
    public static DecimalFormat DECIMAL_FORMATTER;
    private static final String CSV_HEADER = "Latitude,Longitude,MagX,MagY,MagZ,TimeStamp,NetField";
    private static final String CSV_FILE_NAME = "/magnetometerdata.csv";
    FileWriter writer = null;
    private LocationManager locationManager;
    DateFormat dateFormat ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        value = (TextView) findViewById(R.id.value);
        // define decimal formatter
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DECIMAL_FORMATTER = new DecimalFormat("#.000", symbols);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Intent intent = new Intent(this, MagnetometerService.class);
        startService(intent);





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

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // get values for each axes X,Y,Z
            float magX = event.values[0];
            float magY = event.values[1];
            float magZ = event.values[2];
            double magnitude = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));


//



            // get GPS location
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String dateString = dateFormat.format(new Date());
            System.out.println(dateString);


//            String dateString = dateFormat.format(new Date(timestamp));




            // set value on the screen
            value.setText(DECIMAL_FORMATTER.format(magnitude) + " \u00B5Tesla");
            // create csv file and write values to it
            String csvRow = DECIMAL_FORMATTER.format(magX) + "," +
                    DECIMAL_FORMATTER.format(magY) + "," +
                    DECIMAL_FORMATTER.format(magZ) + "," +
                    (dateString) + "," +
                    DECIMAL_FORMATTER.format(magnitude);
//                    location.getLatitude() + "," +
//                    location.getLongitude();

//            FileWriter writer = null;
            try {
//                writer = new FileWriter(Environment.getExternalStorageDirectory()+CSV_FILE_NAME, true);
//                if(writer!=null){
//                    writer.append(CSV_HEADER);
//                    writer.append('\n');
//                }
                writer.append(csvRow);
                writer.append('\n');
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                try {
////                    writer.flush();
////                    writer.close();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}