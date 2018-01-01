package com.flipkart.chatheads.container;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeadManager;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;
import com.flipkart.chatheads.arrangement.MaximizedArrangement;
import com.flipkart.chatheads.arrangement.MinimizedArrangement;

import static android.content.Context.WINDOW_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

/**
 * Created by kiran.kumar on 08/11/16.
 */

public class WindowManagerContainer extends FrameChatHeadContainer {
    /**
     * A transparent view of the size of chat head which capture motion events and delegates them to the real view (frame layout)
     * This view is required since window managers will delegate the touch events to the window beneath it only if they are outside the bounds.
     * {@link android.view.WindowManager.LayoutParams#FLAG_NOT_TOUCH_MODAL}
     */
    private View motionCaptureView;

    private int cachedHeight;
    private int cachedWidth;
    private WindowManager windowManager;
    private ChatHeadArrangement currentArrangement;
    private boolean motionCaptureViewAdded;
    private ClickListener onClickListener;
    private BroadcastReceiver receiver;

    public WindowManagerContainer(Context context) {
        super(context);
    }

    public void setOnClickListener(ClickListener onClickListener){
        this.onClickListener = onClickListener;

    }
    @Override
    public void onInitialized(ChatHeadManager manager) {
        super.onInitialized(manager);
        motionCaptureView = new MotionCaptureView(getContext());

        MotionCapturingTouchListener listener = new MotionCapturingTouchListener(){
            @Override
            public void onClick() {
                super.onClick();
                if(onClickListener!=null){
                    onClickListener.onClick();
                }
            }

            @Override
            public void onLongClick() {
                super.onLongClick();
                if(onClickListener!=null){
                    onClickListener.onLongClick();
                }
            }

            @Override
            public void onDoubleClick() {
                super.onDoubleClick();
                if(onClickListener!=null){
                    onClickListener.onDoubleClick();
                }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(onClickListener!=null){
                    onClickListener.onTouch(event);
                }
                return super.onTouch(v, event);

            }
        };
        motionCaptureView.setOnTouchListener(listener);
        registerReceiver(getContext());
    }

