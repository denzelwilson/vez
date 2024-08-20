package com.mashood.friendfinder.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SecurityActivity extends AppCompatActivity {

    TextInputLayout lytPassword;
    TextInputEditText etPassword;
    ImageView btnUpdate, btnBack;
    String username, oldPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        lytPassword = findViewById(R.id.lytPassword);
        etPassword = findViewById(R.id.etPassword);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);

        username = new SessionManager(this).getUserDetails().get("username");
        oldPassword = new SessionManager(this).getUserDetails().get("password");

        setListeners();
    }

    private void setListeners() {
        btnUpdate.setOnClickListener(v ->
                validatePassword()
        );

        btnBack.setOnClickListener(v ->
                finish()
        );
    }

    private void validatePassword() {
        String newPassword = etPassword.getText().toString();
        clearErrors();

        if (newPassword.isEmpty()) {
            lytPassword.setError("Please enter your new password");
            etPassword.requestFocus();
            return;
        }

        if (newPassword.equals(oldPassword)) {
            lytPassword.setError("New password is same as old one");
            etPassword.requestFocus();
            return;
        }

        updatePassword(newPassword);
    }

    private void clearErrors() {
        lytPassword.setError(null);
    }

    private void updatePassword(String password) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.UPDATE_PASSWORD_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Updating password...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");

                        if (status.equals("1")) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            new SessionManager(this).updatePassword(password);
                            finish();
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
}