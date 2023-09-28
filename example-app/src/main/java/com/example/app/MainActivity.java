package com.example.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.heartlandpaymentsystems.library.terminals.ConnectionConfig;
import com.heartlandpaymentsystems.library.terminals.c2x.C2XDevice;
import com.heartlandpaymentsystems.library.terminals.enums.ConnectionMode;
import com.heartlandpaymentsystems.library.terminals.enums.Environment;
import com.heartlandpaymentsystems.library.terminals.moby.MobyDevice;
import com.heartlandpaymentsystems.library.utilities.PermissionHelper;
import com.heartlandpaymentsystems.library.utilities.PermissionHelper.PermissionsCallBack;

public class MainActivity extends BaseActivity implements View.OnClickListener, PermissionsCallBack {

    public static final String SAVED_PREFS = "GPAPP_SAVED_PREFS";
    public static final String BLUETOOTH_NAME = "BLUETOOTH_NAME";
    public static final String BLUETOOTH_ADDRESS = "BLUETOOTH_ADDRESS";
    public static final String BLUETOOTH_MESSAGE_SHOWN = "BLUETOOTH_MESSAGE_SHOWN";
    public static final String SAVED_PUBLIC_KEY = "SAVED_PUBLIC_KEY";
    public static final String SAVED_USERNAME = "SAVED_USERNAME";
    public static final String SAVED_PASSWORD = "SAVED_PASSWORD";
    public static final String SAVED_SITE_ID = "SAVED_SITE_ID";
    public static final String SAVED_DEVICE_ID = "SAVED_DEVICE_ID";
    public static final String SAVED_LICENSE_ID = "SAVED_LICENSE_ID";
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;

    public static C2XDevice c2XDevice;
    public static MobyDevice mobyDevice;
    public static String firmwareVersion;
    public static String kernelVersion;
    public static String transactionId;
    public static TransactionState transactionState = TransactionState.None;
    public static String cardReaderStatus;
    public static String transactionResult;

    public static String PUBLIC_KEY;
    public static String USERNAME;
    public static String PASSWORD;
    public static String SITE_ID;
    public static String DEVICE_ID;
    public static String LICENSE_ID;

    private static boolean isAboutClick = false;
    Switch simpleSwitch;
    Button about;
    Button disconnect;
    private int startCounter = 0;

    public static boolean isShowAbout() {
        return isAboutClick;
    }

    @Override
    public void permissionsGranted() {
        Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void permissionsDenied() {
        Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //load saved credentials, if they exist
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE);
        String savedPublicKey = sharedPreferences.getString(MainActivity.SAVED_PUBLIC_KEY, null);
        String savedUsername = sharedPreferences.getString(MainActivity.SAVED_USERNAME, null);
        String savedPassword = sharedPreferences.getString(MainActivity.SAVED_PASSWORD, null);
        String savedSiteId = sharedPreferences.getString(MainActivity.SAVED_SITE_ID, null);
        String savedDeviceId = sharedPreferences.getString(MainActivity.SAVED_DEVICE_ID, null);
        String savedLicenseId = sharedPreferences.getString(MainActivity.SAVED_LICENSE_ID, null);
        if (savedPublicKey != null) {
            PUBLIC_KEY = savedPublicKey;
        }
        if (savedUsername != null) {
            USERNAME = savedUsername;
        }
        if (savedPassword != null) {
            PASSWORD = savedPassword;
        }
        if (savedSiteId != null) {
            SITE_ID = savedSiteId;
        }
        if (savedDeviceId != null) {
            DEVICE_ID = savedDeviceId;
        }
        if (savedLicenseId != null) {
            LICENSE_ID = savedLicenseId;
        }

        findViewById(R.id.credentials_button).setOnClickListener(this);
        findViewById(R.id.connect_to_device_button).setOnClickListener(this);
        findViewById(R.id.connect_to_moby_button).setOnClickListener(this);
        findViewById(R.id.connect_to_mobyUSB_button).setOnClickListener(this);
        findViewById(R.id.manual_card_button).setOnClickListener(this);
        findViewById(R.id.transaction_button).setOnClickListener(this);
        findViewById(R.id.ota_update_button).setOnClickListener(this);
        simpleSwitch = (Switch)findViewById(R.id.simpleSwitch);
        simpleSwitch.setOnClickListener(this);
        about = (Button)findViewById(R.id.about_button);
        disconnect = (Button)findViewById(R.id.disconnect_button);
        disconnect.setOnClickListener(this);
        //check location permission
        boolean permissionGranted = checkLocationPermission();
        if (permissionGranted) {
            //check if the message has already been shown before
            boolean bluetoothMessageShown = sharedPreferences.getBoolean(MainActivity.BLUETOOTH_MESSAGE_SHOWN, false);

            //show the message if it hasn't been shown before
            if (!bluetoothMessageShown) {
                SharedPreferences.Editor editor = getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(MainActivity.BLUETOOTH_MESSAGE_SHOWN, true);
                editor.commit();
                showAlertDialog(getString(R.string.bluetooth_improved_accuracy),
                        getString(R.string.bluetooth_accuracy_message));
            }
        }
    }

    private void showHideButtonDisplay(boolean isVisible) {
        if (mobyDevice != null && mobyDevice.isConnected()) {
            showHideButtonDisplay(isVisible, DeviceType.MOBY);
        }

        if (c2XDevice != null && c2XDevice.isConnected()) {
            showHideButtonDisplay(isVisible, DeviceType.C2X_C3X);
        }
    }

