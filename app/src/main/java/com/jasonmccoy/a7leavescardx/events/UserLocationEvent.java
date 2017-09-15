package com.jasonmccoy.a7leavescardx.events;

public class UserLocationEvent {

    private final int action;
    private final double latitude;
    private final double longitude;

    public UserLocationEvent(int action, double latitude, double longitude) {
        this.action = action;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getAction() {
        return action;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
