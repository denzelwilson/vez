package com.mashood.friendfinder.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.auth.PhotoUploadActivity;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.databinding.ActivityProfileBinding;
import com.mashood.friendfinder.friends.FriendsListActivity;
import com.mashood.friendfinder.story.MyStoriesActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    //    CircleImageView imgProfile;
//    MaterialButton btnEditProfile;
//    ImageView btnBack;
//    MaterialTextView tvName, tvEmail, tvPhone, tvFriendsCount;
//    LinearLayout lytFriends;
    SessionManager sessionManager;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        tvName = findViewById(R.id.tvName);
//        tvEmail = findViewById(R.id.tvEmail);
//        tvPhone = findViewById(R.id.tvPhone);
//        tvFriendsCount = findViewById(R.id.tvFriendsCount);
//        imgProfile = findViewById(R.id.imgProfile);
//        btnBack = findViewById(R.id.btnBack);
//        btnEditProfile = findViewById(R.id.btnEditProfile);
//        lytFriends = findViewById(R.id.lytFriends);

        init();
        setListeners();
    }

    private void init() {
        sessionManager = new SessionManager(this);
        binding.tvName.setText(sessionManager.getUserDetails().get("name"));
        binding.tvEmail.setText(sessionManager.getUserDetails().get("email"));
        binding.tvPhone.setText(sessionManager.getUserDetails().get("phone"));
        if (!sessionManager.getUserDetails().get("image").isEmpty())
            Picasso.get()
                    .load(NetworkConfig.PROFILE_IMAGE_URL + sessionManager.getUserDetails().get("image"))
                    .into(binding.imgProfile);
    }

    private void setListeners() {
        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class))
        );

        binding.btnEditImage.setOnClickListener(v -> {
            Intent i = new Intent(this, PhotoUploadActivity.class);
            i.putExtra("username", sessionManager.getUserDetails().get("mashood"));
            i.putExtra("type", "update");
            startActivity(i);
            finish();
        });

        binding.btnBack.setOnClickListener(v ->
                finish()
        );

        binding.lytFriends.setOnClickListener(v ->
                startActivity(new Intent(this, FriendsListActivity.class))
        );

        binding.lytStories.setOnClickListener(v ->
                startActivity(new Intent(this, MyStoriesActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFriendsCount(sessionManager.getUserDetails().get("username"));
    }

    private void getFriendsCount(String username) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.GET_FRIENDS_COUNT_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Fetching friends count...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");
                        String friendsCount = data.getString("count");

                        if (status.equals("1")) {
                            binding.tvFriendsCount.setText(friendsCount);
                            getStoriesCount(username);
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

    private void getStoriesCount(String username) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.GET_STORIES_COUNT_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Fetching stories count...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");
                        String storiesCount = data.getString("count");

                        if (status.equals("1")) {
                            binding.tvStoriesCount.setText(storiesCount);
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

}