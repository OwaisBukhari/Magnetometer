package com.bukhari.magnetometer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;

public class FileUploadService extends IntentService {

    private static final String TAG = "FileUploadService";
    private static final String SERVER_URL = "http://104.248.224.140/magnetometer/upload.php"; // Replace with your server URL

    public FileUploadService() {
        super("FileUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (fileExists()) {
            if (uploadFileToServer()) {
                // If the upload is successful, delete the file and create a new empty file
                deleteFile();
//                createEmptyFile();
            }
        } else {
            Log.d(TAG, "File does not exist. Skipping upload.");
        }
    }

    private boolean fileExists() {
        String fileToUpload = Environment.getExternalStorageDirectory() + "/magnetometerdata2.csv";
        return new File(fileToUpload).exists();
    }

    private void deleteFile() {
        String filePath = Environment.getExternalStorageDirectory() + "/magnetometerdata2.csv";
        File fileToDelete = new File(filePath);

        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                Log.d(TAG, "File deleted successfully.");
            } else {
                Log.e(TAG, "Failed to delete the file.");
            }
        } else {
            Log.e(TAG, "File not found for deletion.");
        }
    }

    private boolean uploadFileToServer() {
        try {
//            stopService(new Intent(this, MagnetometerService.class));


            File fileToUpload = new File(Environment.getExternalStorageDirectory(), "magnetometerdata2.csv");
            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "file.csv",
                            RequestBody.create(MediaType.parse("text/csv"), fileToUpload))
                    .build();

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(requestBody)
                    .build();

            Headers headers = request.headers();
            for (int i = 0, size = headers.size(); i < size; i++) {
                Log.d(TAG, headers.name(i) + ": " + headers.value(i));
            }

            Response response = client.newCall(request).execute();
            Log.d(TAG, "Response: " + response.body().string());

            if (response.isSuccessful()) {
                Log.d(TAG, "File uploaded successfully.");
                // If the upload is successful, you can perform any additional actions here.
                return true;
            } else {
                Log.e(TAG, "File upload failed.");
                // Handle the case where the upload fails.
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "File upload error: " + e.getMessage());

            // Handle any exceptions that occur during the file upload.
            return false;
        }
    }

    private void createEmptyFile() {
        String filePath = Environment.getExternalStorageDirectory() + "/magnetometerdata2.csv";
        File emptyFile = new File(filePath);

        try {
            if (emptyFile.createNewFile()) {
                Log.d(TAG, "Empty file created successfully.");
            } else {
                Log.e(TAG, "Failed to create empty file.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating empty file: " + e.getMessage());
        }
    }
}
