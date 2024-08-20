package com.mashood.friendfinder.common.models;

public class FriendModel {

    String id, name, username, image;

    public FriendModel(String id, String name, String username, String image) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getImage() {
        return image;
    }
}
