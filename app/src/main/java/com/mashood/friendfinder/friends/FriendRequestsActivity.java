package com.mashood.friendfinder.friends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mashood.friendfinder.common.models.FriendModel;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.databinding.ActivityFriendRequestsBinding;
import com.mashood.friendfinder.friends.adapters.RequestsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendRequestsActivity extends AppCompatActivity implements RequestsAdapter.ClickListener {

    private ActivityFriendRequestsBinding binding;
    ArrayList<FriendModel> requestsList;
    RequestsAdapter adapter;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        username = new SessionManager(this).getUserDetails().get("username");
        fetchRequestsList();
    }

    private void fetchRequestsList() {
        requestsList = new ArrayList<>();
        ProgressManager.showSimpleProgressDialog(this, "Getting friends info...", false);
        String url = NetworkConfig.BASE_URL + NetworkConfig.REQUESTS_LIST_ENDPOINT;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONArray data = new JSONArray(response);

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject user = data.getJSONObject(i);

                            if (username.equals(user.getString("from_username"))) {
                                requestsList.add(new FriendModel(
                                        user.getString("id"),
                                        user.getString("to_name"),
                                        user.getString("to_username"),
                                        user.getString("to_image")
                                ));
                            }
                            else if (username.equals(user.getString("to_username"))) {
                                requestsList.add(new FriendModel(
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
                    Toast.makeText(FriendRequestsActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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
        adapter = new RequestsAdapter(FriendRequestsActivity.this, requestsList, this);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void acceptRequest(String requestId) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.ACCEPT_REQUEST_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Accepting the request...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");

                        if (status.equals("1")) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            fetchRequestsList();
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
                data.put("id", requestId);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void rejectRequest(String requestId) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.REJECT_REQUEST_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Rejecting the request...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");

                        if (status.equals("1")) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            fetchRequestsList();
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
                data.put("id", requestId);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onAcceptClicked(FriendModel item) {
        acceptRequest(item.getId());
    }

    @Override
    public void onRejectClicked(FriendModel item) {
        rejectRequest(item.getId());
    }
}