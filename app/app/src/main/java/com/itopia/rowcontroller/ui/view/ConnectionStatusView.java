package com.itopia.rowcontroller.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.itopia.rowcontroller.core.net.RobotConnection;

import java.util.Map;

import static com.itopia.rowcontroller.AndroidUtils.dp;
import static com.itopia.rowcontroller.AndroidUtils.sp;

public class ConnectionStatusView extends View {
    private RobotConnection robotConnection;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect textRect = new Rect();

    private float interpolatedTime;

    public ConnectionStatusView(Context context) {
        super(context);
        init();
    }

    public ConnectionStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConnectionStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(0xffff0000);

        textPaint.setColor(0xff000000);
        textPaint.setTextSize(sp(14));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (robotConnection.isConnected()) {
            Map<Integer, Long> pongTimes = robotConnection.getPongTimes();
            if (pongTimes.size() > 0) {
                long firstPongTime = Long.MAX_VALUE;
                for (Map.Entry<Integer, Long> set : pongTimes.entrySet()) {
                    if (set.getValue() < firstPongTime) {
                        firstPongTime = set.getValue();
                    }
                }

                long time = System.currentTimeMillis() - firstPongTime;

                interpolatedTime += (time - interpolatedTime) * 0.001f;
            }

            invalidate();
        } else {
            interpolatedTime = 0f;
        }

        canvas.drawRect(0f, 0f, interpolatedTime * dp(1f), dp(18), paint);

        String msText = robotConnection.getLastPongTime() + "ms";
        textPaint.getTextBounds(msText, 0, msText.length(), textRect);
        canvas.drawText(msText, dp(2), dp(14), textPaint);
    }

    public void setRobotConnection(RobotConnection robotConnection) {
        this.robotConnection = robotConnection;
    }
}
