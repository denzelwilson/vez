package com.mashood.friendfinder.common.models;

public class UserModel {

    String id, name, username, image, latitude, longitude;

    public UserModel(String id, String name, String username, String image, String latitude, String longitude) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
