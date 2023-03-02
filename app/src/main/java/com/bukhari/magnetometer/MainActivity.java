package com.bukhari.magnetometer;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {
    private TextView value;
    private SensorManager sensorManager;
    public static DecimalFormat DECIMAL_FORMATTER;
    private static final String CSV_HEADER = "Latitude,Longitude,Altitude,MagX,MagY,MagZ,NetField,TimeStamp";
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
        permissionCheck();
//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//                1);
//        ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION  }, 1);


        DECIMAL_FORMATTER = new DecimalFormat("#.000", symbols);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Intent intent = new Intent(this, MagnetometerService.class);
        startService(intent);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    openFilePicker();
                }else {

                    openFilePicker2();
                }
//

            }

        });









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

    private void openFilePicker2() {
        String fileName = "/magnetometerdata.csv";
////        System.out.println(Environment.getExternalStorageDirectory());
        String filePath = Environment.getExternalStorageDirectory() + fileName;
////        System.out.println("filePath: " + filePath);
//
//        // Get the content:// URI using the Storage Access Framework
        Uri uri = Uri.parse(filePath);
//        File file = new File(Environment.getExternalStorageDirectory(),fileName);
//        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
//
        // Create an Intent to open the file
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/csv");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PackageManager packageManager = getPackageManager();

        // Verify that an app is available to handle the Intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the activity to open the file
            startActivity(intent);
        } else {
            // If no app is available to handle the Intent, display an error message
            Toast.makeText(this, "No app available to open CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void openFilePicker() {
//        permissionCheck();

                // Define the URI for the CSV file
                Uri csvFileUri = Uri.parse("content://com.bukhari.magnetometer/magnetometerdata.csv");

        // Create an Intent to open the CSV file in another app
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(csvFileUri, "text/csv");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Launch the other app
                startActivity(intent);


    }


//    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {

                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);


            }
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG,"bg_loc_check");
            if ((checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                // You can use the API that requires the permission.
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Background Location Access")
                            .setMessage("Please grant location access, this app needs background location access. Go to App settings-> Permissions-> Allow all the time")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                       /* handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                c();
                                            }
                                        }, 5000);*/
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "Denied", Toast.LENGTH_SHORT).show();
//                                    c();
                                }
                            })
                            .show();
                }
                new AlertDialog.Builder(MainActivity.this).create().hide();

            }

        }
    }





//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        super.onActivityResult(requestCode, resultCode, resultData);
//        if (requestCode == 42 && resultCode == Activity.RESULT_OK) {
//            // Get the URI of the selected file
//            Uri uri = resultData.getData();
//
//            // Create an Intent to open the file
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(uri, "text/csv");
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            // Verify that an app is available to handle the Intent
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                // Start the activity to open the file
//                startActivity(intent);
//            } else {
//                // If no app is available to handle the Intent, display an error message
//                Toast.makeText(this, "No app available to open CSV file", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }





//    @Override
    protected void onResume() {
        super.onResume();

//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.MANAGE_EXTERNAL_STORAGE  }, 1);
//        sensorManager.registerListener(this,
////                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
////                SensorManager.SENSOR_DELAY_NORMAL);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//
//            System.out.println("55555454454554455454554");
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
//            System.out.println(Man+"--------");
//
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
//        sensorManager.unregisterListener(this);
    }

//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            // get values for each axes X,Y,Z
//            float magX = event.values[0];
//            float magY = event.values[1];
//            float magZ = event.values[2];
//            double magnitude = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));
//
//
////
//
//
//
//            // get GPS location
//            long timestamp = System.currentTimeMillis();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//            String dateString = dateFormat.format(new Date());
//            System.out.println(dateString);
//
//
////            String dateString = dateFormat.format(new Date(timestamp));
//
//
//
//
//            // set value on the screen
////            value.setText(DECIMAL_FORMATTER.format(magnitude) + " \u00B5Tesla");
//            // create csv file and write values to it
//            String csvRow = DECIMAL_FORMATTER.format(magX) + "," +
//                    DECIMAL_FORMATTER.format(magY) + "," +
//                    DECIMAL_FORMATTER.format(magZ) + "," +
//                    (dateString) + "," +
//                    DECIMAL_FORMATTER.format(magnitude);
////                    location.getLatitude() + "," +
////                    location.getLongitude();
//
////            FileWriter writer = null;
//            try {
////                writer = new FileWriter(Environment.getExternalStorageDirectory()+CSV_FILE_NAME, true);
////                if(writer!=null){
////                    writer.append(CSV_HEADER);
////                    writer.append('\n');
////                }
//                writer.append(csvRow);
//                writer.append('\n');
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
////                try {
//////                    writer.flush();
//////                    writer.close();
//////                } catch (IOException e) {
//////                    e.printStackTrace();
//////                }
//            }
//        }
//
//    }

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//    }


}