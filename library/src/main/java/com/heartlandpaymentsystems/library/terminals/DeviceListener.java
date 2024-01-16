package com.heartlandpaymentsystems.library.terminals;

import java.util.HashSet;

import android.bluetooth.BluetoothDevice;

import com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo;
import com.heartlandpaymentsystems.library.terminals.enums.ErrorType;

public interface DeviceListener {
    void onBluetoothDeviceFound(BluetoothDevice bluetoothDevice);
    void onBluetoothDeviceList(HashSet<BluetoothDevice> deviceList);
    void onConnected(TerminalInfo terminalInfo);
    void onDisconnected();
    void onError(Error Error, ErrorType errorType);
    void onTerminalInfoReceived(TerminalInfo terminalInfo);
}