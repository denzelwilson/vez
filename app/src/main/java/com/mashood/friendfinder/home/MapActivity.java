package com.mashood.friendfinder.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.auth.LoginActivity;
import com.mashood.friendfinder.common.LocationService;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.common.models.UserModel;
import com.mashood.friendfinder.databinding.ActivityMapBinding;
import com.mashood.friendfinder.friends.FriendDetailsActivity;
import com.mashood.friendfinder.friends.FriendsListActivity;
import com.mashood.friendfinder.profile.ProfileActivity;
import com.mashood.friendfinder.settings.SettingsActivity;
import com.mashood.friendfinder.story.StoriesListActivity;
import com.mashood.friendfinder.story.StoryUploadActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mumayank.com.airlocationlibrary.AirLocation;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapBinding binding;

    ArrayList<UserModel> usersList;
    SessionManager sessionManager;
    String username, selectedUsername, latitude, longitude;
    AirLocation airLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();

        setListeners();
    }

    private void init() {
        sessionManager = new SessionManager(this);
        username = sessionManager.getUserDetails().get("username");
        if (!sessionManager.getUserDetails().get("image").isEmpty())
            Picasso.get()
                    .load(NetworkConfig.PROFILE_IMAGE_URL + sessionManager.getUserDetails().get("image"))
                    .into(binding.imgProfile);

        // Start the location service
        startService(new Intent(getApplicationContext(), LocationService.class));
    }

    private void setListeners() {
        binding.btnConnect.setOnClickListener(v -> {
            Intent i = new Intent(this, FriendDetailsActivity.class);
            i.putExtra("username", selectedUsername);
            startActivity(i);
        });

        binding.imgProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        binding.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );

        binding.btnCamera.setOnClickListener(v -> {
            startActivity(new Intent(this, StoryUploadActivity.class));
        });

        binding.btnChat.setOnClickListener(v ->
                startActivity(new Intent(this, FriendsListActivity.class))
        );

        binding.btnStories.setOnClickListener(v ->
                startActivity(new Intent(this, StoriesListActivity.class))
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_map));
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            // Set the username to variable
            selectedUsername = marker.getSnippet();
            // Handle the visibility of connect button
            if (username.equals(marker.getSnippet())) {
                binding.btnConnect.setVisibility(View.GONE);
                binding.btnConnect.setText("Connect");
            } else {
                binding.btnConnect.setVisibility(View.VISIBLE);
                binding.btnConnect.setText("Connect with " + marker.getTitle());
            }
            return true;
        });

        mMap.setOnMapClickListener(latLng -> {
            binding.btnConnect.setVisibility(View.GONE);
            selectedUsername = "";
        });

        fetchLocation();
    }

    private void fetchLocation() {
        ProgressManager.showSimpleProgressDialog(this, "Fetching current location...", false);

        //AirLocation fetching process...
        airLocation = new AirLocation(this, true, true, new AirLocation.Callbacks() {
            @Override
            public void onSuccess(@NonNull Location location) {
                ProgressManager.removeSimpleProgressDialog();
                Toast.makeText(MapActivity.this, "Location fetched successfully", Toast.LENGTH_LONG).show();
                double lat, lng;
                lat = location.getLatitude();
                lng = location.getLongitude();
                latitude = Double.toString(lat);
                longitude = Double.toString(lng);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));

                fetchUsersData();
            }

            @Override
            public void onFailed(@NonNull AirLocation.LocationFailedEnum locationFailedEnum) {
                ProgressManager.removeSimpleProgressDialog();
                Toast.makeText(getApplicationContext(), "Location fetching failed. Please check your internet connection", Toast.LENGTH_LONG).show();
            }
        });
    }


    // override and call airLocation object's method by the same name
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        airLocation.onActivityResult(requestCode, resultCode, data);
    }


    // override and call airLocation object's method by the same name
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void fetchUsersData() {
        usersList = new ArrayList<>();
        String url = NetworkConfig.BASE_URL + NetworkConfig.GET_USERS_LIST_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Getting user details...", false);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    if (response.equals("null")) {
                        Toast.makeText(this, "No users found!", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            JSONArray data = new JSONArray(response);

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject user = data.getJSONObject(i);
                                usersList.add(new UserModel(
                                        user.getString("id"),
                                        user.getString("name"),
                                        user.getString("username"),
                                        user.getString("image"),
                                        user.getString("latitude"),
                                        user.getString("longitude")
                                ));
                            }
                            addMarkersInMap();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    ProgressManager.removeSimpleProgressDialog();
                    Toast.makeText(MapActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void addMarkersInMap() {
        for (int i = 0; i < usersList.size(); i++) {
            if (!usersList.get(i).getLatitude().isEmpty() && !usersList.get(i).getLongitude().isEmpty()) {
                LatLng location = new LatLng(
                        Double.parseDouble(usersList.get(i).getLatitude()),
                        Double.parseDouble(usersList.get(i).getLongitude())
                );
                if (!usersList.get(i).getImage().isEmpty()) {
                    UserModel data = usersList.get(i);
                    // Start a new thread to load the bitmap from image URL
                    Thread thread = new Thread(() -> {
                        Bitmap image = null;
                        try {
                            URL url = new URL(NetworkConfig.PROFILE_IMAGE_URL + data.getImage());
                            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Bitmap finalImage = image;
                        runOnUiThread(() -> {
                            if (finalImage != null) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(data.getName())
                                        .snippet(data.getUsername())
                                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(finalImage))));
//                                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
//                                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                            }
                        });
                    });
                    thread.start();
                } else {
                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(usersList.get(i).getName())
                            .snippet(usersList.get(i).getUsername()));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                }
            }
        }
    }

    private Bitmap getMarkerBitmapFromView(Bitmap image) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageBitmap(image);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        // Scale the bitmap to lower size and return it
        return Bitmap.createScaledBitmap(returnedBitmap, 100, 100, false);
    }

    @Override
    public void onBackPressed() {
        showLogoutAlert();
    }


    private void showLogoutAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from your account?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    startActivity(new Intent(MapActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
