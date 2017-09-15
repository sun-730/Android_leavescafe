package com.jasonmccoy.a7leavescardx;

import android.animation.ValueAnimator;
import android.content.Context;
import android.location.Location;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jasonmccoy.a7leavescardx.items.TeamMember;
import com.jasonmccoy.a7leavescardx.items.User;
import com.jasonmccoy.a7leavescardx.ux.ViewAnimation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USERS;
import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_BIRTHDAY;
import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_EMAIL;
import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_GENDER;
import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_NAME;
import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_REFERRAL_CODE;
import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_TEAMS;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

public class Helper {

    private static final String TAG = TEST + Helper.class.getSimpleName();

    public static void loadImage(Context context, String uri, ImageView imageView) {
        Glide.with(context).load(uri).into(imageView);
    }

    public static DatabaseReference getUserReference(Context context) {
        return FirebaseDatabase.getInstance().getReference()
                .child(DATABASE_NODE_USERS)
                .child(User.getCurrentUser(context).getKey());
    }

    public static DatabaseReference getUserReferenceByKey(String key) {
        return FirebaseDatabase.getInstance().getReference()
                .child(DATABASE_NODE_USERS)
                .child(key);
    }

    public static Query queryUserByReferralCode(String referralCode) {
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child(DATABASE_NODE_USERS);
        return usersReference.orderByChild(DATABASE_NODE_USER_REFERRAL_CODE).equalTo(referralCode.trim());
    }

    public static Query queryUserByEmail(String email) {
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child(DATABASE_NODE_USERS);
        return usersReference.orderByChild(DATABASE_NODE_USER_EMAIL).equalTo(email);
    }

    public static void addUserToTeam(String userWhoReferredKey, String userToAddKey, TeamMember member) {
        FirebaseDatabase.getInstance().getReference()
                .child(DATABASE_NODE_USERS)
                .child(userWhoReferredKey)
                .child(DATABASE_NODE_USER_TEAMS)
                .child(userToAddKey)
                .setValue(member);
    }

    public static String generateReferralCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    public static void updateProfile(String userKey, String userName, String email, String gender, long birthDay) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(DATABASE_NODE_USERS)
                .child(userKey);

        userRef.child(DATABASE_NODE_USER_NAME).setValue(userName);
        userRef.child(DATABASE_NODE_USER_EMAIL).setValue(email);
        userRef.child(DATABASE_NODE_USER_GENDER).setValue(gender);
        userRef.child(DATABASE_NODE_USER_BIRTHDAY).setValue(birthDay);
    }


    public static void setStamps(LinearLayout top, LinearLayout bottom, User user, int lastStampCount) {
        if (user == null) return;
        int redeem = user.getRedeemCount();
        int stamp = Math.min(user.getStampCount(), 10);

        int emptyImage = redeem == 0 ? R.drawable.ic_tier1 : redeem == 1 ? R.drawable.ic_tier2 : R.drawable.ic_tier3;
        final int fullImage = redeem == 0 ? R.drawable.ic_tier1_full : redeem == 1 ? R.drawable.ic_tier2_full : R.drawable.ic_tier3_full;

        ArrayList<ImageView> imageViews = new ArrayList<>();

        for (int i = 0, size = top.getChildCount(); i < size; i++) {
            imageViews.add((ImageView) top.getChildAt(i));
        }

        for (int i = 0, size = bottom.getChildCount(); i < size; i++) {
            imageViews.add((ImageView) bottom.getChildAt(i));
        }

        for (int i = 0, size = 10; i < size; i++) {
            imageViews.get(i).setImageResource(emptyImage);
        }

        Log.d(TAG, lastStampCount + " ");

        if (lastStampCount == 0) {
            for (int i = 0; i < stamp; i++) imageViews.get(i).setImageResource(fullImage);
        } else {
            int stampsUntilAnimate = stamp - lastStampCount;
            int delay = 1500;

            Log.d(TAG, "stampsUntilAnimate " + stampsUntilAnimate);

            for (int i = 0; i < stamp; i++) {
                final ImageView imageView = imageViews.get(i);
                if (i > stampsUntilAnimate - 1) {
                    imageView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ViewAnimation.animateView(imageView);
                            imageView.setImageResource(fullImage);
                        }
                    }, delay);
                    delay += 300;
                } else {
                    imageView.setImageResource(fullImage);
                }
            }
        }
    }

    public static long getUnixStamp(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        return c.getTimeInMillis() / 1000;
    }

    public static String getTime(long time, String format) {
        String a = new SimpleDateFormat(format, Locale.getDefault())
                .format(new Date(time * 1000));

        Log.d(TAG, a);
        return a;
    }

    public static String getTime(long time) {
        return getTime(time, "MMMM dd, yyyy");
    }

    public static boolean isUserNearStore(int radius, double centerLatitude, double centerLongitude,
                                          double testLatitude, double testLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(centerLatitude, centerLongitude, testLatitude, testLongitude, results);
        float distanceInMeters = results[0];
        return distanceInMeters < radius;
    }

    public static void animateBars(final ActionBarDrawerToggle toggle, final DrawerLayout drawer) {
        ValueAnimator anim = ValueAnimator.ofFloat(1, 0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                toggle.onDrawerSlide(drawer, slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        anim.start();
    }

    public static boolean isValidEmail(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
