package com.heartlandpaymentsystems.library.terminals.c2x;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.heartlandpaymentsystems.library.terminals.UpdateTerminalListener;
import com.heartlandpaymentsystems.library.terminals.AvailableTerminalVersionsListener;
import com.heartlandpaymentsystems.library.terminals.ConnectionConfig;
import com.heartlandpaymentsystems.library.terminals.DeviceListener;
import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.heartlandpaymentsystems.library.terminals.TransactionListener;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionResult;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.Environment;
import com.heartlandpaymentsystems.library.terminals.enums.TerminalUpdateType;
import com.heartlandpaymentsystems.library.terminals.receivers.BluetoothDiscoveryListener;
import com.heartlandpaymentsystems.library.terminals.receivers.BluetoothReceiver;
import com.tsys.payments.library.connection.ConnectionListener;
import com.tsys.payments.library.domain.CardholderInteractionRequest;
import com.tsys.payments.library.domain.GatewayConfiguration;
import com.tsys.payments.library.domain.TerminalConfiguration;
import com.tsys.payments.library.domain.TerminalInfo;
import com.tsys.payments.library.domain.TransactionConfiguration;
import com.tsys.payments.library.domain.TransactionRequest;
import com.tsys.payments.library.domain.TransactionResponse;
import com.tsys.payments.library.enums.ConnectionType;
import com.tsys.payments.library.enums.CurrencyCode;
import com.tsys.payments.library.enums.TerminalAuthenticationCapability;
import com.tsys.payments.library.enums.TerminalInputCapability;
import com.tsys.payments.library.enums.TerminalOperatingEnvironment;
import com.tsys.payments.library.enums.TerminalOutputCapability;
import com.tsys.payments.library.enums.TerminalType;
import com.tsys.payments.library.enums.TransactionStatus;
import com.tsys.payments.library.exceptions.InitializationException;
import com.tsys.payments.library.exceptions.Error;
import com.tsys.payments.library.gateway.enums.GatewayType;
import com.tsys.payments.library.terminal.AvailableVersionsListener;
import com.tsys.payments.library.terminal.TerminalInfoListener;
import com.tsys.payments.library.terminal.UpdateListener;
import com.tsys.payments.library.utils.LibraryConfigHelper;
import com.tsys.payments.transaction.TransactionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class C2XDevice implements IDevice {

    private static final String TAG = C2XDevice.class.getSimpleName();

    private Context applicationContext;
    private ConnectionConfig connectionConfig;
    private TransactionConfiguration transactionConfig;
    private TerminalConfiguration terminalConfig;
    private GatewayConfiguration gatewayConfig;
    private TransactionManager transactionManager;
    private TerminalInfo connectedTerminalInfo;
    private DeviceListener deviceListener;
    private TransactionListener transactionListener;
    private AvailableTerminalVersionsListener availableTerminalVersionsListener;
    private UpdateTerminalListener updateTerminalListener;
    private HashSet<BluetoothDevice> bluetoothDevices;
    private BluetoothReceiver bluetoothReceiver;

    public C2XDevice(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public C2XDevice(Context applicationContext, ConnectionConfig connectionConfig) {
        this.applicationContext = applicationContext;
        this.setConnectionConfig(connectionConfig);
    }

    public void setDeviceListener(DeviceListener deviceListener) {
        this.deviceListener = deviceListener;
    }

    public void setTransactionListener(TransactionListener transactionListener) {
        this.transactionListener = transactionListener;
    }

    public void setAvailableTerminalVersionsListener(AvailableTerminalVersionsListener availableTerminalVersionsListener) {
        this.availableTerminalVersionsListener = availableTerminalVersionsListener;
    }

    public void setUpdateTerminalListener(UpdateTerminalListener updateTerminalListener) {
        this.updateTerminalListener = updateTerminalListener;
    }

    public void initialize() {
        scan();
    }

    public void connect(BluetoothDevice device) {
        connect(device.getAddress());
    }

    public void connect(String address) {
        terminalConfig.setHost(address);

        if (isTransactionManagerConnected()) {
            return;
        }

        transactionManager = TransactionManager.getInstance();

        if (transactionManager != null && terminalConfig != null) {
            ConnectionType[] connectionTypes =
                    transactionManager.getSupportedTerminalConnectionTypes(terminalConfig.getTerminalType());

            terminalConfig.setConnectionType(connectionTypes[0]);

            if (!transactionManager.isInitialized()) {
                initializeTransactionManager();
            }

            if (transactionManager.isInitialized()) {
                transactionManager.connect(new ConnectionListenerImpl());
                transactionManager.updateTransactionListener(new TransactionListenerImpl());
            }
        }
    }

    public void disconnect() {
        if (isTransactionManagerConnected()) {
            transactionManager.disconnect();
        }
    }

    public void unregisterBluetoothReceiver() {
        if (applicationContext != null) {
            applicationContext.unregisterReceiver(bluetoothReceiver);
        }
    }

    public void getDeviceInfo() {
        if (isTransactionManagerConnected()) {
            transactionManager.getDeviceInfo(new TerminalInfoListenerImpl());
        }
    }

    @Override
    public void doTransaction(TransactionRequest transactionRequest) {
        if (transactionManager == null) {
            transactionManager = TransactionManager.getInstance();
        }

        if (!transactionManager.isInitialized()) {
            initializeTransactionManager();
        }

        transactionManager.startTransaction(transactionRequest, new TransactionListenerImpl());
    }

    public void sendCardholderInteractionResult(CardholderInteractionResult cardholderInteractionResult) {
        if (isTransactionManagerConnected()) {
            transactionManager.sendCardholderInteractionResult(map(cardholderInteractionResult));
        }
    }

    protected void initializeTransactionManager() {
        try {
            transactionManager.initialize(
                    applicationContext,
                    terminalConfig,
                    transactionConfig,
                    gatewayConfig
            );
        } catch (InitializationException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isTransactionManagerConnected();
    }

    public void scan() {
        if (bluetoothReceiver == null) {
            bluetoothReceiver = new BluetoothReceiver();
            bluetoothReceiver.setListener(new BluetoothListenerImpl());
        }

        if (applicationContext != null) {
            IntentFilter bluetoothFilter = new IntentFilter();
            bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
            bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            applicationContext.registerReceiver(bluetoothReceiver, bluetoothFilter);
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
    }

    public void cancelScan(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;

        LibraryConfigHelper.setDebugMode(connectionConfig.getEnvironment().equals(Environment.TEST));
        LibraryConfigHelper.setAllowDupes(connectionConfig.getAllowDupes());

        transactionConfig = new TransactionConfiguration();
        transactionConfig.setChipEnabled(true);
        transactionConfig.setContactlessEnabled(true);
        transactionConfig.setCurrencyCode(CurrencyCode.USD);
        transactionConfig.setMagStripeEnabled(true);

        terminalConfig = new TerminalConfiguration();
        terminalConfig.setTerminalType(TerminalType.BBPOS_C2X);
        terminalConfig.setCapability(TerminalInputCapability.MAGSTRIPE_ICC_KEYED_ENTRY_ONLY);
        terminalConfig.setOutputCapability(TerminalOutputCapability.PRINT_AND_DISPLAY);
        terminalConfig.setAuthenticationCapability(TerminalAuthenticationCapability.NO_CAPABILITY);
        terminalConfig.setOperatingEnvironment(TerminalOperatingEnvironment.ON_MERCHANT_PREMISES_ATTENDED);

        final Long timeout = connectionConfig.getTimeout();
        terminalConfig.setTimeout(timeout != 0 ? timeout : 60000L);

        HashMap<String, String> credentials = new HashMap<>();
//        credentials.put("secret_api_key", connectionConfig.getSecretApiKey());
        credentials.put("version_number", "3409");
        credentials.put("developer_id", "002914");
        credentials.put("user_name", connectionConfig.getUsername());
        credentials.put("license_id", connectionConfig.getLicenseId());
        credentials.put("site_id", connectionConfig.getSiteId());
        credentials.put("password", connectionConfig.getPassword());
        credentials.put("terminal_id", connectionConfig.getDeviceId());

        gatewayConfig = new GatewayConfiguration();
        gatewayConfig.setGatewayType(GatewayType.PORTICO);
        gatewayConfig.setCredentials(credentials);
    }

    protected boolean isTransactionManagerConnected() {
        return transactionManager != null && transactionManager.isConnected();
    }

    public void cancelTransaction() {
        transactionManager.cancel();
    }

    //OTA methods
    public void getAvailableTerminalVersions(TerminalUpdateType terminalUpdateType) {
        if (!transactionManager.isInitialized()) {
            Log.e(TAG, "TransactionManager not initialized, please connect to device first.");
            return;
        }
        if (availableTerminalVersionsListener == null) {
            Log.e(TAG, "AvailableTerminalVersionsListener is null, please set a valid listener.");
            return;
        }

        com.tsys.payments.library.enums.TerminalUpdateType updateType = com.tsys.payments.library.enums.TerminalUpdateType.FIRMWARE;
        if (terminalUpdateType == TerminalUpdateType.CONFIG) {
            updateType = com.tsys.payments.library.enums.TerminalUpdateType.KERNEL;
        }

        transactionManager.getAvailableTerminalVersions(updateType, null, new AvailableTerminalVersionsListenerImpl());
    }

    public void updateTerminal(@NonNull TerminalUpdateType terminalUpdateType,
                               @Nullable String version) {
        if (!transactionManager.isInitialized()) {
            Log.e(TAG, "TransactionManager not initialized, please connect to device first.");
            return;
        }
        if (updateTerminalListener == null) {
            Log.e(TAG, "UpdateTerminalListener is null, please set a valid listener.");
            return;
        }

        com.tsys.payments.library.enums.TerminalUpdateType updateType = com.tsys.payments.library.enums.TerminalUpdateType.FIRMWARE;
        if (terminalUpdateType == TerminalUpdateType.CONFIG) {
            updateType = com.tsys.payments.library.enums.TerminalUpdateType.KERNEL;
        }

        transactionManager.updateTerminal(updateType, null, version, new UpdateTerminalListenerImpl());
    }

    protected class ConnectionListenerImpl implements ConnectionListener {
        @Override
        public void onConnected(TerminalInfo terminalInfo) {
            connectedTerminalInfo = terminalInfo;

            if (deviceListener != null) {
                deviceListener.onConnected(map(terminalInfo));
            }
        }

        @Override
        public void onError(Error error) {
            if (deviceListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                deviceListener.onError(err);
            }
        }
    }

    protected class BluetoothListenerImpl implements BluetoothDiscoveryListener {
        @Override
        public void onDiscoveryStarted() {
            bluetoothDevices = new HashSet<>();
        }

        @Override
        public void onDiscoveryFinished() {
            if (deviceListener == null) return;
            deviceListener.onBluetoothDeviceList(bluetoothDevices);
        }

        @Override
        public void onBluetoothDeviceFound(BluetoothDevice foundDevice) {
            if (foundDevice == null) {
                return;
            }

            if (foundDevice.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC &&
                    foundDevice.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
                return;
            }

            bluetoothDevices.add(foundDevice);

            if (deviceListener != null) deviceListener.onBluetoothDeviceFound(foundDevice);
        }
    }

    protected class TerminalInfoListenerImpl implements TerminalInfoListener {
        @Override
        public void onTerminalInfoReceived(TerminalInfo terminalInfo) {
            if (deviceListener != null) {
                deviceListener.onTerminalInfoReceived(map(terminalInfo));
            }
        }

        @Override
        public void onError(Error error) {
            if (deviceListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                deviceListener.onError(err);
            }
        }
    }

    protected class TransactionListenerImpl implements com.tsys.payments.library.transaction.TransactionListener {
        @Override
        public void onStatusUpdate(TransactionStatus transactionStatus) {
            if (transactionListener != null) {
                transactionListener.onStatusUpdate(
                        com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus.fromVitalSdk(transactionStatus)
                );
            }
        }

        @Override
        public void onCardholderInteractionRequested(CardholderInteractionRequest cardholderInteractionRequest) {
            if (transactionListener != null) {
                transactionListener.onCardholderInteractionRequested(map(cardholderInteractionRequest));
            }
        }

        @Override
        public void onTransactionComplete(TransactionResponse transactionResponse) {
            if (transactionListener != null) {
                transactionListener.onTransactionComplete(TerminalResponse.fromTransactionResponse(transactionResponse));
            }

        }

        @Override
        public void onError(Error error) {
            if (transactionListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                transactionListener.onError(err);
            }
        }
    }

    protected class AvailableTerminalVersionsListenerImpl implements com.tsys.payments.library.terminal.AvailableTerminalVersionsListener {

        @Override
        public void onAvailableTerminalVersionsReceived(com.tsys.payments.library.enums.TerminalUpdateType type, List<String> versions) {
            if (availableTerminalVersionsListener != null) {
                TerminalUpdateType updateType = TerminalUpdateType.FIRMWARE;
                if (type == com.tsys.payments.library.enums.TerminalUpdateType.KERNEL) {
                    updateType = TerminalUpdateType.CONFIG;
                }
                availableTerminalVersionsListener.onAvailableTerminalVersionsReceived(updateType, versions);
            }
        }

        @Override
        public void onTerminalVersionInfoError(Error error) {
            if (availableTerminalVersionsListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                availableTerminalVersionsListener.onTerminalVersionInfoError(err);
            }
        }
    }

    protected class UpdateTerminalListenerImpl implements com.tsys.payments.library.terminal.UpdateTerminalListener {

        @Override
        public void onProgress(@Nullable Double completionPercentage, @Nullable String progressMessage) {
            if (updateTerminalListener != null) {
                updateTerminalListener.onProgress(completionPercentage, progressMessage);
            }
        }

        @Override
        public void onTerminalUpdateSuccess() {
            if (updateTerminalListener != null) {
                updateTerminalListener.onTerminalUpdateSuccess();
            }
        }

        @Override
        public void onTerminalUpdateError(Error error) {
            if (updateTerminalListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                updateTerminalListener.onTerminalUpdateError(err);
            }
        }
    }

    private com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo map(TerminalInfo info) {
        final com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo ti =
                new com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo();
        ti.setAppName(info.getAppName());
        ti.setAppVersion(info.getAppVersion());
        ti.setBatteryLevel(info.getBatteryLevel());
        ti.setFirmwareVersion(info.getFirmwareVersion());
        ti.setSerialNumber(info.getSerialNumber());
        ti.setTerminalType(info.getTerminalType());
        return ti;
    }

    private com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest map(CardholderInteractionRequest info) {
        final com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest cr =
                new com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest();
        cr.setCardholderInteractionType(info.getCardholderInteractionType());
        cr.setCommercialCardDataFields(info.getCommercialCardDataFields());
        cr.setFinalTransactionAmount(info.getFinalTransactionAmount());
        cr.setSupportedApplications(info.getSupportedApplications());
        return cr;
    }

    private com.tsys.payments.library.domain.CardholderInteractionResult map(CardholderInteractionResult info) {
        final com.tsys.payments.library.domain.CardholderInteractionResult result =
                new com.tsys.payments.library.domain.CardholderInteractionResult(info.getCardholderInteractionType());
        result.setCommercialCardData(info.getCommercialCardData());
        result.setFinalAmountConfirmed(info.getFinalAmountConfirmed());
        result.setSelectedAidIndex(info.getSelectedAidIndex());
        return result;
    }
}
