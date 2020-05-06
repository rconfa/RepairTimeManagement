package com.technobit.repair_timer.ui.customize.preference;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class RipPreference extends Preference {
    boolean toRip;

    public RipPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RipPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RipPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RipPreference(Context context) {
        super(context);
    }

    public void setToRip(boolean toRip) {
        this.toRip = toRip;
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // if the summary is empty I highlight the preference
        if(toRip) {

            final ValueAnimator colorAnimation = new ValueAnimator();
            colorAnimation.setIntValues(Color.WHITE, Color.parseColor("#ddeaf6"));
            colorAnimation.setEvaluator(new ArgbEvaluator());
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    holder.itemView.setBackgroundColor((int) animator.getAnimatedValue());
                }
            });
            colorAnimation.setDuration(500); // milliseconds
            colorAnimation.setRepeatCount(ValueAnimator.REVERSE);
            colorAnimation.start();
        }
        else{
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void deleteRip(){
        this.toRip = false;
        notifyChanged();
    }
}
