package com.mashood.friendfinder.friends;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendDetailsActivity extends AppCompatActivity {

    ImageView imgProfileFull;
    CircleImageView imgProfile;
    MaterialTextView tvName, tvUsername;
    MaterialButton btnConnect;

    String username;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);

        imgProfile = findViewById(R.id.imgProfile);
        imgProfileFull = findViewById(R.id.imgProfileFull);
        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        btnConnect = findViewById(R.id.btnConnect);

        username = getIntent().getStringExtra("username");
        sessionManager = new SessionManager(this);

        setListeners();
        getUserDetails();
    }

    private void setListeners() {
        btnConnect.setOnClickListener(v ->
                sendFriendRequest()
        );
    }

    private void getUserDetails() {
        String url = NetworkConfig.BASE_URL + NetworkConfig.GET_USER_DATA_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Fetching user details...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");
                        String name = data.getString("name");
                        String image = data.getString("image");

                        if (status.equals("1")) {
                            tvName.setText(name);
                            tvUsername.setText(username);
                            Picasso.get()
                                    .load(NetworkConfig.PROFILE_IMAGE_URL + image)
                                    .into(imgProfile);
                            Picasso.get()
                                    .load(NetworkConfig.PROFILE_IMAGE_URL + image)
                                    .into(imgProfileFull);
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
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void sendFriendRequest() {
        String url = NetworkConfig.BASE_URL + NetworkConfig.REQUEST_FRIEND_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Sending connection request...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");

                        if (status.equals("1")) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                data.put("from_username", sessionManager.getUserDetails().get("username"));
                data.put("from_name", sessionManager.getUserDetails().get("name"));
                data.put("from_image", sessionManager.getUserDetails().get("image"));
                data.put("to_username", username);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}