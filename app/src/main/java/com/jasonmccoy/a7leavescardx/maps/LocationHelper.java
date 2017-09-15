package com.jasonmccoy.a7leavescardx.maps;

import android.Manifest;
import android.app.Application;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.jasonmccoy.a7leavescardx.events.UserLocationEvent;

import org.greenrobot.eventbus.EventBus;

import pub.devrel.easypermissions.EasyPermissions;

public class LocationHelper {

    @SuppressWarnings("MissingPermission")
    public static final void getLocation(LocationManager locationManager, final int action) {
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location Changes", location.toString());
                EventBus.getDefault().post(new UserLocationEvent(action, location.getLatitude(), location.getLongitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };


        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        locationManager.requestSingleUpdate(criteria, locationListener, null);
    }
}
