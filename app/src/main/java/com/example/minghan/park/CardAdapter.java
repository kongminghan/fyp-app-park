package com.example.minghan.park;

import android.support.v7.widget.CardView;

/**
 * Created by MingHan on 3/26/2017.
 */

public interface CardAdapter {
    int MAX_ELEVATION_FACTOR = 8;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();
}
