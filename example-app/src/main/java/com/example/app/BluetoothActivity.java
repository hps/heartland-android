package com.example.app;

import static com.example.app.Dialogs.hideProgress;
import static com.example.app.Dialogs.showProgress;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.heartlandpaymentsystems.library.terminals.DeviceListener;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BluetoothActivity";

    private TextView connectionStatusTextView;
    private Button connectButton;
    private Button reconnectButton;
    private String bluetoothDeviceName = null;
    private String bluetoothDeviceAddress = null;
    private BluetoothDevice previousSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        connectionStatusTextView = findViewById(R.id.c2x_connection_status);
        connectButton = findViewById(R.id.connect_button);
        reconnectButton = findViewById(R.id.reconnect_button);

        connectButton.setOnClickListener(this);
        reconnectButton.setOnClickListener(this);

        //check for previous bluetooth device
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE);
        bluetoothDeviceName = sharedPreferences.getString(MainActivity.BLUETOOTH_NAME, null);
        Log.e("test", "saved BT device: " + bluetoothDeviceName);
        if (bluetoothDeviceName != null) {
            updateReconnectButton(true, bluetoothDeviceName);
        }

        if (MainActivity.c2XDevice.isConnected()) {
            updateConnectionStatus("Connected - " + bluetoothDeviceName, false);
        }

        MainActivity.c2XDevice.setDeviceListener(new DeviceListener() {
            @Override
            public void onBluetoothDeviceFound(BluetoothDevice bluetoothDevice) {
                Log.d(TAG, "onBluetoothDeviceFound - " + bluetoothDevice.getName());
            }

            @Override
            public void onBluetoothDeviceList(final HashSet<BluetoothDevice> deviceList) {
                hideProgress();
                // receive list of available bluetooth devices
                // present to user for selection/confirmation
                if (deviceList == null || deviceList.isEmpty()) {
                    return;
                }

                Log.e(TAG, "onBluetoothDeviceList - size " + deviceList.size());

                //Use this flag is the list of bluetooth keep popping up with the list of bluetooth.
                if (previousSelected != null) {
                    return;
                }

                // receive list of available bluetooth devices
                // present to user for selection/confirmation
                final List<BluetoothDevice> list = new ArrayList<BluetoothDevice>(deviceList);
                final List<String> btList = new ArrayList<String>();

                for (BluetoothDevice dev : list) {
                    if(dev.getName() != null) {
                        btList.add(dev.getName().trim());
                    }
                }

                //This is a dialog to show the list of devices
                //Here we use a dialog with a callback and a listener but you can use your own user selection
                AlertDialog d = Dialogs.showListDialog("Available devices",
                        BluetoothActivity.this, btList.toArray(new String[0]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                BTSelection.getInstance().Selection(list.get(i), new BTSelection.SelectionCallback() {
                                    @Override
                                    public void onSelection(BluetoothDevice btDevice) {
                                        //keep track of current device info
                                        bluetoothDeviceName = btDevice.getName();
                                        bluetoothDeviceAddress = btDevice.getAddress();
                                        dialogInterface.dismiss();
                                        if (selectionListener != null) {
                                            previousSelected = btDevice;
                                            selectionListener.onSelection(btDevice);
                                        }
                                    }
                                });
                            }
                        });
            }

            @Override
            public void onConnected(final TerminalInfo terminalInfo) {
                Log.d(TAG, "onConnected");

                //update connection status
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateConnectionStatus("Connected - " + terminalInfo.getSerialNumber(),
                                false);
                    }
                });

                //save the bluetooth data
                SharedPreferences.Editor editor = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE).edit();
                editor.putString(MainActivity.BLUETOOTH_NAME, bluetoothDeviceName);
                if (bluetoothDeviceAddress != null) {
                    editor.putString(MainActivity.BLUETOOTH_ADDRESS, bluetoothDeviceAddress);
                }
                editor.commit();
            }

            @Override
            public void onError(Error error) {
                Log.e(TAG, "onError - " + error.toString());

                //update connection status
                updateConnectionStatus("Error - " + error.getMessage(), true);
            }

            @Override
            public void onTerminalInfoReceived(TerminalInfo terminalInfo) {
                Log.d(TAG, "onTerminalInfoReceived");
            }
        });
    }

    /**
     * Updated the connection status TextView and the connect Button.
     * @param status Text status to be displayed.
     * @param connectButtonEnabled Whether the connect button should be enabled/disabled.
     */
    private void updateConnectionStatus(String status, boolean connectButtonEnabled) {
        //update connection status
        connectionStatusTextView.setText(status);
        //update the connect button
        connectButton.setEnabled(connectButtonEnabled);
        //update the reconnect button
        reconnectButton.setEnabled(connectButtonEnabled);
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
            case R.id.connect_button:
                MainActivity.c2XDevice.initialize();

                //update connection status
                updateConnectionStatus("Scanning", false);
                showProgress(this, "Scanning", "Scanning for Bluetooth devices");
                break;
            case R.id.reconnect_button:
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE);
                String previousDevice = sharedPreferences.getString(MainActivity.BLUETOOTH_NAME, null);
                String previousAddress = sharedPreferences.getString(MainActivity.BLUETOOTH_ADDRESS, null);
                MainActivity.c2XDevice.connect(previousAddress);

                Log.d(TAG, "bluetooth device connect - " + previousDevice);

                //update connection status
                updateConnectionStatus("Connecting - " + previousDevice,
                        false);
                break;
        }
    }

    private interface ISelection {
        void onSelection(BluetoothDevice device);
    }

    final ISelection selectionListener = new ISelection() {
        @Override
        public void onSelection(BluetoothDevice device) {
            try {
                //Connect the selected device.
                MainActivity.c2XDevice.connect(device);
                updateConnectionStatus("Connecting - " + device.getName(),
                        false);
            } catch (Exception e) {
                e.printStackTrace();
                updateConnectionStatus("An exception occurred.", true);
            }
        }
    };
}