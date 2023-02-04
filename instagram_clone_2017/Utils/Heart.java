package com.example.instagram_clone_2017.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Heart {
    private static final String TAG = "HEARTH";

    public ImageView heartWhite, hearthRed;

    private static final DecelerateInterpolator DECCELORATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    public Heart(ImageView heartWhite, ImageView hearthRed) {
        this.heartWhite = heartWhite;
        this.hearthRed = hearthRed;
    }

    public void toggleLike()
    {
        Log.d(TAG, "toggleLike: toggling hearth");
        AnimatorSet animationSet = new AnimatorSet();

        if (hearthRed.getVisibility() == View.VISIBLE)
        {
            Log.d(TAG, "toggleLike: tooglling heart red off.");
            hearthRed.setScaleX(0.1f);
            hearthRed.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(hearthRed, "scaleY", 1f, 0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(hearthRed, "scaleX", 1f, 0f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

            hearthRed.setVisibility(View.GONE);
            heartWhite.setVisibility(View.VISIBLE);

            animationSet.playTogether(scaleDownY, scaleDownX);

        }

        else if (hearthRed.getVisibility() == View.GONE)
        {
            Log.d(TAG, "toggleLike: tooglling heart red on.");
            hearthRed.setScaleX(0.1f);
            hearthRed.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(hearthRed, "scaleY", 0.1f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECCELORATE_INTERPOLATOR);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(hearthRed, "scaleX", 0.1f, 1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECCELORATE_INTERPOLATOR);

            hearthRed.setVisibility(View.VISIBLE);
            heartWhite.setVisibility(View.GONE);

            animationSet.playTogether(scaleDownY, scaleDownX);
        }

        animationSet.start();

    }
}
