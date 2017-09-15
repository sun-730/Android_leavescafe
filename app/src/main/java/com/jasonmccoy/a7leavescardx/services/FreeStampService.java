package com.jasonmccoy.a7leavescardx.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jasonmccoy.a7leavescardx.Helper;
import com.jasonmccoy.a7leavescardx.R;
import com.jasonmccoy.a7leavescardx.items.Stamp;
import com.jasonmccoy.a7leavescardx.items.User;

import java.util.Date;

import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_STAMP_COUNT;
import static com.jasonmccoy.a7leavescardx.services.FreeStampJob.FREE_STAMP_JOB_SCHEDULED;
import static com.jasonmccoy.a7leavescardx.services.FreeStampJob.FREE_STAMP_PREFERENCES;
import static com.jasonmccoy.a7leavescardx.services.FreeStampJob.scheduleFreeStampJob;

public class FreeStampService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences preferences = getSharedPreferences(FREE_STAMP_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(FREE_STAMP_JOB_SCHEDULED, false).apply();
        upDateStamps(1);
        scheduleFreeStampJob(this, User.getCurrentUser(getApplicationContext()).getBirthDay());
        return START_NOT_STICKY;
    }

    private void upDateStamps(final int i) {
        final DatabaseReference userReference = Helper.getUserReference(this);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User current = dataSnapshot.getValue(User.class);
                userReference
                        .child(DATABASE_NODE_USER_STAMP_COUNT)
                        .setValue(current.getStampCount() + i);


                userReference
                        .child("allStamps")
                        .push()
                        .setValue(new Stamp(i, Helper.getTime(new Date().getTime() / 1000,
                                "dd MMMM yyyy 'at' hh:mm:ss aaa")));

                displayNotification();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayNotification() {
        Notification notification = oneSenderNotification();
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;

        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, notification);
    }

    private Notification oneSenderNotification() {
        Bitmap remote_picture = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

        return new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_free_stamp)
                .setAutoCancel(true)
                .setLargeIcon(remote_picture)
                .setContentTitle(getString(R.string.free_stamp_notification_title))
                .setContentText(getString(R.string.free_stamp_notification_message))
                .build();
    }
}
