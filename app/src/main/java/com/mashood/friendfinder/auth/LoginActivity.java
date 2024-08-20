package com.mashood.friendfinder.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.home.MapActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout lytUsername, lytPassword;
    TextInputEditText etUsername, etPassword;
    ImageView btnLogin;
    MaterialTextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Add splash screen by SplashScreen API
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lytUsername = findViewById(R.id.lytUsername);
        lytPassword = findViewById(R.id.lytPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        setListeners();
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v ->
                validateFields()
        );

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegistrationActivity.class))
        );
    }

    private void validateFields() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        // Clear all previously set errors
        clearErrors();

        if (username.isEmpty()) {
            lytUsername.setError("Please enter username");
            etUsername.requestFocus();
            return;
        } else if (password.isEmpty()) {
            lytPassword.setError("Please enter password");
            etPassword.requestFocus();
            return;
        }

        login(username, password);
    }

    private void clearErrors() {
        lytUsername.setError(null);
        lytPassword.setError(null);
    }

    private void login(String username, String password) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.LOGIN_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Logging in...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");
                        String id = data.getString("id");
                        String email = data.getString("email");
                        String phone = data.getString("phone");
                        String gender = data.getString("gender");
                        String image = data.getString("image");
                        String name = data.getString("name");

                        if (status.equals("1")) {
                            // Save user's data via SessionManager
                            new SessionManager(this).createLoginSession(
                                    id, name, username, password, email, phone, gender, image
                            );
                            etUsername.setText("");
                            etPassword.setText("");
                            showLocationTrackingAlert();
                        } else Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    ProgressManager.removeSimpleProgressDialog();
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> data = new HashMap<>();
                data.put("username", username);
                data.put("password", password);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showLocationTrackingAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Your location will be shared with the community on continuing. Are you sure you want to continue?")
                .setPositiveButton("Continue", (dialogInterface, i) ->
                        checkPermissions()
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) + ContextCompat
                    .checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale
                        (this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale
                                (this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    Snackbar.make(this.findViewById(android.R.id.content),
                            "Please enable the permissions to continue",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            v -> requestPermissions(
                                    new String[]{Manifest.permission
                                            .ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                                    100)).show();
                } else {
                    requestPermissions(
                            new String[]{Manifest.permission
                                    .ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            100);
                }
            } else {
                startActivity(new Intent(LoginActivity.this, MapActivity.class));
                finish();
            }
        } else {
            startActivity(new Intent(LoginActivity.this, MapActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0) {
                boolean fineLocationPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean coarseLocationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (coarseLocationPermission && fineLocationPermission) {
                    startActivity(new Intent(LoginActivity.this, MapActivity.class));
                    finish();
                } else {
                    Snackbar.make(this.findViewById(android.R.id.content),
                            "Please enable the permissions to continue",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            v -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(
                                            new String[]{Manifest.permission
                                                    .ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                                            100);
                                }
                            }).show();
                }
            }
        }
    }
}
