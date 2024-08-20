package com.mashood.friendfinder.story;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.mashood.friendfinder.databinding.ActivityStoriesListBinding;
import com.mashood.friendfinder.friends.FriendsListActivity;
import com.mashood.friendfinder.friends.adapters.FriendsAdapter;
import com.mashood.friendfinder.story.adapters.StoryAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoriesListActivity extends AppCompatActivity implements StoryAdapter.ClickListener {

    private ActivityStoriesListBinding binding;
    ArrayList<FriendModel> friendsList;
    ArrayList<StoryModel> storiesList;
    String username;
    StoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoriesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        username = new SessionManager(this).getUserDetails().get("username");
        fetchFriendsList();

        binding.btnBack.setOnClickListener(view ->
                finish()
        );
    }

    private void fetchFriendsList() {
        friendsList = new ArrayList<>();
        ProgressManager.showSimpleProgressDialog(this, "Getting stories info...", false);
        String url = NetworkConfig.BASE_URL + NetworkConfig.FRIENDS_LIST_ENDPOINT;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
//                    ProgressManager.removeSimpleProgressDialog();
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
                        // Call stories list API
                        fetchStoriesList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    ProgressManager.removeSimpleProgressDialog();
                    Toast.makeText(StoriesListActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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

    private void fetchStoriesList() {
        storiesList = new ArrayList<>();
        ProgressManager.showSimpleProgressDialog(this, "Getting stories...", false);
        String url = NetworkConfig.BASE_URL + NetworkConfig.STORY_LIST_ENDPOINT;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    ProgressManager.removeSimpleProgressDialog();
                    try {
                        JSONArray data = new JSONArray(response);

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject story = data.getJSONObject(i);

                            friendsList.forEach(friend -> {
                                try {
                                    if (friend.getUsername().equals(story.getString("username"))) {
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
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        // Show all friends in recyclerView
                        setupRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    ProgressManager.removeSimpleProgressDialog();
                    Toast.makeText(StoriesListActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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
        adapter = new StoryAdapter(StoriesListActivity.this, storiesList, this, "StoriesListActivity");
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onDeleteClicked(String id) {
        // Nothing to do here
    }
}
