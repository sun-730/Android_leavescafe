package com.jasonmccoy.a7leavescardx.ux;

public class BounceInterpolator implements android.view.animation.Interpolator {

    double mAmplitude = 1;

    double mFrequency = 10;

    public BounceInterpolator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    public float getInterpolation(float time) {
        double amplitude = mAmplitude;
        if (amplitude == 0) {
            amplitude = 0.05;
        }

        return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) * Math.cos(mFrequency * time) + 1);
    }
}
