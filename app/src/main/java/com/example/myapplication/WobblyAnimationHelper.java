package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class WobblyAnimationHelper {

    /**
     * Attaches a programmatic "Wobbly Slime" touch listener to a view.
     * This ensures the animation plays even if stateListAnimator is blocked.
     */
    public static void attachWobblyTouchListener(View view) {
        if (view == null) return;

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Immediate Squish
                    v.animate()
                            .scaleX(1.3f)
                            .scaleY(0.75f)
                            .setDuration(100)
                            .setInterpolator(null) // Linear/Fast
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Dramatic Wobble Back
                    AnimatorSet bounceSet = new AnimatorSet();
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f, 1.0f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 0.75f, 1.0f);
                    
                    scaleX.setDuration(600);
                    scaleY.setDuration(600);
                    
                    scaleX.setInterpolator(new OvershootInterpolator(4.0f)); // High tension
                    scaleY.setInterpolator(new OvershootInterpolator(4.0f));
                    
                    bounceSet.playTogether(scaleX, scaleY);
                    bounceSet.start();
                    

                    break;
            }
            return false; // Don't consume — let click/long-click listeners fire normally
        });
    }
}
