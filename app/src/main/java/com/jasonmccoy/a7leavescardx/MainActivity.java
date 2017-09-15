package com.jasonmccoy.a7leavescardx;

import android.Manifest;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.jasonmccoy.a7leavescardx.events.FeedbackFinishEvent;
import com.jasonmccoy.a7leavescardx.events.NearestStoreFoundEvent;
import com.jasonmccoy.a7leavescardx.events.ProfileFinishEvent;
import com.jasonmccoy.a7leavescardx.events.ProfileUpdateEvent;
import com.jasonmccoy.a7leavescardx.events.RedeemFinishEvent;
import com.jasonmccoy.a7leavescardx.events.ReferFinishEvent;
import com.jasonmccoy.a7leavescardx.events.TeamFinishEvent;
import com.jasonmccoy.a7leavescardx.events.UserLocationEvent;
import com.jasonmccoy.a7leavescardx.fragments.FeedbackFragment;
import com.jasonmccoy.a7leavescardx.fragments.ProfileFragment;
import com.jasonmccoy.a7leavescardx.fragments.RedeemFragment;
import com.jasonmccoy.a7leavescardx.fragments.ReferFragment;
import com.jasonmccoy.a7leavescardx.fragments.TeamFragment;
import com.jasonmccoy.a7leavescardx.items.Store;
import com.jasonmccoy.a7leavescardx.items.User;
import com.jasonmccoy.a7leavescardx.maps.LocationHelper;
import com.jasonmccoy.a7leavescardx.maps.NearestStoreHelper;
import com.jasonmccoy.auth.AuthUI;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_INFO_PREFERENCES;
import static com.jasonmccoy.a7leavescardx.LoginActivity.isUserLogIn;
import static com.jasonmccoy.a7leavescardx.fragments.RedeemFragment.ARGS_REFERRAL_CODE;
import static com.jasonmccoy.a7leavescardx.maps.NearestStoreHelper.NEAREST_STORE_ACTION_CHECK_USER_POSITION;
import static com.jasonmccoy.a7leavescardx.maps.NearestStoreHelper.NEAREST_STORE_ACTION_OPEN_MAPS;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ResultCallback<AppInviteInvitationResult>, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = TEST + MainActivity.class.getSimpleName();
    private static final int RC_CAMERA_AND_LOCATION = 100;
    private static final int RC_SCAN_QR = 1;
    private static final int RC_SCAN_NFC = 2;

    private GoogleApiClient googleApiClient;
    private FragmentManager fragmentManager;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.stamps_top)
    LinearLayout stampTop;
    @BindView(R.id.stamps_bottom)
    LinearLayout stampBottom;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    private boolean mToolBarNavigationListenerIsRegistered = false;
    private boolean requestLocationOnly = false;
    private ActionBarDrawerToggle toggle;
    private ActionBar actionBar;
    private ProgressDialog progressDialog;

    private int lastStampCount = 0;
    private User user;

    ImageView userIcon;
    TextView userName;
    TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate");

        FirebaseRemoteConfig.getInstance().fetch();
        fragmentManager = getFragmentManager();

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View nav = navigationView.getHeaderView(0);

        userIcon = (ImageView) nav.findViewById(R.id.user_icon);
        userName = (TextView) nav.findViewById(R.id.user_name);
        userEmail = (TextView) nav.findViewById(R.id.user_email);
        user = User.getCurrentUser(this);

        setUpUser();
        connectToGoogle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == RC_SCAN_QR) {
            lastStampCount = Integer.parseInt(data.getAction());
            Log.d(TAG, lastStampCount + " ");

            Helper.setStamps(stampTop, stampBottom, user, lastStampCount);
            lastStampCount = 0;
        }else if(resultCode == RESULT_OK && requestCode == RC_SCAN_NFC) {
            lastStampCount = Integer.parseInt(data.getAction());
            Log.d(TAG, lastStampCount + " ");

            Helper.setStamps(stampTop, stampBottom, user, lastStampCount);
            lastStampCount = 0;
        }
    }

    private void requestPermissions() {
        boolean location = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED;
        boolean camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED;


        if (location) {
            Log.d(TAG, "requestPermissions" + Arrays.toString(getPermissionArray()));
            ActivityCompat.requestPermissions(this, getPermissionArray(), RC_CAMERA_AND_LOCATION);
        }

        if (camera) {
            Log.d(TAG, "requestPermissions" + Arrays.toString(getPermissionArray()));
            ActivityCompat.requestPermissions(this, getPermissionArray(), RC_CAMERA_AND_LOCATION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Log.d(TAG, "onRequestPermissionsResult" + Arrays.toString(getPermissionArray()));
            if (!EasyPermissions.hasPermissions(this, getPermissionArray())) {
                Toast.makeText(this, getString(R.string.permission_not_granted_toast), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }

        if (!EasyPermissions.hasPermissions(this, getPermissionArray())) {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                    RC_CAMERA_AND_LOCATION, getPermissionArray());
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestLocationOnly) {
            getNearestStore();
            return;
        }

        checkUserPosition();
    }

    private String[] getPermissionArray() {
        if (requestLocationOnly)
            return new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
        return new String[]{android.Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
    }

    private void setUpUser() {
        User currentUser = User.getCurrentUser(this);
        userName.setText(currentUser.getName());
        userEmail.setText(currentUser.getEmail());
        Helper.loadImage(this, currentUser.getPhotoURL(), userIcon);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Helper.getUserReference(MainActivity.this).addValueEventListener(userListener);
            }
        }, 1000);
    }

    public void connectToGoogle() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(AppInvite.API)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        AppInvite.AppInviteApi.getInvitation(googleApiClient, this, false).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull AppInviteInvitationResult appInviteInvitationResult) {
        String link = AppInviteReferral.getDeepLink(appInviteInvitationResult.getInvitationIntent());
        if (link == null) return;
        Log.d(TAG, link);
        link = link.substring(link.indexOf("=") + 1);
        displayRedeemFragment(link);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
            Helper.setStamps(stampTop, stampBottom, user, 0);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @OnClick(R.id.fab)
    public void startRedeemQR() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) checkUserPosition();
        else requestPermissions();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.ic_nav_profile:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_place, new ProfileFragment(), ProfileFragment.TAG)
                        .addToBackStack(ProfileFragment.TAG)
                        .commit();
                lockDrawer();
                break;
            case R.id.ic_nav_store:
