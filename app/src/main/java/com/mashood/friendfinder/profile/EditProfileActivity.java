package com.mashood.friendfinder.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.auth.PhotoUploadActivity;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    TextInputLayout lytFullName, lytUsername, lytEmail, lytPhone;
    TextInputEditText etFullName, etUsername, etEmail, etPhone;
    ImageView btnUpdate, btnBack;

    SessionManager sessionManager;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        lytFullName = findViewById(R.id.lytFullName);
        lytUsername = findViewById(R.id.lytUsername);
        lytEmail = findViewById(R.id.lytEmail);
        lytPhone = findViewById(R.id.lytPhone);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);

        init();
        setListeners();
    }

    private void init() {
        sessionManager = new SessionManager(this);
        username = sessionManager.getUserDetails().get("username");
        etUsername.setText(username);
        etFullName.setText(sessionManager.getUserDetails().get("name"));
        etEmail.setText(sessionManager.getUserDetails().get("email"));
        etPhone.setText(sessionManager.getUserDetails().get("phone"));
    }

    private void setListeners() {
        btnUpdate.setOnClickListener(v ->
                validateFields()
        );

        btnBack.setOnClickListener(v ->
                finish()
        );
    }

    private void validateFields() {
        String fullName = etFullName.getText().toString();
        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();

        // Clear all previously set errors
        clearErrors();

        if (fullName.isEmpty()) {
            lytFullName.setError("Please enter full name");
            etFullName.requestFocus();
            return;
        }
        else if (email.isEmpty()) {
            lytEmail.setError("Please enter email");
            etEmail.requestFocus();
            return;
        }
        else if (phone.isEmpty()) {
            lytPhone.setError("Please enter phone number");
            etPhone.requestFocus();
            return;
        }

        updateProfile(fullName, email, phone);
    }

    private void clearErrors() {
        lytFullName.setError(null);
        lytEmail.setError(null);
        lytPhone.setError(null);
    }

    private void updateProfile(String fullName, String email, String phone) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.UPDATE_PROFILE_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Updating profile...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");

                        if (status.equals("1")) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            sessionManager.updateSession(fullName, email, phone);
                            finish();
                        }
                        else Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                data.put("name", fullName);
                data.put("username", username);
                data.put("email", email);
                data.put("phone", phone);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}
