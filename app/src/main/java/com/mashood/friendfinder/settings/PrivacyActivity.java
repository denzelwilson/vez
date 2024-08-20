package com.mashood.friendfinder.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.LocationService;

public class PrivacyActivity extends AppCompatActivity {

    ImageView btnBack;
    SwitchMaterial switchLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        btnBack = findViewById(R.id.btnBack);
        switchLocation = findViewById(R.id.switchLocation);

        setListeners();
    }

    private void setListeners() {
        btnBack.setOnClickListener(v ->
                finish()
        );

        switchLocation.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                startService(new Intent(getApplicationContext(), LocationService.class));
                Toast.makeText(this, "Location service enabled", Toast.LENGTH_SHORT).show();
            }
            else {
                stopService(new Intent(getApplicationContext(), LocationService.class));
                Toast.makeText(this, "Location service disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
