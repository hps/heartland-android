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

import com.heartlandpaymentsystems.library.UpdateTerminalListener;
import com.heartlandpaymentsystems.library.terminals.AvailableTerminalVersionsListener;
import com.heartlandpaymentsystems.library.terminals.enums.TerminalUpdateType;

import java.util.List;

public class OTAUpdateActivity extends AppCompatActivity implements View.OnClickListener,
        AvailableTerminalVersionsListener, UpdateTerminalListener {

    private static final String TAG = OTAUpdateActivity.class.getSimpleName();

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otaupdate);

        findViewById(R.id.check_firmware_button).setOnClickListener(this);
        findViewById(R.id.check_kernel_button).setOnClickListener(this);

        MainActivity.c2XDevice.setAvailableTerminalVersionsListener(this);
        MainActivity.c2XDevice.setUpdateTerminalListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.check_firmware_button:
                if (!MainActivity.c2XDevice.isConnected()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_connected_ota));
                    return;
                }
                MainActivity.c2XDevice.getAvailableTerminalVersions(TerminalUpdateType.FIRMWARE);
                showProgress(this, getString(R.string.checking), getString(R.string.checking_firmware));
                break;
            case R.id.check_kernel_button:
                if (!MainActivity.c2XDevice.isConnected()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_connected_ota));
                    return;
                }
                MainActivity.c2XDevice.getAvailableTerminalVersions(TerminalUpdateType.CONFIG);
                showProgress(this, getString(R.string.checking), getString(R.string.checking_kernel));
                break;
        }
    }

    @Override
    public void onAvailableTerminalVersionsReceived(TerminalUpdateType type, List<String> versions) {
        hideProgress();
        if (type == TerminalUpdateType.FIRMWARE) {
            AlertDialog d = Dialogs.showListDialog("Available firmware versions",
                    OTAUpdateActivity.this, versions.toArray(new String[0]),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            showProgress(OTAUpdateActivity.this, "Updating", "Updating firmware...", 0);
                            MainActivity.c2XDevice.updateTerminal(TerminalUpdateType.FIRMWARE, versions.get(i));
                        }
                    });
        } else {
            AlertDialog d = Dialogs.showListDialog("Available kernel versions",
                    OTAUpdateActivity.this, versions.toArray(new String[0]),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            showProgress(OTAUpdateActivity.this, "Updating", "Updating kernel...", 0);
                            MainActivity.c2XDevice.updateTerminal(TerminalUpdateType.CONFIG, versions.get(i));
                        }
                    });
        }
    }

    @Override
    public void onTerminalVersionInfoError(Error error) {
        hideProgress();
        Log.e(TAG, "onTerminalVersionInfoError: " + error.getMessage());
    }

    @Override
    public void onProgress(@Nullable Double completionPercentage, @Nullable String progressMessage) {
        Log.d(TAG, "onProgress - " + completionPercentage + ", " + progressMessage);
        updateProgress(completionPercentage);
    }

    @Override
    public void onTerminalUpdateSuccess() {
        hideProgress();
        MainActivity.c2XDevice.disconnect();
        showAlertDialog("Update Success", "Terminal successfully updated. The device has been disconnected so please reconnect.");
    }

    @Override
    public void onTerminalUpdateError(Error error) {
        hideProgress();
        showAlertDialog("Update Error", "Terminal failed to update - " + error.getMessage());
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
}