package com.mashood.friendfinder.friends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.models.FriendModel;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.friends.adapters.FriendsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsListActivity extends AppCompatActivity implements FriendsAdapter.ClickListener {

    RecyclerView recycler;
    ArrayList<FriendModel> friendsList;
    FriendsAdapter adapter;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        recycler = findViewById(R.id.recycler);
        init();
    }

    private void init() {
        username = new SessionManager(this).getUserDetails().get("username");
        fetchFriendsList();
    }

    private void fetchFriendsList() {
        friendsList = new ArrayList<>();
        ProgressManager.showSimpleProgressDialog(this, "Getting friends info...", false);
        String url = NetworkConfig.BASE_URL + NetworkConfig.FRIENDS_LIST_ENDPOINT;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONArray data = new JSONArray(response);

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject user = data.getJSONObject(i);

                            if (username.equals(user.getString("from_username"))) {
                                friendsList.add(new FriendModel(
                                        user.getString("id"),
                                        user.getString("to_name"),
                                        user.getString("to_username"),
                                        user.getString("to_image")
                                ));
                            } else if (username.equals(user.getString("to_username"))) {
                                friendsList.add(new FriendModel(
                                        user.getString("id"),
                                        user.getString("from_name"),
                                        user.getString("from_username"),
                                        user.getString("from_image")
                                ));
                            }
                        }
                        // Show all friends in recyclerView
                        setupRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    ProgressManager.removeSimpleProgressDialog();
                    Toast.makeText(FriendsListActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void setupRecyclerView() {
        adapter = new FriendsAdapter(FriendsListActivity.this, friendsList, this);
        recycler.setHasFixedSize(true);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void getPhoneNumber(String username) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.GET_USER_DATA_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Fetching phone number...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");
                        String phone = data.getString("phone");

                        if (status.equals("1")) {
                            if (!phone.contains("+91")) {
                                phone = "+91" + phone;
                            }
                            String whatsappURL = "https://api.whatsapp.com/send?phone=" + phone;
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappURL));
                            startActivity(browserIntent);
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

    @Override
    public void onChatClicked(String username) {
        getPhoneNumber(username);
    }
}
