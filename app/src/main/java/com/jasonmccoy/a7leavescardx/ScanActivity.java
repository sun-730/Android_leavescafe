package com.jasonmccoy.a7leavescardx;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.jasonmccoy.a7leavescardx.items.Stamp;
import com.jasonmccoy.a7leavescardx.items.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_REDEEM_COUNT;
import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_STAMP_COUNT;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler,
        EasyPermissions.PermissionCallbacks {

    private static final String TAG = TEST + ScanActivity.class.getSimpleName();
    private static final int RC_CAMERA = 101;

    @BindView(R.id.content_frame)
    ViewGroup contentFrame;

    private FirebaseRemoteConfig remoteConfig;
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);


        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.activateFetched();

        mScannerView = new ZXingScannerView(this);
        mScannerView.setAutoFocus(true);
        setupFormats();
        contentFrame.addView(mScannerView);

        startScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    private void requestPermissions() {
        boolean camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED;

        if (camera) {
            String[] perms = {android.Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, perms, RC_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        String[] perms = {android.Manifest.permission.CAMERA};

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (!EasyPermissions.hasPermissions(this, perms)) {
                Toast.makeText(this, R.string.permission_not_granted_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @AfterPermissionGranted(RC_CAMERA)
    private void onReceivePermission() {
        String[] perms = {android.Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                    RC_CAMERA, perms);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);

        if (mScannerView != null) {
            mScannerView.setFormats(formats);
        }
    }


    private void startScanner() {
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        mScannerView.setAutoFocus(true);
    }

    @Override
    public void handleResult(Result rawResult) {
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        String scan = rawResult.getText();
        boolean qrMatches = false;
        String[] qr_map = getResources().getStringArray(R.array.qr_map);

        for (int i = 0, size = qr_map.length; i < size; i++) {
            if (remoteConfig.getString(qr_map[i]).equals(scan)) {
                upDateStamps(i);
                qrMatches = true;
                break;
            }
        }

        if (!qrMatches)
            showAlert(getString(R.string.scan_dialog_title), getString(R.string.scan_invalid_code));
    }

    private void upDateStamps(final int i) {
        final DatabaseReference userReference = Helper.getUserReference(this);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User current = dataSnapshot.getValue(User.class);
                int promotionStamp = getStampCountIfPromotion();


                if (i == 0) {
                    if (current.getStampCount() < 10) {
                        showAlert(getString(R.string.scan_dialog_title), getString(R.string.scan_not_enough_to_redeem));
                        return;
                    }

                    userReference
                            .child(DATABASE_NODE_USER_STAMP_COUNT)
                            .setValue(current.getStampCount() - 10);


                    userReference
                            .child(DATABASE_NODE_USER_REDEEM_COUNT)
                            .setValue(current.getRedeemCount() + 1);
                } else {
                    userReference
                            .child(DATABASE_NODE_USER_STAMP_COUNT)
                            .setValue(current.getStampCount() + i + promotionStamp);
                }

                userReference
                        .child("allStamps")
                        .push()
                        .setValue(new Stamp(i, Helper.getTime(new Date().getTime() / 1000,
                                "dd MMMM yyyy 'at' hh:mm:ss aaa")));


                if (promotionStamp != 0) {
                    showPromotionDialog(i + promotionStamp);
                    return;
                }

                Intent intent = new Intent();
                intent.setAction(i + "");
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int getStampCountIfPromotion() {
        String startDate = remoteConfig.getString("startDate");
        String endDate = remoteConfig.getString("endDate");

        DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        long start = 0;
        long end = 0;
        try {
            start = dataFormat.parse(startDate).getTime();
            end = dataFormat.parse(endDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        boolean hasStarted = (System.currentTimeMillis() - start) > 0;
        boolean hasEnded = (System.currentTimeMillis() - end) > 0;


        return hasStarted && !hasEnded ? 1 : 0;
    }

    private void showPromotionDialog(final int i) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.scan_promotion_dialog_title))
                .setMessage(getString(R.string.scan_promotion_dialog_message))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(i + "");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .setCancelable(false)
                .setIcon(R.drawable.ic_gift_flat)
                .show();
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mScannerView.resumeCameraPreview(ScanActivity.this);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel(null);
                    }
                })
                .setIcon(R.drawable.ic_qr_error)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @OnClick(R.id.cancel)
    public void cancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
