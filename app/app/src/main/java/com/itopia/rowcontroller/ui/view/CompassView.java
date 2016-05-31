package com.itopia.rowcontroller.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.itopia.rowcontroller.core.net.packet.CompassPacket;

import static com.itopia.rowcontroller.AndroidUtils.dp;

public class CompassView extends View {
    private Paint linePaint;

    private CompassPacket compassData;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xff0000ff);
        linePaint.setStrokeWidth(dp(5));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setCompassData(CompassPacket compassData) {
        this.compassData = compassData;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (compassData != null) {
            canvas.save();

            float arrowSize = Math.min(getWidth(), getHeight()) * 0.10f;

            canvas.translate(getPaddingLeft(), getPaddingTop());
            float width = getWidth() - getPaddingLeft() - getPaddingRight();
            float height = getHeight() - getPaddingTop() - getPaddingBottom();
            float x = width / 2f;
            float y = height / 2f;

//            float radians = (float) (compassData.angle * (Math.PI / 180f));

//            float toX = (float) (x + Math.cos(radians) * lineLength);
//            float toY = (float) (y + Math.sin(radians) * lineLength);

            canvas.rotate(compassData.angle, x, y);

            canvas.drawLine(x, y, x, 0f, linePaint);
            canvas.drawLine(x, 0f, x - arrowSize, arrowSize, linePaint);
            canvas.drawLine(x, 0f, x + arrowSize, arrowSize, linePaint);

            canvas.restore();
        }
    }
}
