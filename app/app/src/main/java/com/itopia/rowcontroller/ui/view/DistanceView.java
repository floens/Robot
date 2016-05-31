package com.itopia.rowcontroller.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.itopia.rowcontroller.core.net.packet.DistancePacket;

import static com.itopia.rowcontroller.AndroidUtils.dp;

public class DistanceView extends View {
    private float[] points;
    private Paint linePaint;
    private Paint textPaint;

    public DistanceView(Context context) {
        this(context, null);
    }

    public DistanceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DistanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xff0000ff);
        linePaint.setStrokeWidth(dp(5));
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xff000000);
//        textPaint.setTextSize(sp(12));

        points = new float[20];
    }

    public void setData(DistancePacket data) {
        int position = Math.max(0, Math.min(points.length - 1, (int) (data.position * (points.length - 1))));

        float distance = data.distance;
        if (distance == 0f) {
            distance = 500f;
        }

        points[position] = distance;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        for (int i = 1; i < points.length; i++) {
            float lx = ((i - 1) / (float) (points.length - 1)) * width;
            float x = (i / (float) (points.length - 1)) * width;
            float ly = points[i - 1];
            float y = points[i];

            canvas.drawLine(lx, ly, x, y, linePaint);

            canvas.drawText(String.valueOf(points[i]), x, 500f, textPaint);
        }
    }
}
