package com.itopia.rowcontroller.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static com.itopia.rowcontroller.AndroidUtils.dp;

public class MotorControlView extends View {
    private static final String TAG = "MotorControlView";

    private MotorControlViewCallback callback;

    private Slider speedSlider;
    private Slider directionSlider;

    public MotorControlView(Context context) {
        super(context);
        init();
    }

    public MotorControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MotorControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        speedSlider = new Slider(Slider.ORIENTATION_VERTICAL, false);
        directionSlider = new Slider(Slider.ORIENTATION_HORIZONTAL, false);
    }

    public void setCallback(MotorControlViewCallback callback) {
        this.callback = callback;
    }

    public Slider getSpeedSlider() {
        return speedSlider;
    }

    public Slider getDirectionSlider() {
        return directionSlider;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY && (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST)) {
            int width = MeasureSpec.getSize(widthMeasureSpec);

            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(width / 2, MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int sliderWidth = dp(50);

        canvas.save();

//        int offsetX = drawSize < getWidth() ? (getWidth() - drawSize) / 2 : 0;
//        int offsetY = drawSize < getHeight() ? (getHeight() - drawSize) / 2 : 0;

        speedSlider.setParameters((getWidth() / 2 / 2) - (sliderWidth / 2), 0, sliderWidth, getHeight(), 0, getHeight());
        speedSlider.draw(canvas);

        directionSlider.setParameters(getWidth() / 2, (getHeight() / 2) - (sliderWidth / 2), getWidth() / 2, sliderWidth, getHeight(), getHeight());
        directionSlider.draw(canvas);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        int id = event.getPointerId(event.getActionIndex());
//        Log.i(TAG, "getActionIndex = " + event.getActionIndex());

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
//                Log.i(TAG, "ACTION_{POINTER}_DOWN id = " + id);

                if (speedSlider.touchInterested(event)) {
                    return true;
                } else if (directionSlider.touchInterested(event)) {
                    return true;
                }

                return true;
            case MotionEvent.ACTION_MOVE:
//                Log.i(TAG, "ACTION_MOVE id = " + id);

                speedSlider.onTouchMove(event);
                directionSlider.onTouchMove(event);

                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
//                Log.i(TAG, "ACTION_{POINTER}_UP id = " + id);

                speedSlider.onTouchUp(event);
                directionSlider.onTouchUp(event);

                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
        }

        return super.onTouchEvent(event);
    }

    public class Slider {
        private static final int ORIENTATION_HORIZONTAL = 0;
        private static final int ORIENTATION_VERTICAL = 1;

        private Paint sliderPaint;
        private Paint cursorPaint;
        private int orientation;

        private boolean sticky;
        private int x;
        private int y;
        private int width;
        private int height;
        private int touchX;
        private int touchWidth;

        private float sliderProgress = 0.5f;

        private int pointerId = -1;

        private Rect drawRect = new Rect();

        public Slider(int orientation, boolean sticky) {
            this.orientation = orientation;
            this.sticky = sticky;

            sliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sliderPaint.setColor(0xff000000);

            cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cursorPaint.setColor(0xff428bca);
        }

        public boolean isDown() {
            return sticky || pointerId >= 0;
        }

        public float getSliderProgress() {
            return sliderProgress;
        }

        private void setParameters(int x, int y, int width, int height, int touchX, int touchWidth) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.touchX = touchX;
            this.touchWidth = touchWidth;
        }

        private void draw(Canvas canvas) {
            drawRect.set(x, y, x + width, y + height);
            canvas.drawRect(drawRect, sliderPaint);

            if (orientation == ORIENTATION_HORIZONTAL) {
                canvas.drawCircle(x + sliderProgress * width, y + height / 2, height / 2, cursorPaint);
            } else {
                canvas.drawCircle(x + width / 2, y + sliderProgress * height, width / 2, cursorPaint);
            }
        }

        private boolean touchInterested(MotionEvent event) {
            boolean interested = false;

            float x = event.getX(event.getActionIndex());
            if (pointerId < 0 && x >= touchX && x < touchX + touchWidth) {
                interested = true;
            }

            if (interested) {
                pointerId = getPointerId(event);
            }
            return interested;
        }

        private void onTouchMove(MotionEvent event) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                if (i == event.findPointerIndex(pointerId)) {
                    float lastProgress = sliderProgress;

                    if (orientation == ORIENTATION_HORIZONTAL) {
                        sliderProgress = clamp((event.getX(i) - x) / width);
                    } else {
                        sliderProgress = clamp((event.getY(i) - y) / height);
                    }

                    if (sliderProgress != lastProgress) {
                        onCursorChanged();
                    }
                }
            }
        }

        private boolean onTouchUp(MotionEvent event) {
            if (getPointerId(event) == pointerId) {
                pointerId = -1;
                if (!sticky) {
                    sliderProgress = 0.5f;
                }
                onCursorChanged();
                return true;
            } else {
                return false;
            }
        }

        private void onCursorChanged() {
            invalidate();
            callback.onCursorChanged(MotorControlView.this);
        }

        private int getPointerId(MotionEvent event) {
            return event.getPointerId(event.getActionIndex());
        }

        private float clamp(float value) {
            return Math.min(1f, Math.max(0f, value));
        }
    }

    public interface MotorControlViewCallback {
        void onCursorChanged(MotorControlView motorControlView);
    }
}