    public void registerReceiver(Context context) {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                HostFrameLayout frameLayout = getFrameLayout();
                if (frameLayout != null) {
                    frameLayout.minimize();
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    public WindowManager getWindowManager() {
        if (windowManager == null) {
            windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        }
        return windowManager;
    }

    protected void setContainerHeight(View container, int height) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.height = height;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected void setContainerWidth(View container, int width) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.width = width;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected WindowManager.LayoutParams getOrCreateLayoutParamsForContainer(View container) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) container.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = createContainerLayoutParams(false);
            container.setLayoutParams(layoutParams);
        }
        return layoutParams;
    }

    protected void setContainerX(View container, int xPosition) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.x = xPosition;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected int getContainerX(View container) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        return layoutParams.x;
    }


    protected void setContainerY(View container, int yPosition) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.y = yPosition;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected int getContainerY(View container) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        return layoutParams.y;
    }

    protected WindowManager.LayoutParams createContainerLayoutParams(boolean focusable) {
        int focusableFlag;
        if (focusable) {
            focusableFlag = FLAG_NOT_TOUCH_MODAL;
        } else {
            focusableFlag = FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE;
        }
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(MATCH_PARENT, MATCH_PARENT,
                TYPE_PHONE,
                focusableFlag,
                PixelFormat.TRANSLUCENT);
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        return layoutParams;
    }

    @Override
    public void addContainer(View container, boolean focusable) {
        WindowManager.LayoutParams containerLayoutParams = createContainerLayoutParams(focusable);
        addContainer(container, containerLayoutParams);
    }

    public void addContainer(View container, WindowManager.LayoutParams containerLayoutParams) {
        container.setLayoutParams(containerLayoutParams);
        getWindowManager().addView(container, containerLayoutParams);
    }

    @Override
    public void setViewX(View view, int xPosition) {
        super.setViewX(view, xPosition);
        if (view instanceof ChatHead) {
            boolean hero = ((ChatHead) view).isHero();
            if (hero && currentArrangement instanceof MinimizedArrangement) {
                setContainerX(motionCaptureView, xPosition);
                setContainerWidth(motionCaptureView, view.getMeasuredWidth());
            }
        }
    }

    @Override
    public void setViewY(View view, int yPosition) {
        super.setViewY(view, yPosition);
        if (view instanceof ChatHead && currentArrangement instanceof MinimizedArrangement) {
            boolean hero = ((ChatHead) view).isHero();
            if (hero) {
                setContainerY(motionCaptureView, yPosition);
                setContainerHeight(motionCaptureView, view.getMeasuredHeight());
            }
        }
    }

    @Override
    public void onArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement) {
        currentArrangement = newArrangement;
        if (oldArrangement instanceof MinimizedArrangement && newArrangement instanceof MaximizedArrangement) {
            // about to be maximized
            WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(motionCaptureView);
            layoutParams.flags |= FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE;
            windowManager.updateViewLayout(motionCaptureView, layoutParams);

            layoutParams = getOrCreateLayoutParamsForContainer(getFrameLayout());
            layoutParams.flags &= ~FLAG_NOT_FOCUSABLE; //add focusability
            layoutParams.flags &= ~FLAG_NOT_TOUCHABLE; //add focusability
            layoutParams.flags |= FLAG_NOT_TOUCH_MODAL;

            windowManager.updateViewLayout(getFrameLayout(), layoutParams);

            setContainerX(motionCaptureView, 0);
            setContainerY(motionCaptureView, 0);
            setContainerWidth(motionCaptureView, getFrameLayout().getMeasuredWidth());
            setContainerHeight(motionCaptureView, getFrameLayout().getMeasuredHeight());

        } else {
            // about to be minimized
            WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(motionCaptureView);
            layoutParams.flags |= FLAG_NOT_FOCUSABLE; //remove focusability
            layoutParams.flags &= ~FLAG_NOT_TOUCHABLE; //add touch
            layoutParams.flags |= FLAG_NOT_TOUCH_MODAL; //add touch
            windowManager.updateViewLayout(motionCaptureView, layoutParams);

            layoutParams = getOrCreateLayoutParamsForContainer(getFrameLayout());
            layoutParams.flags |= FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE;
            windowManager.updateViewLayout(getFrameLayout(), layoutParams);
        }
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, layoutParams);
        if (!motionCaptureViewAdded && getManager().getChatHeads().size() > 0) {
            addContainer(motionCaptureView, true);
            WindowManager.LayoutParams motionCaptureParams = getOrCreateLayoutParamsForContainer(motionCaptureView);
            motionCaptureParams.width = 0;
            motionCaptureParams.height = 0;
            windowManager.updateViewLayout(motionCaptureView,motionCaptureParams);
            motionCaptureViewAdded = true;
        }
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        if (getManager().getChatHeads().size() == 0) {
            windowManager.removeViewImmediate(motionCaptureView);
            motionCaptureViewAdded = false;
        }
    }

    private void removeContainer(View motionCaptureView) {
        windowManager.removeView(motionCaptureView);
    }

    public void destroy() {
        windowManager.removeViewImmediate(motionCaptureView);
        windowManager.removeViewImmediate(getFrameLayout());
        if(receiver!=null) {
            getContext().unregisterReceiver(receiver);
        }
    }


    protected class MotionCapturingTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public MotionCapturingTouchListener() {
            gestureDetector = new GestureDetector(getContext(), new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            event.offsetLocation(getContainerX(v), getContainerY(v));
            HostFrameLayout frameLayout = getFrameLayout();
            if (frameLayout != null) {
                return frameLayout.dispatchTouchEvent(event);
            } else {
                return false;
            }
        }


        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeUp() {
        }

        public void onSwipeDown() {
        }

        public void onClick() {
            Log.v("ChatHeadService", "onClick");

        }

        public void onDoubleClick() {
            Log.v("ChatHeadService", "onDoubleClick");
        }

        public void onLongClick() {
            Log.v("ChatHeadService", "onLongClick");
        }
        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onClick();
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                onDoubleClick();
                return super.onDoubleTap(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                onLongClick();
                super.onLongPress(e);
            }

            // Determines the fling velocity and then fires the appropriate swipe event accordingly
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeDown();
                            } else {
                                onSwipeUp();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

    }



    private class MotionCaptureView extends View {
        public MotionCaptureView(Context context) {
            super(context);
        }

    }
}
