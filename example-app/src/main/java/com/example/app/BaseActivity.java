package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.widget.Toast;
import com.heartlandpaymentsystems.library.terminals.DeviceListener;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.ConnectionMode;
import com.heartlandpaymentsystems.library.terminals.enums.ErrorType;
import com.heartlandpaymentsystems.library.utilities.ReceiptHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static com.example.app.Dialogs.hideProgress;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    protected AlertDialog mAlertDialog;
    protected static String bluetoothDeviceName = null;
    protected static String bluetoothDeviceAddress = null;
    private static String connectionStatus;
    protected BluetoothDevice previousSelected;
    protected ConnectionMode connectionMode;
    protected static boolean connectButtonEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (bluetoothDeviceName == null) {
            SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE);
            bluetoothDeviceName = sharedPreferences.getString(MainActivity.BLUETOOTH_NAME, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(MainActivity.mobyDevice != null) {
            MainActivity.mobyDevice.setMobyPairingContext(this);
            MainActivity.mobyDevice.setDeviceListener(mobyDeviceListener);
        }

        if(MainActivity.c2XDevice != null) {
            MainActivity.c2XDevice.setDeviceListener(c2xDeviceListener);
        }

        if (!(this instanceof BluetoothActivity)) {
            updateConnectionStatus(connectionStatus, connectButtonEnabled);
        }
    }

    protected void updateConnectionStatus(String status, boolean connectButtonEnabled) {
        this.connectButtonEnabled = connectButtonEnabled;
        //update the actionbar
        if (status != null && !status.isEmpty()) {
            connectionStatus = status;
        } else {
            connectionStatus = getString(R.string.bluetooth_status_not_connected);
        }
        getSupportActionBar().setTitle(connectionStatus);
    }

    protected void showAlertDialog(String title, String message) {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            Log.e("BaseActivity", "showAlertDialog - dialog is already showing");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
    }

    protected void showAlertDialog(String title, String message, boolean showReceiptOption, TerminalResponse transaction) {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            Log.e("BaseActivity", "showAlertDialog - dialog is already showing");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog.dismiss();
            }
        });
        if (showReceiptOption) {
            builder.setNeutralButton(getString(R.string.email_receipt), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mAlertDialog.dismiss();

                    //create email intent
                    Bitmap receipt = ReceiptHelper.createReceiptImage(transaction);
                    String path = Images.Media.insertImage(getContentResolver(), receipt, "receipt_" + System.currentTimeMillis(), null);
                    Uri imageUri = Uri.parse(path);
                    final Intent emailIntent1 = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    emailIntent1.putExtra(Intent.EXTRA_STREAM, imageUri);
                    emailIntent1.setType("image/png");
                    startActivity(Intent.createChooser(emailIntent1, "Send email using"));
                }
            });
        }
        mAlertDialog = builder.create();
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
    }

    protected void disconnectEvent() {
        //let children activities override if needed
    }

    protected void hideAlertDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.hide();
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
                if(MainActivity.c2XDevice != null) {
                    MainActivity.c2XDevice.connect(device);
                } else {
                    MainActivity.mobyDevice.connect(device);
                }
                updateConnectionStatus("Connecting - " + device.getName(),
                        false);
            } catch (Exception e) {
                e.printStackTrace();
                updateConnectionStatus("An exception occurred.", true);
            }
        }
    };

    protected DeviceListener mobyDeviceListener = new DeviceListener() {
        @Override
        public void onBluetoothDeviceFound(BluetoothDevice bluetoothDevice) {
            Log.d(TAG, "onBluetoothDeviceFound - " + bluetoothDevice.getName());
        }

        @Override
        public void onBluetoothDeviceList(HashSet<BluetoothDevice> deviceList) {
            hideProgress();
            if (deviceList == null || deviceList.isEmpty()) {
                return;
            }

            //Use this flag is the list of bluetooth keep popping up with the list of bluetooth.
            if (previousSelected != null) {
                return;
            }

            // receive list of available bluetooth devices
            // present to user for selection/confirmation
            final List<BluetoothDevice> list = new ArrayList<BluetoothDevice>(deviceList);
            final List<String> btList = new ArrayList<String>();

            for (BluetoothDevice dev : list) {
                if (dev.getName() != null) {
                    btList.add(dev.getName().trim());
                } else {
                    btList.add(dev.getAddress());
                }
            }

            //This is a dialog to show the list of devices
            //Here we use a dialog with a callback and a listener but you can use your own user selection
            AlertDialog d = Dialogs.showListDialog("Available devices",
                    BaseActivity.this, btList.toArray(new String[0]),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            BTSelection.getInstance()
                                    .Selection(list.get(i), new BTSelection.SelectionCallback() {
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
        public void onConnected(TerminalInfo terminalInfo) {
            hideProgress();
            Log.d(TAG, "onConnected - " + terminalInfo.toString());
            //keep the device version data
            MainActivity.firmwareVersion = terminalInfo.getFirmwareVersion();
            MainActivity.kernelVersion = terminalInfo.getKernelVersion();

            MainActivity.mobyDevice.getDeviceInfo();

            //update connection status
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connected - " +
                            terminalInfo.getSerialNumber(), Toast.LENGTH_LONG).show();
                    updateConnectionStatus("Connected - " + terminalInfo.getSerialNumber(),
                            false);
                    finish();
                }
            });

            if (connectionMode == ConnectionMode.BLUETOOTH) {
                //save the bluetooth data
                SharedPreferences.Editor editor =
                        getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE).edit();
                editor.putString(MainActivity.BLUETOOTH_NAME, bluetoothDeviceName);
                if (bluetoothDeviceAddress != null) {
                    editor.putString(MainActivity.BLUETOOTH_ADDRESS, bluetoothDeviceAddress);
                }
                editor.commit();
            }
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "onDisconnected");
            MainActivity.mobyDevice = null;
            //update connection status
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();
                    updateConnectionStatus("Disconnected", false);
                    disconnectEvent();
                }
            });
        }

        @Override
        public void onError(Error error, ErrorType errorType) {
            Log.e(TAG, "onError - " + error.toString() + ", " + errorType);
            if (Dialogs.pd != null && Dialogs.pd.isShowing()) {
                Dialogs.hideProgress();
            }
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            //update connection status
            updateConnectionStatus("Error - " + error.getMessage(), true);
        }

        @Override
        public void onTerminalInfoReceived(TerminalInfo terminalInfo) {
            Log.d(TAG, "onTerminalInfoReceived - " + terminalInfo.toString());
            if (MainActivity.isShowAbout()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Provision Version: " + terminalInfo.getAppVersion(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };

    protected DeviceListener c2xDeviceListener = new DeviceListener() {
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
                if (dev.getName() != null) {
                    btList.add(dev.getName().trim());
                } else {
                    btList.add(dev.getAddress());
                }
            }

            //This is a dialog to show the list of devices
            //Here we use a dialog with a callback and a listener but you can use your own user selection
            AlertDialog d = Dialogs.showListDialog("Available devices",
                    BaseActivity.this, btList.toArray(new String[0]),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            BTSelection.getInstance()
                                    .Selection(list.get(i), new BTSelection.SelectionCallback() {
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
            Log.d(TAG, "onConnected - " + terminalInfo.toString());
            //keep the device version data
            MainActivity.firmwareVersion = terminalInfo.getFirmwareVersion();
            MainActivity.kernelVersion = terminalInfo.getKernelVersion();

            //MainActivity.c2XDevice.getDeviceInfo();

            //update connection status
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connected - " +
                            terminalInfo.getSerialNumber(), Toast.LENGTH_LONG).show();
                    updateConnectionStatus("Connected - " + terminalInfo.getSerialNumber(),
                            false);
                    finish();
                }
            });

            //save the bluetooth data
            SharedPreferences.Editor editor =
                    getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE).edit();
            editor.putString(MainActivity.BLUETOOTH_NAME, bluetoothDeviceName);
            if (bluetoothDeviceAddress != null) {
                editor.putString(MainActivity.BLUETOOTH_ADDRESS, bluetoothDeviceAddress);
            }
            editor.commit();
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "onDisconnected");
            MainActivity.c2XDevice = null;
            //update connection status
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();
                    updateConnectionStatus("Disconnected", false);
                    disconnectEvent();
                }
            });
        }

        @Override
        public void onError(Error error, ErrorType errorType) {
            Log.e(TAG, "onError - " + error.toString() + ", " + errorType);
            if (Dialogs.pd != null && Dialogs.pd.isShowing()) {
                Dialogs.hideProgress();
            }
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            //update connection status
            updateConnectionStatus("Error - " + error.getMessage(), true);
        }

        @Override
        public void onTerminalInfoReceived(TerminalInfo terminalInfo) {
            Log.d(TAG, "onTerminalInfoReceived - " + terminalInfo.toString());
        }
    };
}