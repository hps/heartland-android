package com.heartlandpaymentsystems.library.terminals;

import java.util.HashSet;

import android.bluetooth.BluetoothDevice;

import com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo;

public interface DeviceListener {
    void onBluetoothDeviceFound(BluetoothDevice bluetoothDevice);
    void onBluetoothDeviceList(HashSet<BluetoothDevice> deviceList);
    void onConnected(TerminalInfo terminalInfo);
    void onError(Error Error);
    void onTerminalInfoReceived(TerminalInfo terminalInfo);
}
