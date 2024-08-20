package com.mashood.friendfinder.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.databinding.ActivitySettingsBinding;
import com.mashood.friendfinder.friends.FriendRequestsActivity;
import com.squareup.picasso.Picasso;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();
    }

    private void init() {
        binding.tvName.setText(new SessionManager(this).getUserDetails().get("name"));
        binding.tvPhone.setText(new SessionManager(this).getUserDetails().get("phone"));
        if (!new SessionManager(this).getUserDetails().get("image").isEmpty())
            Picasso.get()
                    .load(NetworkConfig.PROFILE_IMAGE_URL + new SessionManager(this).getUserDetails().get("image"))
                    .into(binding.imgProfile);
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v ->
                finish()
        );

        binding.lytNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, FriendRequestsActivity.class))
        );

        binding.lytPrivacy.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyActivity.class))
        );

        binding.lytSecurity.setOnClickListener(v ->
                startActivity(new Intent(this, SecurityActivity.class))
        );
    }
}