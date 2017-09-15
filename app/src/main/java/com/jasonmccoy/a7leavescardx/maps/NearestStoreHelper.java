package com.jasonmccoy.a7leavescardx.maps;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jasonmccoy.a7leavescardx.events.NearestStoreFoundEvent;
import com.jasonmccoy.a7leavescardx.items.Store;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NearestStoreHelper implements ValueEventListener {

    public static final int NEAREST_STORE_ACTION_OPEN_MAPS = 100;
    public static final int NEAREST_STORE_ACTION_CHECK_USER_POSITION = 101;

    private ArrayList<Store> phoneNumbers = new ArrayList<>();

    private final int action;
    private final double lat;
    private final double lan;

    public NearestStoreHelper(int action, double lat, double lon) {
        this.action = action;
        this.lat = lat;
        this.lan = lon;
        getNearestStore();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        ArrayList<Store> stores = getStoreLocations(dataSnapshot, lat, lan);
        Store nearest = stores.get(0);
        Log.d("nearest", nearest.toString());
        EventBus.getDefault().post(new NearestStoreFoundEvent(action, nearest, lat, lan));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    private void getNearestStore() {
        DatabaseReference storeReference = FirebaseDatabase.getInstance().getReference("stores");
        storeReference.addListenerForSingleValueEvent(this);
    }


    private ArrayList<Store> getStoreLocations(DataSnapshot stores, final double lat, final double lon) {

        for (DataSnapshot dsp : stores.getChildren()) {
            Store store = dsp.getValue(Store.class);
            double long1 = (double)dsp.child("longitude").getValue();
            double lati1 = (double)dsp.child("latitude").getValue();
            store.setLatitude(long1);
            store.setLongitude(lati1);
            phoneNumbers.add(store);
            Log.d("test", store.toString());
        }

        Collections.sort(phoneNumbers, new Comparator<Store>() {
            @Override
            public int compare(Store a, Store b) {
                double aDist = distance(a.getLat(), a.getLan(), lat, lon);
                double bDist = distance(b.getLat(), b.getLan(), lat, lon);
                return (int) (aDist - bDist);
            }
        });

        return phoneNumbers;
    }

    private static double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon / 2), 2)));
        return radius * angle;
    }

}
