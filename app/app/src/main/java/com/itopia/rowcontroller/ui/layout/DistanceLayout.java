package com.itopia.rowcontroller.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itopia.rowcontroller.R;
import com.itopia.rowcontroller.core.net.RobotConnection;
import com.itopia.rowcontroller.core.net.packet.DistancePacket;
import com.itopia.rowcontroller.core.net.packet.DistanceRequestPacket;
import com.itopia.rowcontroller.ui.view.DistanceView;

public class DistanceLayout extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    private RobotConnection connection;

    private TextView distanceText;
    private SeekBar positionControl;
    private DistanceView distanceView;

    public DistanceLayout(Context context) {
        super(context);
    }

    public DistanceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DistanceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        distanceText = (TextView) findViewById(R.id.distance_text);
        positionControl = (SeekBar) findViewById(R.id.position_control);
        positionControl.setOnSeekBarChangeListener(this);
        distanceView = (DistanceView) findViewById(R.id.distance_view);
    }

    public void setRobotConnection(RobotConnection robotConnection) {
        this.connection = robotConnection;
    }

    public void onDistanceResult(DistancePacket packet) {
        distanceText.setText(packet.distance + "cm");
        distanceView.setData(packet);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        DistanceRequestPacket distanceRequest = new DistanceRequestPacket(progress / (float) seekBar.getMax());
        connection.queuePacket(distanceRequest);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
