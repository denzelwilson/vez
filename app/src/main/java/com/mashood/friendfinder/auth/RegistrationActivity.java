package com.mashood.friendfinder.auth;

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
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    TextInputLayout lytFullName, lytUsername, lytPassword, lytEmail, lytPhone;
    TextInputEditText etFullName, etUsername, etPassword, etEmail, etPhone;
    MaterialButton btnMale, btnFemale;
    ImageView btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        lytFullName = findViewById(R.id.lytFullName);
        lytUsername = findViewById(R.id.lytUsername);
        lytPassword = findViewById(R.id.lytPassword);
        lytEmail = findViewById(R.id.lytEmail);
        lytPhone = findViewById(R.id.lytPhone);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);

        setListeners();
    }

    private void setListeners() {
        btnRegister.setOnClickListener(v ->
                validateFields()
        );
    }

    private void validateFields() {
        String fullName = etFullName.getText().toString();
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();

        // Clear all previously set errors
        clearErrors();

        if (fullName.isEmpty()) {
            lytFullName.setError("Please enter full name");
            etFullName.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            lytUsername.setError("Please enter username");
            etUsername.requestFocus();
            return;
        }
        else if (password.isEmpty()) {
            lytPassword.setError("Please enter password");
            etPassword.requestFocus();
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
        else if (!btnMale.isChecked() && !btnFemale.isChecked()) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender;
        if (btnMale.isChecked()) gender = "Male";
        else gender = "Female";

        register(fullName, username, password, email, phone, gender);
    }

    private void clearErrors() {
        lytFullName.setError(null);
        lytUsername.setError(null);
        lytPassword.setError(null);
        lytEmail.setError(null);
        lytPhone.setError(null);
    }

    private void register(String fullName, String username, String password, String email, String phone, String gender) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.REGISTRATION_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Creating your account...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");

                        if (status.equals("1")) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(this, PhotoUploadActivity.class);
                            i.putExtra("username", username);
                            i.putExtra("type", "upload");
                            startActivity(i);
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
                data.put("password", password);
                data.put("email", email);
                data.put("phone", phone);
                data.put("gender", gender);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
