package com.example.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.heartlandpaymentsystems.library.terminals.enums.ConnectionMode;
import static com.example.app.Dialogs.showProgress;

public class BluetoothActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "BluetoothActivity";

    public static final String EXTRA_CONNECTION_MODE = "EXTRA_CONNECTION_MODE";

    private Button scanButton;
    private Button reconnectButton;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        connectionMode = (ConnectionMode) getIntent().getSerializableExtra(EXTRA_CONNECTION_MODE);

        scanButton = findViewById(R.id.scan_button);
        reconnectButton = findViewById(R.id.reconnect_button);
        connectButton = findViewById(R.id.connect_button);

        scanButton.setOnClickListener(this);
        reconnectButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);

        //check for previous bluetooth device
        if (bluetoothDeviceName != null && connectionMode == ConnectionMode.BLUETOOTH) {
            updateReconnectButton(true, bluetoothDeviceName);
        }

        if (MainActivity.c2XDevice != null && MainActivity.c2XDevice.isConnected()) {
            updateConnectionStatus("Connected - " + bluetoothDeviceName, false);
        } else if(MainActivity.mobyDevice != null && MainActivity.mobyDevice.isConnected()) {
            updateConnectionStatus("Connected - " + bluetoothDeviceName, false);
        } else {
            updateConnectionStatus(null, true);
        }

        //change the visible buttons for USB
        if (connectionMode == ConnectionMode.USB) {
            scanButton.setVisibility(View.GONE);
            reconnectButton.setVisibility(View.GONE);
            connectButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updated the connection status TextView and the connect Button.
     * @param status Text status to be displayed.
     * @param connectButtonEnabled Whether the connect button should be enabled/disabled.
     */
    @Override
    protected void updateConnectionStatus(String status, boolean connectButtonEnabled) {
        //update connection status
        super.updateConnectionStatus(status, connectButtonEnabled);
        //update the scan button
        scanButton.setEnabled(connectButtonEnabled);
        //update the reconnect button
        reconnectButton.setEnabled(connectButtonEnabled);
        //update the connect button
        connectButton.setEnabled(connectButtonEnabled);
    }

    /**
     * Update the reconnect button text and enabled status.
     * @param enabled
     * @param deviceName
     */
    private void updateReconnectButton(boolean enabled, String deviceName) {
        //update enabled
        reconnectButton.setEnabled(enabled);
        //update the text
        reconnectButton.setText(enabled ? getString(R.string.bluetooth_reconnect) + " (" + deviceName + ")" :
                getString(R.string.bluetooth_reconnect));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_button:
                if(MainActivity.c2XDevice != null) {
                    MainActivity.c2XDevice.initialize();
                } else {
                    MainActivity.mobyDevice.initialize();
                }

                //update connection status
                updateConnectionStatus("Scanning", false);
                showProgress(this, "Scanning", "Scanning for Bluetooth devices", null);
                break;
            case R.id.reconnect_button:
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE);
                String previousDevice = sharedPreferences.getString(MainActivity.BLUETOOTH_NAME, null);
                String previousAddress = sharedPreferences.getString(MainActivity.BLUETOOTH_ADDRESS, null);

                if(MainActivity.c2XDevice != null) {
                    MainActivity.c2XDevice.connect(previousAddress);
                } else {
                    MainActivity.mobyDevice.connect(previousAddress);
                }

                Log.d(TAG, "bluetooth device connect - " + previousDevice);

                //update connection status
                updateConnectionStatus("Connecting - " + previousDevice,
                        false);
                break;
            case R.id.connect_button:
                updateConnectionStatus("Connecting USB", false);
                showProgress(this, "Connecting", "Connecting to device using USB", null);
                MainActivity.mobyDevice.initialize();
                break;
        }
    }
}