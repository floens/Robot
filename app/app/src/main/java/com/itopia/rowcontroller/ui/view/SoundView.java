package com.itopia.rowcontroller.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itopia.rowcontroller.R;

public class SoundView extends LinearLayout {
    private TextView textView;

    public SoundView(Context context) {
        super(context);
    }

    public SoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SoundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        textView = (android.widget.TextView) findViewById(R.id.text);
    }

    public void setText(String text) {
        textView.setText(text);
    }
}
