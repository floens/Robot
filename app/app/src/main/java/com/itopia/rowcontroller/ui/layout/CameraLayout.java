package com.itopia.rowcontroller.ui.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import static com.itopia.rowcontroller.AndroidUtils.dp;

public class CameraLayout extends WebView {
    public static final int MAX_WIDTH = 250;

    public CameraLayout(Context context) {
        this(context, null);
    }

    public CameraLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public CameraLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getSettings().setJavaScriptEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            int width = widthSize;
            width = Math.min(width, dp(MAX_WIDTH));
            int height = (int) (width * 3f / 4f);
            int leftPadding = (int) Math.max(0f, (widthSize - width) / 2f);
//            setPadding(leftPadding, 0, 0, 0);
//            invalidate();
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
