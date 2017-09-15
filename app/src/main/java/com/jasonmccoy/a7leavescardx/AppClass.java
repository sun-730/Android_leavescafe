package com.jasonmccoy.a7leavescardx;

import android.app.Application;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.jasonmccoy.a7leavescardx.services.FreeStampJob;

public class AppClass extends Application {
    public static final String TEST = "test";
    public static final String DATABASE_NODE_USERS = "users";

    public static final String STORAGE_PROFILE_PIC_FOLDER = "profilePics";

    public static final String DATABASE_NODE_USER_NAME = "name";
    public static final String DATABASE_NODE_USER_EMAIL = "email";
    public static final String DATABASE_NODE_USER_PHOTO_URL = "photoURL";
    public static final String DATABASE_NODE_USER_GENDER = "gender";
    public static final String DATABASE_NODE_USER_BIRTHDAY = "birthDay";
    public static final String DATABASE_NODE_USER_REFERRAL_CODE = "referralCode";
    public static final String DATABASE_NODE_USER_STAMP_COUNT = "stampCount";
    public static final String DATABASE_NODE_USER_REDEEM_COUNT = "redeemCount";
    public static final String DATABASE_NODE_USER_TEAMS = "teams";

    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new JobCreator() {
            @Override
            public Job create(String tag) {
                return new FreeStampJob();
            }
        });
    }
}
