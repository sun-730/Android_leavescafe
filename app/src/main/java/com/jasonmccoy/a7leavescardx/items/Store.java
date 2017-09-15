package com.jasonmccoy.a7leavescardx.items;

import com.google.gson.Gson;

import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

public class Store {

    public static final String TAG = TEST + Store.class.getSimpleName();

    private String identifier;
    private double longitude;
    private double latitude;
    private String note;
    private int radius;

    public Store() {

    }

    public Store(String identifier, double longitude, double latitude, String note, int radius) {
        this.identifier = identifier;
        this.longitude = longitude;
        this.latitude = latitude;
        this.note = note;
        this.radius = radius;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getIdentifier() {
        return identifier;
    }

    public double getLan() {
        return longitude;
    }

    public double getLat() {
        return latitude;
    }

    public String getNote() {
        return note;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
