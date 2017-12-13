package com.flipkart.chatheads.container;

import android.view.MotionEvent;

/**
 * Created by ledat on 5/2/2017.
 */

public abstract class ClickListener {
    public abstract void onTouch(MotionEvent event);
    public abstract void onClick();
    public abstract void onLongClick();
    public abstract void onDoubleClick();
}
