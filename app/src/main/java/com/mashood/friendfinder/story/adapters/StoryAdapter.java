package com.mashood.friendfinder.story.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.models.StoryModel;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.MyViewHolder> {

    ArrayList<StoryModel> data;
    Context c;
    LayoutInflater inflater;
    ClickListener listener;
    String fromPage;

    public StoryAdapter(Context c, ArrayList<StoryModel> data, ClickListener listener, String fromPage) {
        this.data = data;
        this.c = c;
        inflater = LayoutInflater.from(c);
        this.listener = listener;
        this.fromPage = fromPage;
    }

    public interface ClickListener {
        void onDeleteClicked(String id);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_story, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final StoryModel item = data.get(position);

        // Handle the visibility of DELETE
        if (fromPage.equals("MyStoriesActivity"))
            holder.btnDelete.setVisibility(View.VISIBLE);
        else
            holder.btnDelete.setVisibility(View.GONE);

        holder.tvName.setText(item.getName());
        holder.tvPlace.setText(item.getPlace());
        holder.tvTime.setText(item.getTime());

        if (!TextUtils.isEmpty(item.getProfileImage())) {
            if (!item.getProfileImage().isEmpty())
                Picasso.get()
                        .load(NetworkConfig.PROFILE_IMAGE_URL + item.getProfileImage())
                        .into(holder.imgProfile);
        }

        if (!TextUtils.isEmpty(item.getStoryImage())) {
            Glide.with(c)
                    .load(NetworkConfig.STORY_IMAGE_URL + item.getStoryImage())
                    .placeholder(R.drawable.img_no_img)
                    .error(R.drawable.img_no_img)
                    .into(holder.imgStory);
        }

        holder.btnDelete.setOnClickListener(view ->
                listener.onDeleteClicked(item.getId())
        );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPlace, tvTime;
        CircleImageView imgProfile;
        ImageView imgStory, btnDelete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvPlace = itemView.findViewById(R.id.tvPlace);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgStory = itemView.findViewById(R.id.imgStory);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
