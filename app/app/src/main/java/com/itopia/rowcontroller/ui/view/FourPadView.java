package com.itopia.rowcontroller.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static com.itopia.rowcontroller.AndroidUtils.dp;

public class FourPadView extends View {
    public static final String TAG = "FourPadView";

    public enum Hit {
        NONE, TOP, RIGHT, BOTTOM, LEFT
    }

    private Paint backgroundPaint;
    private Paint arrowPaint;
    private Path arrowPath;

    private FourPadViewCallback callback;

    private int spaceLeft;
    private int spaceTop;
    private int size;
    private Hit hit = Hit.NONE;

    public FourPadView(Context context) {
        super(context);
        init();
    }

    public FourPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FourPadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCallback(FourPadViewCallback callback) {
        this.callback = callback;
    }

    public Hit getHit() {
        return hit;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int minSize = Math.min(getWidth(), getHeight());
        spaceLeft = (getWidth() - minSize) / 2;
        spaceTop = (getHeight() - minSize) / 2;
        size = minSize / 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(spaceLeft, spaceTop);

        drawArrow(canvas, size, 0, size, 0f, hit == Hit.TOP);
        drawArrow(canvas, size * 2, size, size, 90f, hit == Hit.RIGHT);
        drawArrow(canvas, size, size * 2, size, 180f, hit == Hit.BOTTOM);
        drawArrow(canvas, 0, size, size, 270f, hit == Hit.LEFT);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getAction();
        Hit previousHit = hit;

        int x = (int) event.getX() - spaceLeft;
        int y = (int) event.getY() - spaceTop;

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            hit = Hit.NONE;
        } else {
            hit = hitTest(x, y);
        }

        if (hit != previousHit) {
            hitChanged();
        }

        return true;
    }

    private void hitChanged() {
        if (callback != null) {
            callback.onHitChanged(hit);
        }

        invalidate();
    }

    private Hit hitTest(int x, int y) {
        if (x > 0 && x < size) {
            // Left region
            if (y > size && y < size * 2) {
                return Hit.LEFT;
            }
        } else if (x > size && x < size * 2) {
            // Center
            if (y > 0 && y < size) {
                return Hit.TOP;
            } else if (y > size * 2 && y < size * 3) {
                return Hit.BOTTOM;
            }
        } else if (x > size * 2 && x < size * 3) {
            // Right
            if (y > size && y < size * 2) {
                return Hit.RIGHT;
            }
        }

        return Hit.NONE;
    }

    private void drawArrow(Canvas canvas, int left, int top, int size, float rotation, boolean highlight) {
        arrowPaint.setColor(highlight ? 0xFFC5C5C5 : 0xFFABABAB);

        canvas.save();
        canvas.rotate(rotation, left + (size / 2), top + (size / 2));

        canvas.drawRoundRect(left, top, left + size, top + size, dp(5), dp(5), backgroundPaint);
        canvas.drawRect(left, top + (size / 2), left + size, top + size, backgroundPaint);

        int padding = dp(10);
        arrowPath.rewind();
        arrowPath.moveTo(left + (size / 2), top + padding);
        arrowPath.lineTo(left + size - padding, top + (size / 2) + padding);
        arrowPath.lineTo(left + padding, top + (size / 2) + padding);
        arrowPath.close();

        canvas.drawPath(arrowPath, arrowPaint);
        canvas.restore();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFFDADADA);
        arrowPath = new Path();

        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public interface FourPadViewCallback {
        void onHitChanged(Hit hit);
    }
}
