package com.mashood.friendfinder.friends.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.mashood.friendfinder.R;
import com.mashood.friendfinder.common.NetworkConfig;
import com.mashood.friendfinder.common.models.FriendModel;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MyViewHolder> {

    ArrayList<FriendModel> data;
    Context c;
    LayoutInflater inflater;
    ClickListener listener;

    public interface ClickListener {
        void onChatClicked(String username);
    }

    public FriendsAdapter(Context c, ArrayList<FriendModel> data, ClickListener listener) {
        this.data = data;
        this.c = c;
        inflater = LayoutInflater.from(c);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_friend, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final FriendModel item = data.get(position);

        holder.lytButtonControls.setVisibility(View.GONE);

        holder.tvName.setText(item.getName());
        holder.tvUsername.setText(item.getUsername());

        if (!TextUtils.isEmpty(item.getImage())) {
            if (!item.getImage().isEmpty())
                Picasso.get()
                        .load(NetworkConfig.PROFILE_IMAGE_URL + item.getImage())
                        .into(holder.imgProfile);
        }

        holder.btnChat.setOnClickListener(v ->
                listener.onChatClicked(item.getUsername())
        );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvUsername;
        MaterialButton btnAccept, btnReject, btnChat;
        CircleImageView imgProfile;
        LinearLayout lytButtonControls;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnChat = itemView.findViewById(R.id.btnChat);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            lytButtonControls = itemView.findViewById(R.id.lytButtonControls);
        }
    }
}
