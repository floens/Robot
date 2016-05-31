package com.itopia.rowcontroller.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itopia.rowcontroller.R;
import com.itopia.rowcontroller.core.net.RobotConnection;
import com.itopia.rowcontroller.core.net.packet.CompassPacket;
import com.itopia.rowcontroller.core.net.packet.MotorCommandPacket;
import com.itopia.rowcontroller.ui.view.CompassView;
import com.itopia.rowcontroller.ui.view.ConnectionStatusView;
import com.itopia.rowcontroller.ui.view.MotorControlView;

public class ControllerLayout extends LinearLayout implements View.OnClickListener, MotorControlView.MotorControlViewCallback {
    private static final String TAG = "ControllerLayout";

    private WebView webView;
    private TextView status;
    private ConnectionStatusView connectionStatusView;
    private MotorControlView motorControlView;
    private CompassView compassView;

    private RobotConnection connection;

    public ControllerLayout(Context context) {
        super(context);
    }

    public ControllerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControllerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        webView = (WebView) findViewById(R.id.web_view);

        status = (TextView) findViewById(R.id.status);

        connectionStatusView = (ConnectionStatusView) findViewById(R.id.connection_status_view);

        motorControlView = (MotorControlView) findViewById(R.id.motor_control_view);
        motorControlView.setCallback(this);

        compassView = (CompassView) findViewById(R.id.compass_view);
    }

    public void setRobotConnection(RobotConnection connection) {
        this.connection = connection;
        connectionStatusView.setRobotConnection(connection);
    }

    @Override
    public void onClick(View v) {
    }

    public void onTcpConnected(boolean connected) {
        connectionStatusView.invalidate();
    }

    public void setCompassData(CompassPacket packet) {
        compassView.setCompassData(packet);
    }

    public WebView getCameraView() {
        return webView;
    }

    public void onDestroy() {
        connection.disconnect();
    }

    @Override
    public void onCursorChanged(MotorControlView motorControlView) {
        MotorCommandPacket command = new MotorCommandPacket();

        MotorControlView.Slider speed = motorControlView.getSpeedSlider();
        MotorControlView.Slider direction = motorControlView.getDirectionSlider();

        if (speed.isDown()) {
            command.speed = 1f - (speed.getSliderProgress() * 2f);
        } else {
            command.speed = 0f;
        }

        if (direction.isDown()) {
            command.direction = (direction.getSliderProgress() * 2f) - 1f;
        } else {
            command.direction = 0f;
        }

        connection.queuePacket(command);
    }
}
