package com.jasonmccoy.a7leavescardx.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jasonmccoy.a7leavescardx.Helper;
import com.jasonmccoy.a7leavescardx.events.ProfileUpdateEvent;
import com.jasonmccoy.a7leavescardx.items.User;

import org.greenrobot.eventbus.EventBus;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_PHOTO_URL;
import static com.jasonmccoy.a7leavescardx.AppClass.STORAGE_PROFILE_PIC_FOLDER;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;
import static com.jasonmccoy.a7leavescardx.LoginActivity.PROFILE_PHOTO_NAME;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_BIRTHDAY;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_EMAIL;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_GENDER;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_INFO_PREFERENCES;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_KEY;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_NAME;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_PHOTO_URL;
import static com.jasonmccoy.a7leavescardx.services.FreeStampJob.scheduleFreeStampJob;

@SuppressLint({"ApplySharedPref"})
public class SaveProfileService extends Service implements OnSuccessListener<UploadTask.TaskSnapshot> {

    private static final String TAG = TEST + SaveProfileService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_STICKY;
    }

    @SuppressWarnings({"ConstantConditions"})
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        Uri uri = intent.getData();

        Log.d(TAG, uri + "");

        String userName = intent.getStringExtra(USER_NAME);
        String email = intent.getStringExtra(USER_EMAIL);
        String gender = intent.getStringExtra(USER_GENDER);
        long birthDay = intent.getLongExtra(USER_BIRTHDAY, 0);

        SharedPreferences preferences = getSharedPreferences(USER_INFO_PREFERENCES, MODE_PRIVATE);

        preferences.edit()
                .putString(USER_NAME, userName)
                .putString(USER_EMAIL, email)
                .putString(USER_GENDER, gender)
                .putLong(USER_BIRTHDAY, birthDay)
                .commit();

        if (birthDay != 0) scheduleFreeStampJob(getApplicationContext(), birthDay);

        String userKey = preferences.getString(USER_KEY, null);
        if (userKey == null) throw new IllegalArgumentException("user key is null");


        Helper.updateProfile(userKey, userName, email, gender, birthDay);

        if (uri != null) try {
            saveImage(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    private void saveImage(Uri uri) throws FileNotFoundException {
        User user = User.getCurrentUser(getApplicationContext());
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReferenceFromUrl("gs://leaves-cafe.appspot.com/")
                .child(user.getKey())
                .child(STORAGE_PROFILE_PIC_FOLDER)
                .child(PROFILE_PHOTO_NAME);

        InputStream in = getContentResolver().openInputStream(uri);

        UploadTask uploadTask = imageRef.putStream(in);
        uploadTask.addOnSuccessListener(this);
    }

    @SuppressWarnings({"VisibleForTests", "ConstantConditions"})
    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        Uri uri = taskSnapshot.getDownloadUrl();
        getSharedPreferences(USER_INFO_PREFERENCES, MODE_PRIVATE).edit()
                .putString(USER_PHOTO_URL, uri.toString())
                .commit();

        //update user photo url
        Helper.getUserReference(getApplicationContext())
                .child(DATABASE_NODE_USER_PHOTO_URL)
                .setValue(uri.toString());

        Log.d(TAG, uri.toString());
        EventBus.getDefault().post(new ProfileUpdateEvent());
        stopSelf();
    }
}
