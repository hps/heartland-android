package com.example.app;

import static com.example.app.Dialogs.hideProgress;
import static com.example.app.Dialogs.showProgress;
import static com.example.app.Dialogs.updateProgress;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.heartlandpaymentsystems.library.terminals.AvailableTerminalVersionsListener;
import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.heartlandpaymentsystems.library.terminals.UpdateTerminalListener;
import com.heartlandpaymentsystems.library.terminals.enums.TerminalUpdateType;

import java.util.List;

public class OTAUpdateActivity extends BaseActivity implements View.OnClickListener,
        AvailableTerminalVersionsListener, UpdateTerminalListener {

    private static final String TAG = OTAUpdateActivity.class.getSimpleName();
    TerminalUpdateType terminalUpdateType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otaupdate);

        findViewById(R.id.check_firmware_button).setOnClickListener(this);
        findViewById(R.id.check_kernel_button).setOnClickListener(this);
        findViewById(R.id.rki_button).setOnClickListener(this);

        if (MainActivity.mobyDevice != null) {
            MainActivity.mobyDevice.setAvailableTerminalVersionsListener(this);
            MainActivity.mobyDevice.setUpdateTerminalListener(this);
        } else {
            MainActivity.c2XDevice.setAvailableTerminalVersionsListener(this);
            MainActivity.c2XDevice.setUpdateTerminalListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.check_firmware_button:
                if ((MainActivity.mobyDevice == null || !MainActivity.mobyDevice.isConnected()) &&
                        (MainActivity.c2XDevice == null || !MainActivity.c2XDevice.isConnected())) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_connected_ota));
                    return;
                }

                showProgress(this, getString(R.string.checking), getString(R.string.checking_firmware), null);

                if (MainActivity.mobyDevice != null) {
                    MainActivity.mobyDevice.getAvailableTerminalVersions(TerminalUpdateType.FIRMWARE);
                } else {
                    MainActivity.c2XDevice.getAvailableTerminalVersions(TerminalUpdateType.FIRMWARE);
                }
                break;
            case R.id.check_kernel_button:
                if ((MainActivity.mobyDevice == null || !MainActivity.mobyDevice.isConnected()) &&
                        (MainActivity.c2XDevice == null || !MainActivity.c2XDevice.isConnected())) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_connected_ota));
                    return;
                }

                showProgress(this, getString(R.string.checking), getString(R.string.checking_kernel), null);

                if (MainActivity.mobyDevice != null) {
                    MainActivity.mobyDevice.getAvailableTerminalVersions(TerminalUpdateType.CONFIG);
                } else {
                    MainActivity.c2XDevice.getAvailableTerminalVersions(TerminalUpdateType.CONFIG);
                }
                break;
            case R.id.rki_button:
                if ((MainActivity.mobyDevice == null || !MainActivity.mobyDevice.isConnected()) &&
                        (MainActivity.c2XDevice == null || !MainActivity.c2XDevice.isConnected())) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_connected_ota));
                    return;
                }

                if (MainActivity.mobyDevice != null) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_supported_rki));
                    return;
                }

                terminalUpdateType = TerminalUpdateType.RKI;
                showProgress(this, getString(R.string.checking), getString(R.string.checking_kernel), null);
                MainActivity.c2XDevice.remoteKeyInjection();
                break;
        }
    }

    @Override
    public void onAvailableTerminalVersionsReceived(TerminalUpdateType type, List<String> versions) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                if (type == TerminalUpdateType.FIRMWARE) {
                    AlertDialog d = Dialogs.showListDialog("Available firmware versions",
                            OTAUpdateActivity.this, versions.toArray(new String[0]),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialogInterface, int i) {
                                    terminalUpdateType = TerminalUpdateType.FIRMWARE;
                                    showProgress(OTAUpdateActivity.this, "Updating", "Updating firmware...", 0);
                                    if (MainActivity.mobyDevice != null) {
                                        MainActivity.mobyDevice.updateTerminal(TerminalUpdateType.FIRMWARE, null);
                                    } else {
                                        MainActivity.c2XDevice.updateTerminal(TerminalUpdateType.FIRMWARE, versions.get(i));
                                    }
                                }
                            });
                } else {
                    AlertDialog d = Dialogs.showListDialog("Available kernel versions",
                            OTAUpdateActivity.this, versions.toArray(new String[0]),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialogInterface, int i) {
                                    terminalUpdateType = TerminalUpdateType.CONFIG;
                                    showProgress(OTAUpdateActivity.this, "Updating", "Updating kernel...", 0);
                                    if (MainActivity.mobyDevice != null) {
                                        MainActivity.mobyDevice.updateTerminal(TerminalUpdateType.CONFIG, versions.get(i));
                                    } else {
                                        MainActivity.c2XDevice.updateTerminal(TerminalUpdateType.CONFIG, versions.get(i));
                                    }
                                }
                            });
                }

            }
        });
    }

    @Override
    public void onTerminalVersionInfoError(Error error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                Log.e(TAG, "onTerminalVersionInfoError: " + error.getMessage());
            }
        });
    }

    @Override
    public void onProgress(@Nullable Double completionPercentage, @Nullable String progressMessage) {
        Log.d(TAG, "onProgress - " + completionPercentage + ", " + progressMessage);
        if(terminalUpdateType == TerminalUpdateType.RKI && completionPercentage <= 1) {
            showProgress(OTAUpdateActivity.this, "Injecting", "Injecting Keys...", 0);
        }
        updateProgress(completionPercentage);
    }

    @Override
    public void onTerminalUpdateSuccess() {
        String msg = "";
        hideProgress();
        if(terminalUpdateType != TerminalUpdateType.RKI &&
                (terminalUpdateType != TerminalUpdateType.CONFIG && MainActivity.mobyDevice != null)) {
            if (MainActivity.mobyDevice != null) {
                MainActivity.mobyDevice.disconnect();
            } else {
                MainActivity.c2XDevice.disconnect();
            }
            msg = "Terminal successfully updated. The device has been disconnected so please reconnect.";
        } else {
            msg = "Terminal successfully updated.";
        }
        showAlertDialog("Update Success", msg);
    }

    @Override
    public void onTerminalUpdateError(Error error) {
        hideProgress();
        showAlertDialog("Update Error", "Terminal failed to update - " + error.getMessage());
    }
}