//                NFCScan();
//                return;
                if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    getNearestStore();
                } else {
                    requestLocationOnly = true;
                    requestPermissions();
                }
                break;
            case R.id.ic_nav_refer:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_place, new ReferFragment(), ReferFragment.TAG)
                        .addToBackStack(ReferFragment.TAG)
                        .commit();
                lockDrawer();
                break;
            case R.id.ic_nav_redeem:
                displayRedeemFragment(null);
                break;
            case R.id.ic_nav_team:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_place, new TeamFragment(), TeamFragment.TAG)
                        .addToBackStack(TeamFragment.TAG)
                        .commit();
                lockDrawer();
                break;
            case R.id.ic_nav_feedback:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_place, new FeedbackFragment(), FeedbackFragment.TAG)
                        .addToBackStack(FeedbackFragment.TAG)
                        .commit();
                lockDrawer();
                break;
            case R.id.nav_log_out:
                logOutUser();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showBackButton(boolean show) {
        fab.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (!mToolBarNavigationListenerIsRegistered) {
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                mToolBarNavigationListenerIsRegistered = true;
            }
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            Helper.animateBars(toggle, drawer);
            actionBar.setDisplayHomeAsUpEnabled(false);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;
        }
    }


    private void displayRedeemFragment(String referralCode) {
        RedeemFragment redeemFragment = new RedeemFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_REFERRAL_CODE, referralCode);
        redeemFragment.setArguments(args);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_place, redeemFragment, RedeemFragment.TAG)
                .addToBackStack(RedeemFragment.TAG)
                .commit();
        lockDrawer();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void nearestStoreFound(UserLocationEvent event) {
        switch (event.getAction()) {
            case NEAREST_STORE_ACTION_OPEN_MAPS:
                new NearestStoreHelper(NEAREST_STORE_ACTION_OPEN_MAPS,
                        event.getLatitude(), event.getLongitude());
                break;

            case NEAREST_STORE_ACTION_CHECK_USER_POSITION:
                new NearestStoreHelper(NEAREST_STORE_ACTION_CHECK_USER_POSITION,
                        event.getLatitude(), event.getLongitude());
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void nearestStoreFound(NearestStoreFoundEvent event) {
        Store nearest = event.getNearest();

        switch (event.getAction()) {
            case NEAREST_STORE_ACTION_OPEN_MAPS:
                String uri = String.format(Locale.getDefault(), "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)",
                        event.getUserLat(), event.getUserLan(), "My Location", nearest.getLat(), nearest.getLan(), nearest.getIdentifier());

                Uri intentUri = Uri.parse(uri);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                startActivity(mapIntent);

                break;

            case NEAREST_STORE_ACTION_CHECK_USER_POSITION:

                boolean isUserNearStore = Helper.isUserNearStore(nearest.getRadius(), 0, 0,
                        event.getUserLat(), event.getUserLan());
                progressDialog.dismiss();
                if (isUserNearStore) {//test
                    Intent scan = new Intent(this, NFCActivity.class);
                    startActivityForResult(scan, RC_SCAN_NFC);
                } else {

                    Snackbar.make(coordinatorLayout, R.string.main_not_in_store_error, Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProfileUpdateEvent event) {
        setUpUser();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void removeProfileFragment(ProfileFinishEvent event) {
        unlockDrawer();
        fragmentManager.beginTransaction()
                .remove(fragmentManager.findFragmentByTag(ProfileFragment.TAG))
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void removeReferFragment(ReferFinishEvent event) {
        unlockDrawer();
        fragmentManager.beginTransaction()
                .remove(fragmentManager.findFragmentByTag(ReferFragment.TAG))
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void removeRedeemFragment(RedeemFinishEvent event) {
        unlockDrawer();
        fragmentManager.beginTransaction()
                .remove(fragmentManager.findFragmentByTag(RedeemFragment.TAG))
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void removeTeamFragment(TeamFinishEvent event) {
        unlockDrawer();
        fragmentManager.beginTransaction()
                .remove(fragmentManager.findFragmentByTag(TeamFragment.TAG))
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void removeFeedbackFragment(FeedbackFinishEvent event) {
        unlockDrawer();
        fragmentManager.beginTransaction()
                .remove(fragmentManager.findFragmentByTag(FeedbackFragment.TAG))
                .commit();
    }

    @OnClick({R.id.facebook_icon, R.id.facebook_text, R.id.instagram_icon, R.id.instagram_text})
    public void openSocial(View view) {
        String uri = "";
        switch (view.getId()) {
            case R.id.facebook_icon:
            case R.id.facebook_text:
                uri = "https://www.facebook.com/7LeavesCafe";
                break;

            case R.id.instagram_icon:
            case R.id.instagram_text:
                uri = "https://www.instagram.com/7leavescafe";
                break;
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(uri));
        startActivity(i);
    }

    private void getNearestStore() {
        LocationHelper.getLocation((LocationManager) getSystemService(LOCATION_SERVICE),
                NEAREST_STORE_ACTION_OPEN_MAPS);
        requestLocationOnly = false;
    }

    public void checkUserPosition() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.main_get_location_dialog_title));
        progressDialog.show();
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            LocationHelper.getLocation((LocationManager) getSystemService(LOCATION_SERVICE),
                    NEAREST_STORE_ACTION_CHECK_USER_POSITION);
        } else {
            requestPermissions();
        }
    }

    private void lockDrawer() {
        drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
        showBackButton(true);
    }

    private void unlockDrawer() {
        drawer.setDrawerLockMode(LOCK_MODE_UNLOCKED);
        showBackButton(false);

        if (KeyboardVisibilityEvent.isKeyboardVisible(this)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(drawer.getWindowToken(), 0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
        EventBus.getDefault().unregister(this);
    }


    private void logOutUser() {
        final SharedPreferences preferences = getSharedPreferences(USER_INFO_PREFERENCES, MODE_PRIVATE);
        Log.d(TAG, "logOutUser");
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        isUserLogIn = false;
                        preferences.edit().clear().apply();
                        logout();
                    }
                });
    }
    private void logout(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("logout", true);
        startActivity(intent);
        finish();

    }
    private void NFCScan(){
        Intent scan = new Intent(this, NFCActivity.class);
        startActivityForResult(scan, RC_SCAN_NFC);

    }
}
