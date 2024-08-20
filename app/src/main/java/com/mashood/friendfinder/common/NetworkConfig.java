package com.mashood.friendfinder.common;

public interface NetworkConfig {
    String BASE_URL = "http://grapestechs.com/friend_finder/";
    String PROFILE_IMAGE_URL = "http://grapestechs.com/friend_finder/images/";
    String STORY_IMAGE_URL = "http://grapestechs.com/friend_finder/stories/";

    String LOGIN_ENDPOINT = "login.php";
    String REGISTRATION_ENDPOINT = "registration.php";
    String PHOTO_UPLOAD_ENDPOINT = "upload_photo.php";
    String UPDATE_PROFILE_ENDPOINT = "update_profile.php";
    String UPDATE_PASSWORD_ENDPOINT = "update_password.php";
    String UPDATE_LOCATION_ENDPOINT = "update_location.php";
    String GET_USERS_LIST_ENDPOINT = "list_users.php";
    String GET_FRIENDS_COUNT_ENDPOINT = "count_friends.php";
    String GET_STORIES_COUNT_ENDPOINT = "count_stories.php";
    String FRIENDS_LIST_ENDPOINT = "list_friends.php";
    String REQUESTS_LIST_ENDPOINT = "list_requests.php";
    String ACCEPT_REQUEST_ENDPOINT = "accept_request.php";
    String REJECT_REQUEST_ENDPOINT = "reject_request.php";
    String REQUEST_FRIEND_ENDPOINT = "request_friend.php";
    String GET_USER_DATA_ENDPOINT = "get_users_data.php";
    String STORY_UPLOAD_ENDPOINT = "upload_story.php";
    String STORY_LIST_ENDPOINT = "list_stories.php";
    String MY_STORY_LIST_ENDPOINT = "list_my_stories.php";
    String DELETE_STORY_ENDPOINT = "delete_story.php";
}