    @Override
    protected void disconnectEvent() {
        super.disconnectEvent();
        startCounter = 1;
        showHideButtonDisplay(false, DeviceType.MOBY);
    }

    private void showHideButtonDisplay(boolean isVisible, DeviceType type) {
        findViewById(R.id.connect_to_moby_button).setEnabled(!isVisible);
        findViewById(R.id.connect_to_mobyUSB_button).setEnabled(!isVisible);
        findViewById(R.id.connect_to_device_button).setEnabled(!isVisible);
        disconnect.setEnabled(isVisible);

        switch (type) {
            case MOBY:
                if (isVisible) {
                    about.setEnabled(true);
                    about.setOnClickListener(this);
                } else {
                    about.setEnabled(false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startCounter++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (startCounter > 1) {
            showHideButtonDisplay(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (c2XDevice != null) {
            c2XDevice.cancelScan();
        }
        super.onDestroy();
    }

    private boolean areCredentialsNull() {
        if (USERNAME == null || PASSWORD == null || SITE_ID == null || DEVICE_ID == null ||
                LICENSE_ID == null) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.credentials_button:
                Intent credentialsIntent = new Intent(this, CredentialsActivity.class);
                startActivity(credentialsIntent);
                break;
            case R.id.connect_to_device_button:
                if (areCredentialsNull()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_credentials_null));
                    return;
                }
                if (c2XDevice == null) {
                    c2XDevice = new C2XDevice(getApplicationContext(), getConnectionConfig(ConnectionMode.BLUETOOTH));
                }
                Intent connectIntent = new Intent(this, BluetoothActivity.class);
                connectIntent.putExtra(BluetoothActivity.EXTRA_CONNECTION_MODE, ConnectionMode.BLUETOOTH);
                startActivity(connectIntent);
                break;
            case R.id.connect_to_moby_button:
                if (areCredentialsNull()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_credentials_null));
                    return;
                }
                if (mobyDevice == null) {
                    mobyDevice = new MobyDevice(getApplicationContext(), getConnectionConfig(ConnectionMode.BLUETOOTH));
                }
                Intent mobyIntent = new Intent(this, BluetoothActivity.class);
                mobyIntent.putExtra(BluetoothActivity.EXTRA_CONNECTION_MODE, ConnectionMode.BLUETOOTH);
                startActivity(mobyIntent);
                break;
            case R.id.connect_to_mobyUSB_button:
                if (areCredentialsNull()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_credentials_null));
                    return;
                }
                if (mobyDevice == null) {
                    mobyDevice = new MobyDevice(getApplicationContext(), getConnectionConfig(ConnectionMode.USB));
                }
                Intent mobyUsbIntent = new Intent(this, BluetoothActivity.class);
                mobyUsbIntent.putExtra(BluetoothActivity.EXTRA_CONNECTION_MODE, ConnectionMode.USB);
                startActivity(mobyUsbIntent);
                break;
            case R.id.manual_card_button:
                Intent manualIntent = new Intent(this, CardEntryActivity.class);
                startActivity(manualIntent);
                break;
            case R.id.transaction_button:
                Intent transactionIntent = new Intent(this, TransactionListActivity.class);
                startActivity(transactionIntent);
                break;
            case R.id.ota_update_button:
                Intent updateIntent = new Intent(this, OTAUpdateActivity.class);
                startActivity(updateIntent);
                break;
            case R.id.simpleSwitch:
                if (simpleSwitch.isChecked()) {
                    Toast.makeText(this, "Prod is Selected", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Test is Selected", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.about_button:
                if (mobyDevice != null && mobyDevice.isConnected()) {
                    isAboutClick = true;
                    mobyDevice.getDeviceInfo();
                }
                break;
            case R.id.disconnect_button:
                if (mobyDevice != null && mobyDevice.isConnected()) {
                    mobyDevice.disconnect();
                    mobyDevice = null;
                    startCounter = 1;
                    showHideButtonDisplay(false, DeviceType.MOBY);
                } else if (c2XDevice != null && c2XDevice.isConnected()) {
                    c2XDevice.disconnect();
                    c2XDevice = null;
                    startCounter = 1;
                    showHideButtonDisplay(false, DeviceType.C2X_C3X);
                }
                break;
        }
    }

    private ConnectionConfig getConnectionConfig(ConnectionMode connType) {
        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.setUsername(USERNAME);
        connectionConfig.setPassword(PASSWORD);
        connectionConfig.setSiteId(SITE_ID);
        connectionConfig.setDeviceId(DEVICE_ID);
        connectionConfig.setLicenseId(LICENSE_ID);
        connectionConfig.setConnectionMode(connType);
        connectionConfig.setEnvironment(simpleSwitch.isChecked() ? Environment.PRODUCTION : Environment.TEST);
        return connectionConfig;
    }

    /**
     * Check the read location permission
     *
     * @return true if request permission is granted else return false
     */
    private boolean checkLocationPermission() {
        // final String permissionsRequired = Manifest.permission.ACCESS_FINE_LOCATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionHelper.checkAndRequestPermissions(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.INTERNET
            );
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
        PermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                checkPermissionResult(permissions[i], grantResults[i]);
            }
        }
    }

    //permission methods

    private void checkPermissionResult(String permission, int grantResult) {
        if (permission.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                //show toast
                Toast.makeText(this, getString(R.string.location_permission_necessary), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public enum TransactionState {
        None,
        Processing,
        Complete
    }

    enum DeviceType {
        C2X_C3X,
        MOBY
    }
}
