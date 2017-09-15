package com.jasonmccoy.a7leavescardx.items;

public class Stamp {

    private int stampCount;
    private String time;

    public Stamp(int stampCount, String time) {
        this.stampCount = stampCount;
        this.time = time;
    }

    public int getStampCount() {
        return stampCount;
    }

    public String getTime() {
        return time;
    }
}
