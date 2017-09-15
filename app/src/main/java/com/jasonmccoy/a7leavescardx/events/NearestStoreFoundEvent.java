package com.jasonmccoy.a7leavescardx.events;

import com.jasonmccoy.a7leavescardx.items.Store;

public class NearestStoreFoundEvent {
    private final int action;
    private final Store nearest;
    private final double userLat;
    private final double userLan;

    public NearestStoreFoundEvent(int action, Store nearest, double userLat, double userLan) {
        this.action = action;
        this.nearest = nearest;
        this.userLat = userLat;
        this.userLan = userLan;
    }

    public int getAction() {
        return action;
    }

    public Store getNearest() {
        return nearest;
    }

    public double getUserLat() {
        return userLat;
    }

    public double getUserLan() {
        return userLan;
    }
}
