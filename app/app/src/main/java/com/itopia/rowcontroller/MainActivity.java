package com.itopia.rowcontroller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.itopia.rowcontroller.core.IOUtils;
import com.itopia.rowcontroller.core.net.RobotConnection;
import com.itopia.rowcontroller.core.net.packet.AlprPacket;
import com.itopia.rowcontroller.core.net.packet.AlprRequestPacket;
import com.itopia.rowcontroller.core.net.packet.CompassPacket;
import com.itopia.rowcontroller.core.net.packet.DistancePacket;
import com.itopia.rowcontroller.core.net.packet.SoundPacket;
import com.itopia.rowcontroller.core.settings.AppSettings;
import com.itopia.rowcontroller.ui.layout.AdvancedOptionsLayout;
import com.itopia.rowcontroller.ui.layout.ControllerLayout;
import com.itopia.rowcontroller.ui.layout.DistanceLayout;
import com.itopia.rowcontroller.ui.layout.ScanLayout;
import com.itopia.rowcontroller.ui.layout.SoundsLayout;

import java.util.ArrayList;
import java.util.List;

import static com.itopia.rowcontroller.AndroidUtils.dp;

public class MainActivity extends AppCompatActivity implements RobotConnection.Callback, ScanLayout.ScanLayoutCallback, SoundsLayout.Callback {
    private static final String TAG = "MainActivity";
    private RobotConnection connection;

    private Toolbar toolbar;
    private SectionsPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private List<String> logBuffer = new ArrayList<>();

    private MenuItem connectMenuItem;

    private ControllerLayout controllerLayout;
    private ScanLayout scanLayout;
    private DistanceLayout distanceLayout;
    private SoundsLayout soundsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connection = new RobotConnection(this);
        connection.setTcpAddress(AppSettings.tcpAddressHost.get(), AppSettings.tcpAddressPort.get());

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pagerAdapter = new SectionsPagerAdapter();

        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(100);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        controllerLayout = (ControllerLayout) LayoutInflater.from(this).inflate(R.layout.layout_controller, viewPager, false);
        controllerLayout.setRobotConnection(connection);

        scanLayout = (ScanLayout) LayoutInflater.from(this).inflate(R.layout.layout_scan, viewPager, false);
        scanLayout.setCallback(this);

        distanceLayout = (DistanceLayout) LayoutInflater.from(this).inflate(R.layout.layout_distance, viewPager, false);
        distanceLayout.setRobotConnection(connection);

        soundsLayout = (SoundsLayout) LayoutInflater.from(this).inflate(R.layout.layout_sounds, viewPager, false);
        soundsLayout.setCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        connectMenuItem = menu.findItem(R.id.menu_connect);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_address:
                openAddressDialog();
                return true;
            case R.id.menu_connect:
                if (!connection.isConnected()) {
                    connection.connect();
                } else {
                    connection.disconnect();
                }

                return true;
            case R.id.menu_advanced_options:
                openAdvancedOptionsDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void tcpConnected(boolean connected) {
        connectMenuItem.setTitle(connected ? R.string.disconnect : R.string.connect);

        controllerLayout.onTcpConnected(connected);

        Snackbar.make(findViewById(android.R.id.content), connected ? R.string.connected : R.string.disconnected, Snackbar.LENGTH_SHORT).show();

//        Toast.makeText(getContext(), connected ? R.string.connected : R.string.disconnected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCompass(CompassPacket packet) {
        controllerLayout.setCompassData(packet);
    }

    @Override
    public void showCamera(boolean show, String url) {
        WebView cameraView = controllerLayout.getCameraView();
        if (show) {
            String html = IOUtils.assetAsString(this, "camera.html");
            html = html.replace("__URL__", url);

            cameraView.loadData(html, "text/html", "UTF-8");
        } else {
            cameraView.loadUrl("about:blank");
        }
    }

    @Override
    public void startAlprScan() {
        connection.queuePacket(new AlprRequestPacket());
    }

    @Override
    public void onAlprResult(AlprPacket packet) {
        scanLayout.onAlprResult(packet);
    }

    @Override
    public void onDistanceResult(DistancePacket packet) {
        distanceLayout.onDistanceResult(packet);
    }

    @Override
    public void showStatus(String statusText) {
        logBuffer.add(0, statusText);
        while (logBuffer.size() > 5) {
            logBuffer.remove(logBuffer.size() - 1);
        }

        String total = "";
        for (String line : logBuffer) {
            total += line + "\n";
        }

//        status.setText(total);
    }

    @Override
    public void playSound(SoundsLayout.Sound sound) {
        connection.queuePacket(new SoundPacket(sound.name));
    }

    public void openAddressDialog() {
        String address = AppSettings.tcpAddressHost.get() + ":" + AppSettings.tcpAddressPort.get();

        LinearLayout container = new LinearLayout(this);
        container.setPadding(dp(24), dp(8), dp(24), 0);

        final EditText editText = new EditText(this);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
        editText.setText(address);
        editText.setHint(R.string.address_hint);
        editText.setSingleLine(true);
        editText.setSelection(editText.getText().length());

        container.addView(editText, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.menu_address)
                .setView(container)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, null)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String address = editText.getText().toString();
                        boolean success = false;
                        if (address.contains(":")) {
                            int colon = address.indexOf(":");
                            String host = address.substring(0, colon);
                            String portRaw = address.substring(colon + 1);
                            int port = -1;
                            try {
                                port = Integer.parseInt(portRaw);
                            } catch (NumberFormatException ignored) {
                            }

                            Log.i(TAG, "host = " + host + " port = " + port);

                            if (host.length() > 0 && port > 0 && port < 65536) {
                                AppSettings.tcpAddressHost.set(host);
                                AppSettings.tcpAddressPort.set(port);
                                connection.setTcpAddress(host, port);
                                success = true;
                            }
                        }
                        if (success) {
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.address_invalid, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void openAdvancedOptionsDialog() {
        AdvancedOptionsLayout layout = (AdvancedOptionsLayout) LayoutInflater.from(this).inflate(R.layout.layout_advanced_options, null);
        layout.setRobotConnection(connection);

        new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    public class SectionsPagerAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view;

            switch (position) {
                case 0:
                    view = controllerLayout;
                    break;
                case 1:
                    view = scanLayout;
                    break;
                case 2:
                    view = distanceLayout;
                    break;
                case 3:
                    view = soundsLayout;
                    break;
                default:
                    throw new IllegalStateException();
            }

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Control";
                case 1:
                    return "Scan";
                case 2:
                    return "Distance";
                case 3:
                    return "Sounds";
            }
            return null;
        }
    }
}
