package com.me94me.example_measure_resource;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 94me on 2018/8/6 15:53
 */
public class CustomView extends View {

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
    }

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect();
    }

    public void animateRect() {
        RectF rect = rect_in;
        if (checked) {
            rect.left = rect_out.right/2;
        }else{
            rect.left = 0;
        }
        // ObjectAnimator animator = ObjectAnimator.ofFloat(offset, "offset", offset);

        ObjectAnimator animator = ObjectAnimator.ofObject(rect_in, "rectIn", new TypeEvaluator() {
            @Override
            public Object evaluate(float v, Object o, Object t1) {
                return null;
            }
        }, rect);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(1000);
        animator.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){

        }
        return super.onTouchEvent(event);
    }
}
