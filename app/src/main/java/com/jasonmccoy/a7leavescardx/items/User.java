package com.jasonmccoy.a7leavescardx.items;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_BIRTHDAY;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_EMAIL;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_GENDER;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_INFO_PREFERENCES;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_KEY;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_NAME;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_PHOTO_URL;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_REFERRAL_CODE;

public class User {

    public static final String TAG = TEST + User.class.getSimpleName();

    private String key = "";
    private String name = "";
    private String email = "";
    private String photoURL = "";
    private String gender = "";
    private long birthDay = 0;
    private String referralCode = "";
    private int stampCount = 0;
    private int redeemCount = 0;
    private HashMap<String, TeamMember> teams = new HashMap<>();

    public User() {
    }

    public static User getCurrentUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_INFO_PREFERENCES, MODE_PRIVATE);
        String key = sharedPreferences.getString(USER_KEY, "");
        String name = sharedPreferences.getString(USER_NAME, "");
        String email = sharedPreferences.getString(USER_EMAIL, "");
        String photo = sharedPreferences.getString(USER_PHOTO_URL, "");
        String gender = sharedPreferences.getString(USER_GENDER, "");
        long birthDay = sharedPreferences.getLong(USER_BIRTHDAY, 0);
        String referralCode = sharedPreferences.getString(USER_REFERRAL_CODE, "");

        return new User(key, name, email, photo, gender, birthDay, referralCode);
    }

    public User(String key, String name, String email, String photoURL, String gender, long birthDay, String referralCode) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.photoURL = photoURL;
        this.gender = gender;
        this.birthDay = birthDay;
        this.referralCode = referralCode;
    }

    public User(String key, String name, String email, String photoURL, String gender, long birthDay,
                int stampCount, String referralCode, int redeemCount) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.photoURL = photoURL;
        this.gender = gender;
        this.birthDay = birthDay;
        this.stampCount = stampCount;
        this.referralCode = referralCode;
        this.redeemCount = redeemCount;
    }


    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getGender() {
        return gender;
    }

    public long getBirthDay() {
        return birthDay;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public int getStampCount() {
        return stampCount;
    }

    public int getRedeemCount() {
        return redeemCount;
    }

    public HashMap<String, TeamMember> getTeams() {
        return teams;
    }

    public ArrayList<TeamMember> getTeamArray() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

