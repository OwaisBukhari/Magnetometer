package com.bukhari.magnetometer;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UserLogin extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, emailEditText;
    private Button registerButton, loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);


        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                new UserAuthenticationTask().execute("register", username, password,email);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                new UserAuthenticationTask().execute("login", username, password,email);
            }
        });
    }

    private class UserAuthenticationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString = "http://104.248.224.140/magnetometer/user_operations.php"; // Replace with your server IP
            String operation = params[0];
            String username = params[1];
            String password = params[2];
            String email = params[3];

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Send data
                OutputStream os = urlConnection.getOutputStream();
                String data = URLEncoder.encode("operation", "UTF-8") + "=" + URLEncoder.encode(operation, "UTF-8") +
                        "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") +
                        "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")
                        + "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                os.write(data.getBytes());
                os.flush();
                os.close();

                // Get response
                InputStream is = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String response = reader.readLine();
                reader.close();
                is.close();

                return response;

            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);

                if (result.contains("Registration successful")) {
                    Toast.makeText(UserLogin.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

                // Check if the "userid" key exists in the JSON response
                if (jsonObject.has("userid")) {
                    int userIdint = jsonObject.getInt("userid");
                    System.out.println(userIdint + "userrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
                    String userId = String.valueOf(userIdint);


                    if (result.contains("Login successful")) {
                        Intent intent = new Intent(UserLogin.this, MainActivity.class);
                        intent.putExtra("userid", userId);
                        startActivity(intent);
                    } else {
                        // Handle the case when the login was not successful
                        Toast.makeText(UserLogin.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case when "userid" key is missing in the JSON response
//                    Toast.makeText(UserLogin.this, "Error: No value for userid in the response", Toast.LENGTH_SHORT).show();

                    if (result.contains("Invalid password")) {
                        Toast.makeText(UserLogin.this, "Invalid password", Toast.LENGTH_SHORT).show();

                    }
                }

            } catch (JSONException e) {
                // Handle JSON parsing exception
                e.printStackTrace();
                Toast.makeText(UserLogin.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
            }
        }


    }
}
