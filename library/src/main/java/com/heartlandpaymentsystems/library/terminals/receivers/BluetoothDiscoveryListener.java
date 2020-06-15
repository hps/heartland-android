package com.heartlandpaymentsystems.library.terminals.receivers;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDiscoveryListener {
    void onDiscoveryStarted();
    void onDiscoveryFinished();
    void onBluetoothDeviceFound(BluetoothDevice foundDevice);
}
