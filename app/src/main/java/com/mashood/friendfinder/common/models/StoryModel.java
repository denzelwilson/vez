package com.mashood.friendfinder.common.models;

public class StoryModel {

    String id, username, name, place, time, profileImage, storyImage;

    public StoryModel(String id, String username, String name, String place, String time, String profileImage, String storyImage) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.place = place;
        this.time = time;
        this.profileImage = profileImage;
        this.storyImage = storyImage;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getPlace() {
        return place;
    }

    public String getTime() {
        return time;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getStoryImage() {
        return storyImage;
    }
}
