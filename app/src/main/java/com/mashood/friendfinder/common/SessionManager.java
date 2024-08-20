package com.mashood.friendfinder.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "Login";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String id, String name, String username, String password,
                                   String email, String phone, String gender, String image) {
        editor.putString("id", id);
        editor.putString("name", name);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.putString("gender", gender);
        editor.putString("image", image);
        editor.commit();
    }

    public void updateSession(String name, String email, String phone) {
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.commit();
    }

    public void updatePassword(String password) {
        editor.putString("password", password);
        editor.commit();
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put("id", pref.getString("id", null));
        user.put("name", pref.getString("name", null));
        user.put("username", pref.getString("username", null));
        user.put("password", pref.getString("password", null));
        user.put("email", pref.getString("email", null));
        user.put("phone", pref.getString("phone", null));
        user.put("gender", pref.getString("gender", null));
        user.put("image", pref.getString("image", null));
        return user;
    }

}
