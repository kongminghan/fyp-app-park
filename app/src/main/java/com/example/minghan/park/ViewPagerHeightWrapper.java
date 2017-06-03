package com.example.minghan.park;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by MingHan on 3/26/2017.
 */

public class ViewPagerHeightWrapper extends ViewPager {

    public ViewPagerHeightWrapper(Context context) {
        super(context);
    }

    public ViewPagerHeightWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View firstChild = getChildAt(0);
        firstChild.measure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(firstChild.getMeasuredHeight(), MeasureSpec.EXACTLY));
    }
}
