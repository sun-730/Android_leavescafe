package com.jasonmccoy.a7leavescardx.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.Calendar;
import java.util.Date;

import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

public class FreeStampJob extends Job {

    private static final String TAG = TEST + FreeStampJob.class.getSimpleName();

    public static final String FREE_STAMP_JOB_SCHEDULED = "FREE_STAMP_JOB_SCHEDULED";
    public static final String FREE_STAMP_PREFERENCES = "FREE_STAMP_PREFERENCES";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        getContext().startService(new Intent(getContext(), FreeStampService.class));
        return Result.SUCCESS;
    }

    public static void scheduleFreeStampJob(Context context, long birthDay) {
        SharedPreferences preferences = context.getSharedPreferences(FREE_STAMP_PREFERENCES, Context.MODE_PRIVATE);
        boolean isFreeStampScheduled = preferences.getBoolean(FREE_STAMP_JOB_SCHEDULED, false);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(birthDay * 1000));
        long curentTime = new Date().getTime();

        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        long birthdayTime = calendar.getTimeInMillis();

        long delta = Math.abs(birthdayTime - curentTime);
        Log.d(TAG, delta + " /");

        if (!isFreeStampScheduled) {
            new JobRequest.Builder(TAG)
                    .setExact(delta)
                    .setPersisted(true)
                    .build()
                    .schedule();
            preferences.edit().putBoolean(FREE_STAMP_JOB_SCHEDULED, true).apply();
        }
    }
}
