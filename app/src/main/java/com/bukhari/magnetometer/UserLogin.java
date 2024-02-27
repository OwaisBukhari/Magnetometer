package com.bukhari.magnetometer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    private static final String SHARED_PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";

    private EditText usernameEditText, passwordEditText, emailEditText;
    private Button registerButton, loginButton, forgotPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        // Check for an existing user session
        if (isLoggedIn()) {
            startMainActivity(getSavedUserId());
        }

        // Disable login and register buttons initially
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        // Set up a TextWatcher for email validation
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Do nothing before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Enable login, register, and forgot password buttons if email and password are valid
                validateInputFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing after text changes
            }
        });

        // Set up a TextWatcher for password validation
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Do nothing before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Enable login, register, and forgot password buttons if email and password are valid
                validateInputFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing after text changes
            }
        });

        // Set up a TextWatcher for username validation
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Do nothing before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Enable register button if all fields are valid
                validateInputFields();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing after text changes
            }
        });

        // Set up click listeners for the login, register, and forgot password buttons
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                new UserAuthenticationTask().execute("login", username, password, email);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                new UserAuthenticationTask().execute("register", username, password, email);
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
    }

    private void validateInputFields() {
        // Validate email, password, and username, enable login, register, and forgot password buttons if all are valid
        if (isValidEmail(emailEditText.getText().toString())
                && isValidPassword(passwordEditText.getText().toString())
                && isValidUsername(usernameEditText.getText().toString())) {
            loginButton.setEnabled(true);
            registerButton.setEnabled(true);
            forgotPasswordButton.setEnabled(true);
        } else {
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
            forgotPasswordButton.setEnabled(false);
        }
    }

    private boolean isValidUsername(CharSequence target) {
        // Add your username validation logic here if needed
        return !TextUtils.isEmpty(target);
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean isValidPassword(CharSequence target) {
        // Add your password validation logic here if needed
        return !TextUtils.isEmpty(target);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(KEY_USER_ID);
    }

    private String getSavedUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    private void saveUserId(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    private void startMainActivity(String userId) {
        Intent intent = new Intent(UserLogin.this, MainActivity.class);
        intent.putExtra("userid", userId);
        startActivity(intent);
        finish();  // Finish the current activity to prevent going back to the login screen
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
                if (result.contains("Username already exists")) {
                    Toast.makeText(UserLogin.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

                if (jsonObject.has("userid")) {
                    int userIdint = jsonObject.getInt("userid");
                    String userId = String.valueOf(userIdint);

                    if (result.contains("Login successful")) {
                        // Save the user ID to SharedPreferences
                        saveUserId(userId);

                        // Go to the main activity
                        startMainActivity(userId);
                    } else {
                        Toast.makeText(UserLogin.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (result.contains("Invalid password")) {
                        Toast.makeText(UserLogin.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(UserLogin.this, "Unstable Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        View view = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        final EditText emailEditText = view.findViewById(R.id.forgotEmailEditText);
        builder.setView(view);

        builder.setPositiveButton("Reset Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEditText.getText().toString().trim();
                if (isValidEmail(email)) {
                    new ResetPasswordTask().execute(email);
                } else {
                    Toast.makeText(UserLogin.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private class ResetPasswordTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString = "http://104.248.224.140/magnetometer/forgot_password.php"; // Replace with your server URL
            String email = params[0];

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Send data
                OutputStream os = urlConnection.getOutputStream();
                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
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

                if (result.contains("Password reset link sent")) {
                    Toast.makeText(UserLogin.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                } else if (result.contains("No account found with that email")) {
                    Toast.makeText(UserLogin.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserLogin.this, "Failed to reset password"+result, Toast.LENGTH_SHORT).show();
                    System.out.println(result);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(UserLogin.this, "Unstable Internet Connection"+e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
