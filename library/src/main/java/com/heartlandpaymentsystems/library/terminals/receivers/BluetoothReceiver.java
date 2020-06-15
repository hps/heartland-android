package com.heartlandpaymentsystems.library.terminals.receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothReceiver extends BroadcastReceiver {
    private BluetoothDiscoveryListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (listener == null) {
            return;
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            listener.onDiscoveryStarted();
            return;
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            listener.onDiscoveryFinished();
            return;
        }

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (foundDevice != null) {
                listener.onBluetoothDeviceFound(foundDevice);
            }
        }
    }

    public void setListener(BluetoothDiscoveryListener listener) {
        this.listener = listener;
    }

}
