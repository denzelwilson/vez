package com.mashood.friendfinder.story;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.ProgressManager;
import com.mashood.friendfinder.common.SessionManager;
import com.mashood.friendfinder.common.models.FriendModel;
import com.mashood.friendfinder.common.models.StoryModel;
import com.mashood.friendfinder.databinding.ActivityMyStoriesBinding;
import com.mashood.friendfinder.databinding.ActivityStoriesListBinding;
import com.mashood.friendfinder.story.adapters.StoryAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyStoriesActivity extends AppCompatActivity implements StoryAdapter.ClickListener {

    private ActivityMyStoriesBinding binding;
    ArrayList<StoryModel> storiesList;
    String username;
    StoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyStoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        username = new SessionManager(this).getUserDetails().get("username");
        fetchStoriesList();

        binding.btnBack.setOnClickListener(view ->
                finish()
        );
    }

    private void fetchStoriesList() {
        storiesList = new ArrayList<>();
        ProgressManager.showSimpleProgressDialog(this, "Getting stories...", false);
        String url = NetworkConfig.BASE_URL + NetworkConfig.MY_STORY_LIST_ENDPOINT;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONArray data = new JSONArray(response);

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject story = data.getJSONObject(i);
                            storiesList.add(new StoryModel(
                                    story.getString("id"),
                                    story.getString("username"),
                                    story.getString("name"),
                                    story.getString("place"),
                                    story.getString("time"),
                                    story.getString("profile_image"),
                                    story.getString("image")
                            ));
                        }
                        // Show all friends in recyclerView
                        setupRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    ProgressManager.removeSimpleProgressDialog();
                    Toast.makeText(MyStoriesActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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

    private void deleteStory(String id) {
        String url = NetworkConfig.BASE_URL + NetworkConfig.DELETE_STORY_ENDPOINT;
        ProgressManager.showSimpleProgressDialog(this, "Deleting the story...", false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONObject data = new JSONObject(response);
                        String status = data.getString("status");
                        String message = data.getString("message");

                        if (status.equals("1")) {
                            fetchStoriesList();
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
                data.put("id", id);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void setupRecyclerView() {
        adapter = new StoryAdapter(MyStoriesActivity.this, storiesList, this, "MyStoriesActivity");
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onDeleteClicked(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this story?")
                .setPositiveButton("Delete", (dialogInterface, i) ->
                        deleteStory(id)
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}