package com.itopia.rowcontroller.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itopia.rowcontroller.R;
import com.itopia.rowcontroller.core.net.packet.AlprPacket;

public class ScanLayout extends LinearLayout implements View.OnClickListener {
    private TextView result;
    private Button scan;

    private ScanLayoutCallback callback;

    public ScanLayout(Context context) {
        this(context, null);
    }

    public ScanLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCallback(ScanLayoutCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        result = (TextView) findViewById(R.id.result);
        scan = (Button) findViewById(R.id.scan);
        scan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == scan) {
            callback.startAlprScan();
            scan.setText(R.string.scanning);
        }
    }

    public void onAlprResult(AlprPacket packet) {
        String fine = "€" + String.valueOf(packet.fine / 100.0);
        String text = "Result: " + packet.characters + "\naccuracy: " + packet.accuracy + "%";
        text += "\nowner: " + packet.owner + "\nfine: " + fine;
        result.setText(text);
        scan.setText(R.string.scan);
    }

    public interface ScanLayoutCallback {
        void startAlprScan();
    }
}
