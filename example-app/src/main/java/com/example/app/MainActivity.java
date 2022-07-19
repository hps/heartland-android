package com.example.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.heartlandpaymentsystems.library.terminals.ConnectionConfig;
import com.heartlandpaymentsystems.library.terminals.c2x.C2XDevice;
import com.heartlandpaymentsystems.library.terminals.enums.ConnectionMode;
import com.heartlandpaymentsystems.library.terminals.enums.Environment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    public static final String SAVED_PREFS = "GPAPP_SAVED_PREFS";
    public static final String BLUETOOTH_NAME = "BLUETOOTH_NAME";
    public static final String BLUETOOTH_ADDRESS = "BLUETOOTH_ADDRESS";
    public static final String BLUETOOTH_MESSAGE_SHOWN = "BLUETOOTH_MESSAGE_SHOWN";

    private static final int PERMISSION_REQUEST_CODE = 1;

    public static C2XDevice c2XDevice;

    public static final String PUBLIC_KEY = "YOUR_KEY_GOES_HERE";
    private static final String USERNAME = "YOUR_USERNAME_GOES_HERE";
    private static final String PASSWORD = "YOUR_PASSWORD_GOES_HERE";
    private static final String SITE_ID = "YOUR_SITEID_GOES_HERE";
    private static final String DEVICE_ID = "YOUR_DEVICEID_GOES_HERE";
    private static final String LICENSE_ID = "YOUR_LICENSEID_GOES_HERE";

    private AlertDialog mAlertDialog;

    public enum TransactionState {
        None,
        Processing,
        Complete
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.connect_to_device_button).setOnClickListener(this);
        findViewById(R.id.manual_card_button).setOnClickListener(this);
        findViewById(R.id.transaction_button).setOnClickListener(this);
        findViewById(R.id.ota_update_button).setOnClickListener(this);

        if (PUBLIC_KEY.length() == 0) {
            Toast.makeText(this, "Please provide public key", Toast.LENGTH_SHORT).show();
        }

        //check location permission
        boolean permissionGranted = checkLocationPermission();
        if (permissionGranted) {
            //check if the message has already been shown before
            SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE);
            boolean bluetoothMessageShown = sharedPreferences.getBoolean(MainActivity.BLUETOOTH_MESSAGE_SHOWN, false);

            //show the message if it hasn't been shown before
            if (!bluetoothMessageShown) {
                SharedPreferences.Editor editor = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(MainActivity.BLUETOOTH_MESSAGE_SHOWN, true);
                editor.commit();
                showAlertDialog(getString(R.string.bluetooth_improved_accuracy), getString(R.string.bluetooth_accuracy_message));
            }
        }

        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.setUsername(USERNAME);
        connectionConfig.setPassword(PASSWORD);
        connectionConfig.setSiteId(SITE_ID);
        connectionConfig.setDeviceId(DEVICE_ID);
        connectionConfig.setLicenseId(LICENSE_ID);
        connectionConfig.setConnectionMode(ConnectionMode.BLUETOOTH);
        connectionConfig.setEnvironment(Environment.TEST);
        connectionConfig.setAllowDupes(false);

        c2XDevice = new C2XDevice(getApplicationContext(), connectionConfig);

    }

    private void showAlertDialog(String title, String message) {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            Log.e(TAG, "showAlertDialog - dialog is already showing");
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

    private void hideAlertDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.hide();
        }
    }

    @Override
    protected void onDestroy() {
        c2XDevice.cancelScan();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connect_to_device_button:
                Intent connectIntent = new Intent(this, BluetoothActivity.class);
                startActivity(connectIntent);
                break;
            case R.id.manual_card_button:
                Intent manualIntent = new Intent(this, CardEntryActivity.class);
                startActivity(manualIntent);
                break;
            case R.id.transaction_button:
                Intent transactionIntent = new Intent(this, C2XTransactionActivity.class);
                startActivity(transactionIntent);
                break;
            case R.id.ota_update_button:
                Intent updateIntent = new Intent(this, OTAUpdateActivity.class);
                startActivity(updateIntent);
                break;
        }
    }

    //permission methods
    /**
     * Check the read location permission
     *
     * @return true if request permission is granted else return false
     */
    private boolean checkLocationPermission() {
        final String permissionsRequired = Manifest.permission.ACCESS_FINE_LOCATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check to see if permission already granted.
            int permissionStatus = checkSelfPermission(permissionsRequired);

            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {permissionsRequired}, PERMISSION_REQUEST_CODE);
                return false;
            }
        } else {
            // Permission management not supported on pre Android M devices
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                checkPermissionResult(permissions[i], grantResults[i]);
            }
        }
    }

    private void checkPermissionResult(String permission, int grantResult) {
        if (permission.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                //show toast
                Toast.makeText(this, getString(R.string.location_permission_necessary), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
