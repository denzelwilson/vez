package com.mashood.friendfinder.dashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.LocationService;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.friends.FriendRequestsActivity;
import com.mashood.friendfinder.friends.FriendsListActivity;
import com.mashood.friendfinder.home.MapActivity;
import com.mashood.friendfinder.profile.EditProfileActivity;
//import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends AppCompatActivity {

    CircleImageView imgProfile;
    LinearLayout lytViewFriends;
    TextView tvName, tvFriendsCount;
    MaterialButton btnFriendRequests, btnEditProfile, btnViewNearbyFriends;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        imgProfile = findViewById(R.id.imgProfile);
        lytViewFriends = findViewById(R.id.lytViewFriends);
        tvName = findViewById(R.id.tvName);
        tvFriendsCount = findViewById(R.id.tvFriendsCount);
        btnFriendRequests = findViewById(R.id.btnFriendRequests);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnViewNearbyFriends = findViewById(R.id.btnViewNearbyFriends);

        init();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFriendsCount(sessionManager.getUserDetails().get("username"));
    }

    private void init() {
        sessionManager = new SessionManager(this);
        tvName.setText(sessionManager.getUserDetails().get("name"));
        Glide.with(this)
                .load(NetworkConfig.PROFILE_IMAGE_URL + sessionManager.getUserDetails().get("image"))
                .into(imgProfile);

        // Start the location service
        startService(new Intent(getApplicationContext(), LocationService.class));
    }

    private void setListeners() {
        lytViewFriends.setOnClickListener(v ->
            startActivity(new Intent(this, FriendsListActivity.class))
        );

        btnFriendRequests.setOnClickListener(v ->
            startActivity(new Intent(this, FriendRequestsActivity.class))
        );

        btnEditProfile.setOnClickListener(v ->
            startActivity(new Intent(this, EditProfileActivity.class))
        );

        btnViewNearbyFriends.setOnClickListener(v ->
            startActivity(new Intent(this, MapActivity.class))
        );
    }

    private void getFriendsCount(String username) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.GET_FRIENDS_COUNT_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Fetching profile info...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");
                        String friendsCount = data.getString("count");

                        if (status.equals("1")) {
                            tvFriendsCount.setText(friendsCount);
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
                data.put("username", username);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}