package com.itopia.rowcontroller.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.itopia.rowcontroller.R;
import com.itopia.rowcontroller.core.net.RobotConnection;
import com.itopia.rowcontroller.core.net.packet.CommandPacket;

public class AdvancedOptionsLayout extends LinearLayout implements View.OnClickListener {
    public static final String TAG = "AdvancedOptionsLayout";

    private RobotConnection connection;

    private EditText commandText;
    private Button commandSend;
    private Button toggleAutoscan;

    public AdvancedOptionsLayout(Context context) {
        this(context, null);
    }

    public AdvancedOptionsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvancedOptionsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        commandText = (EditText) findViewById(R.id.command_text);
        commandSend = (Button) findViewById(R.id.command_send);
        commandSend.setOnClickListener(this);
        toggleAutoscan = (Button) findViewById(R.id.toggle_autoscan);
        toggleAutoscan.setOnClickListener(this);
    }

    public void setRobotConnection(RobotConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onClick(View v) {
        if (v == commandSend) {
            connection.queuePacket(new CommandPacket(commandText.getText().toString()));
        } else if (v == toggleAutoscan) {
            connection.queuePacket(new CommandPacket("toggle_autoscan"));
        }
    }
}
