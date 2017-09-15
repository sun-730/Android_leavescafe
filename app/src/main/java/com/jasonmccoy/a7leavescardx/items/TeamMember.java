package com.jasonmccoy.a7leavescardx.items;

import com.google.gson.Gson;

import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

public class TeamMember {

    public static final String TAG = TEST + TeamMember.class.getSimpleName();

    private String name;
    private String photoURL;
    private int redeemCount;
    private int stampCount;

    public TeamMember() {
    }

    public TeamMember(String name, String photoUrl, int redeemCount, int stampCount) {
        this.name = name;
        this.photoURL = photoUrl;
        this.redeemCount = redeemCount;
        this.stampCount = stampCount;
    }

    public static TeamMember getTeamMemberWithUpdateStamps(User user) {
        return new TeamMember(user.getName(), user.getPhotoURL(),
                user.getRedeemCount(), user.getStampCount() + 1);
    }

    public static TeamMember getTeamMember(User user) {
        return new TeamMember(user.getName(), user.getPhotoURL(),
                user.getRedeemCount(), user.getStampCount());
    }

    public String getName() {
        return name;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public int getRedeemCount() {
        return redeemCount;
    }

    public int getStampCount() {
        return stampCount;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static int updateMember(TeamMember member, TeamMember newValue) {
        int changesCount = 0;

        if (!member.name.equals(newValue.name)) {
            member.name = newValue.name;
            changesCount++;
        }

        if (!member.photoURL.equals(newValue.photoURL)) {
            member.photoURL = newValue.photoURL;
            changesCount++;
        }

        if (member.redeemCount != newValue.redeemCount) {
            member.redeemCount = newValue.redeemCount;
            changesCount++;
        }

        if (member.stampCount != newValue.stampCount) {
            member.stampCount = newValue.stampCount;
            changesCount++;
        }

        return changesCount;
    }
}
