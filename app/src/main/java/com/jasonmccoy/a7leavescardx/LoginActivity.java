package com.jasonmccoy.a7leavescardx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.jasonmccoy.a7leavescardx.items.User;
import com.jasonmccoy.auth.AuthUI;

import java.util.Arrays;
import java.util.HashMap;

import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USERS;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = TEST + LoginActivity.class.getSimpleName();
    public static boolean isUserLogIn = false;

    public static final String USER_INFO_PREFERENCES = "USER_INFO_PREFERENCES";

    public static final String USER_KEY = "USER_KEY";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_EMAIL = "USER_EMAIL";
    public static final String USER_PHOTO_URL = "USER_PHOTO_URL";
    public static final String USER_GENDER = "USER_GENDER";
    public static final String USER_BIRTHDAY = "USER_BIRTHDAY";
    public static final String USER_REFERRAL_CODE = "USER_REFERRAL_CODE";

    public static final String PROFILE_PHOTO_NAME = "profilePic.jpeg";

    private static final int RC_SIGN_IN = 123;

    private SharedPreferences preferences;
    private boolean fromLogout = false;

    // TODO: use firebase url shorter
    // TODO: 15.02.2017 if the user changes the email update the profile two not just the database entries
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = getSharedPreferences(USER_INFO_PREFERENCES, MODE_PRIVATE);
        fromLogout = getIntent().getBooleanExtra("logout", false);


        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        FirebaseAuth.getInstance().removeAuthStateListener(loginListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        FirebaseAuth.getInstance().addAuthStateListener(loginListener());
    }

    public FirebaseAuth.AuthStateListener loginListener() {
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "loginListener");
                if (isUserLogIn) {
                    Log.d(TAG, "loginListener isUserLogIn true");
                    if (getIntent() != null){
                        if(fromLogout) finish();
                        else startApp();
//                        finish();
                    }
                    return;
                } else Log.d(TAG, "loginListener isUserLogIn false");


                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "loginListener user != null");
                    if (preferences.getString(USER_EMAIL, null) != null) {
                        Log.d(TAG, "userExistsInPreferences");
                        startApp();
                    } else Log.d(TAG, "userExistsInPreferences creating user");
                } else {
                    Log.d(TAG, "loginListener else");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setTheme(R.style.AuthTheme)
                                    .setLogo(R.drawable.ic_logo)
                                    .setIsSmartLockEnabled(true)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_CANCELED) {
            finish();
            return;
        }
        Log.d(TAG, "onActivityResult");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            onSignedInInitialize(user);
        }
    }

    private void onSignedInInitialize(final FirebaseUser user) {
        Log.d(TAG, "onSignedInInitialize");
        String displayName = user.getDisplayName();


        {
            // For this fo to RegisterEmailFragment in the auth module
            if (displayName == null || displayName.isEmpty()) {
                displayName = preferences.getString(USER_NAME, "");
            }
        }

        final String[] name = {displayName};
        final String[] email = {user.getEmail()};
        final String[] photoUrl = {user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : getString(R.string.default_user_icon)};

        Log.d(TAG, name[0] + "/");
        Log.d(TAG, email[0] + "/");
        Log.d(TAG, photoUrl[0] + "/");


        final DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference()
                .child(DATABASE_NODE_USERS);

        Helper.queryUserByEmail(email[0]).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange" + "/");
                String userKey;
                String gender = "";
                long birthDay = 0;
                int redeemCount = 0;
                String referralCode;
                int stampCount = 0;


                if (dataSnapshot.getChildrenCount() == 0) {
                    userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    referralCode = Helper.generateReferralCode();
                } else {
                    HashMap<String, User> map = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, User>>() {
                    });

                    userKey = map.keySet().iterator().next();

                    User user = map.get(userKey);
                    name[0] = user.getName();
                    email[0] = user.getEmail();
                    photoUrl[0] = user.getPhotoURL();
                    gender = user.getGender();
                    birthDay = user.getBirthDay();
                    stampCount = user.getStampCount();
                    referralCode = user.getReferralCode();
                    redeemCount = user.getRedeemCount();
                }

                Log.d(TAG, userKey + "/");
                Log.d(TAG, referralCode + "/");

                preferences.edit()
                        .putString(USER_KEY, userKey)
                        .putString(USER_NAME, name[0])
                        .putString(USER_EMAIL, email[0])
                        .putString(USER_PHOTO_URL, photoUrl[0])
                        .putString(USER_GENDER, gender)
                        .putLong(USER_BIRTHDAY, birthDay)
                        .putString(USER_REFERRAL_CODE, referralCode)
                        .apply();

                if (dataSnapshot.getChildrenCount() != 0) {
                    startApp();
                    return;
                }

                User user = new User(userKey, name[0], email[0], photoUrl[0], gender, birthDay,
                        stampCount, referralCode, redeemCount);

                usersReference.child(userKey).setValue(user);

                startApp();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void startApp() {
        Log.d(TAG, "startApp");
        isUserLogIn = true;
        startActivity(new Intent(this, MainActivity.class));
        FirebaseAuth.getInstance().removeAuthStateListener(loginListener());
        finish();
    }
}

