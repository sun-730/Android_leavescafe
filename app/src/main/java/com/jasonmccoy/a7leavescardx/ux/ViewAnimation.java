package com.jasonmccoy.a7leavescardx.ux;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.jasonmccoy.a7leavescardx.R;

import java.io.IOException;

public class ViewAnimation {

    public static void animateView(final View view) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.bounce);
        animation.setDuration(600);

        BounceInterpolator interpolator = new BounceInterpolator(0.30, 20.0);
        animation.setInterpolator(interpolator);
        view.startAnimation(animation);

        playAudio(view.getContext());
    }

    private static void playAudio(Context context) {
        MediaPlayer mp = new MediaPlayer();
        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd("click.mp3");
            mp.setDataSource(descriptor.getFileDescriptor(),
                    descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mp.prepare();
            mp.start();
            mp.setVolume(15, 15);

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }
}
