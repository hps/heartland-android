package com.heartlandpaymentsystems.library.terminals.moby;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.heartlandpaymentsystems.library.BuildConfig;
import com.heartlandpaymentsystems.library.R;
import com.heartlandpaymentsystems.library.terminals.AvailableTerminalVersionsListener;
import com.heartlandpaymentsystems.library.terminals.ConnectionConfig;
import com.heartlandpaymentsystems.library.terminals.DeviceListener;
import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.heartlandpaymentsystems.library.terminals.SafListener;
import com.heartlandpaymentsystems.library.terminals.TransactionListener;
import com.heartlandpaymentsystems.library.terminals.UpdateTerminalListener;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionResult;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.ConnectionMode;
import com.heartlandpaymentsystems.library.terminals.enums.Environment;
import com.heartlandpaymentsystems.library.terminals.enums.ErrorType;
import com.heartlandpaymentsystems.library.terminals.enums.TerminalUpdateType;
import com.heartlandpaymentsystems.library.terminals.receivers.BluetoothDiscoveryListener;
import com.heartlandpaymentsystems.library.terminals.receivers.BluetoothReceiver;
import com.roam.roamreaderunifiedapi.callback.LedPairingConfirmationCallback;
import com.roam.roamreaderunifiedapi.data.LedSequence;
import com.roam.roamreaderunifiedapi.view.PairingLedView;
import com.tsys.payments.library.connection.ConnectionListener;
import com.tsys.payments.library.connection.LedMobyPairingListener;
import com.tsys.payments.library.db.DatabaseConfig;
import com.tsys.payments.library.db.SafDatabaseConfig;
import com.tsys.payments.library.db.entity.SafTransaction;
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
import com.tsys.payments.library.enums.TransactionResultType;
import com.tsys.payments.library.enums.TransactionStatus;
import com.tsys.payments.library.enums.TransactionType;
import com.tsys.payments.library.exceptions.Error;
import com.tsys.payments.library.exceptions.InitializationException;
import com.tsys.payments.library.gateway.enums.GatewayType;
import com.tsys.payments.library.terminal.TerminalInfoListener;
import com.tsys.payments.library.utils.LibraryConfigHelper;
import com.tsys.payments.transaction.TransactionManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Moby Device Implementation. Device: Ingenico Moby5500
 */
public class MobyDevice implements IDevice {
    private static final String TAG = MobyDevice.class.getSimpleName();

    private static boolean timberPlanted;
    private TransactionManager transactionManager;
    private Context applicationContext;
    private Context mobyPairingContext;
    private ConnectionConfig connectionConfig;
    private HashSet<BluetoothDevice> bluetoothDevices;
    private BluetoothReceiver bluetoothReceiver;
    private TransactionConfiguration transactionConfig;
    private TerminalConfiguration terminalConfig;
    private GatewayConfiguration gatewayConfig;
    private DatabaseConfig databaseConfig;
    private DeviceListener deviceListener;
    private TransactionListener transactionListener;
    private SafListener safListener;
    private AvailableTerminalVersionsListener availableTerminalVersionsListener;
    private UpdateTerminalListener updateTerminalListener;
    private PairingLedView pairingLedView;
    private AlertDialog dialog;
    private boolean isDeviceSelected;
    private boolean isScanned;

    private List<Long> safIDs;

    /**
     * Instantiates a new Moby device.
     *
     * @param context the context
     */
    public MobyDevice(Context context) {
        this.applicationContext = context;
    }

    /**
     * Instantiates a new Moby device.
     *
     * @param context the context
     * @param config  the config
     */
    public MobyDevice(Context context, ConnectionConfig config) {
        this.applicationContext = context;
        this.setConnectionConfig(config);
    }

    /**
     * Set the Connection Configuration
     *
     * @param connectionConfig the connection config
     */
    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;

        LibraryConfigHelper.setDebugMode(connectionConfig.getEnvironment().equals(Environment.TEST));
        LibraryConfigHelper.setSdkNameVersion("android;version=" + BuildConfig.VERSION_NAME);
        if (connectionConfig.getEnvironment().equals(Environment.TEST) && !timberPlanted) {
            Timber.plant(new Timber.DebugTree());
            timberPlanted = true;
        }

        transactionConfig = new TransactionConfiguration();
        transactionConfig.setQuickChipEnabled(true);
        transactionConfig.setChipEnabled(true);
        transactionConfig.setContactlessEnabled(true);
        transactionConfig.setCurrencyCode(CurrencyCode.USD);
        transactionConfig.setMagStripeEnabled(true);

        terminalConfig = new TerminalConfiguration();
        terminalConfig.setTerminalType(TerminalType.INGENICO_MOBY_5500);
        terminalConfig.setCapability(TerminalInputCapability.MAGSTRIPE_ICC_KEYED_ENTRY_ONLY);
        terminalConfig.setOutputCapability(TerminalOutputCapability.PRINT_AND_DISPLAY);
        terminalConfig.setAuthenticationCapability(TerminalAuthenticationCapability.NO_CAPABILITY);
        terminalConfig.setOperatingEnvironment(TerminalOperatingEnvironment.ON_MERCHANT_PREMISES_ATTENDED);
        if (connectionConfig.getConnectionMode() == ConnectionMode.USB) {
            terminalConfig.setConnectionType(ConnectionType.USB);
        } else {
            terminalConfig.setConnectionType(ConnectionType.BLUETOOTH);
            terminalConfig.setPairingListener(new LedPairingListenerImpl());
        }

        final Long timeout = connectionConfig.getTimeout();
        terminalConfig.setTimeout(timeout != 0 ? timeout : 60000L);

        if (connectionConfig.getPort() != null && !connectionConfig.getPort().isEmpty()) {
            terminalConfig.setPort(Integer.parseInt(connectionConfig.getPort()));
        }

        HashMap<String, String> credentials = new HashMap<>();
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

        SafDatabaseConfig safDatabaseConfig = new SafDatabaseConfig(connectionConfig.isSafEnabled(),
                connectionConfig.getSafExpirationInDays(), TimeUnit.DAYS);
        databaseConfig = new DatabaseConfig(applicationContext, "mobyDB", null,
                safDatabaseConfig, GatewayType.PORTICO);
        safIDs = new ArrayList<Long>();
    }

    /**
     * Initialize transaction manager.
     */
    protected void initializeTransactionManager() {
        try {
            transactionManager.initialize(
                    applicationContext,
                    terminalConfig,
                    transactionConfig,
                    gatewayConfig,
                    databaseConfig
            );
        } catch (InitializationException ex) {
            ex.printStackTrace();
        }
    }

    private Context getMobyPairingContext() {
        if (mobyPairingContext == null) {
            return applicationContext;
        }
        return mobyPairingContext;
    }

    @Override
    public void uploadSAF() {
        if (transactionManager == null) {
            transactionManager = TransactionManager.getInstance();
        }
        transactionManager.processAllSafTransactions(new SafListenerImpl());
    }

    /**
     * Check if force SAF is currently enabled.
     * @return True if force SAF is properly enabled, false otherwise.
     */
    @Override
    public boolean isForcedSafEnabled() {
        if (transactionManager != null) {
            return transactionManager.isForceSafEnabled();
        }
        return false;
    }

    /**
     * Sets force SAF enabled. This will have no effect if SAF is not enabled.
     * @param forcedSaf
     */
    @Override
    public void setForcedSafEnabled(boolean forcedSaf) {
        if (transactionManager == null) {
            transactionManager = TransactionManager.getInstance();
        }
        transactionManager.setForceSafEnabled(forcedSaf);
    }

    @Override
    public void acknowledgeSAFTransaction(String uniqueSafId) {
        if (transactionManager != null) {
            transactionManager.acknowledgeSAFTransaction(uniqueSafId);
        }
    }

    /**
     * Set the context for the pairing Dialog Moby Device.
     *
     * @param context the context
     */
    public void setMobyPairingContext(Context context) {
        this.mobyPairingContext = context;
    }

    /**
     * Set the device listener to get Device events.
     *
     * @param deviceListener the device listener
     */
    public void setDeviceListener(DeviceListener deviceListener) {
        this.deviceListener = deviceListener;
    }

    /**
     * Set the Transaction Listener to receive Transaction events
     *
     * @param transactionListener the transaction listener
     */
    public void setTransactionListener(TransactionListener transactionListener) {
        this.transactionListener = transactionListener;
    }

    public void setSafListener(SafListener safListener) {
        this.safListener = safListener;
    }

    public void setAvailableTerminalVersionsListener(
            AvailableTerminalVersionsListener availableTerminalVersionsListener) {
        this.availableTerminalVersionsListener = availableTerminalVersionsListener;
    }

    public void setUpdateTerminalListener(UpdateTerminalListener updateTerminalListener) {
        this.updateTerminalListener = updateTerminalListener;
    }

    /**
     * Initialize is used to start the connection
     */
    public void initialize() {
        switch (connectionConfig.getConnectionMode()) {
            case USB:
                startConnect();
                break;
            default:
                scan();
                break;
        }
    }

    private void scan() {
        isScanned = true;
        if (!isDeviceSelected) {
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
        }
        startConnect();
    }

    /**
     * Connect with the device address(MAC)
     *
     * @param deviceName the device name
     */
    public void connect(String deviceName) {
        terminalConfig.setHost(deviceName);
        startConnect();
    }

    /**
     * Connect with the device object
     *
     * @param device the device
     */
    public void connect(BluetoothDevice device) {
        isDeviceSelected = true;
        connect(device.getAddress());
    }

    /**
     * Disconnect the connected device
     */
    public void disconnect() {
        if (isTransactionManagerConnected()) {
            transactionManager.disconnect();
        }
        isScanned = false;
    }

    /**
     * Check the status of connection
     *
     * @return boolean
     */
    public boolean isConnected() {
        return isTransactionManagerConnected();
    }

    /**
     * Get the device Information
     */
    public void getDeviceInfo() {
        if (isTransactionManagerConnected()) {
            transactionManager.getDeviceInfo(new TerminalInfoListenerImpl());
        }
    }

    private void startConnect() {
        Timber.d("startConnect() called.");
        if (isTransactionManagerConnected()) {
            return;
        }

        if (transactionManager != null && terminalConfig != null) {
            ConnectionType[] connectionTypes =
                    transactionManager.getSupportedTerminalConnectionTypes(terminalConfig.getTerminalType());

            if (connectionTypes.length > 1) {
                if (terminalConfig.getConnectionType() == connectionTypes[0]) {
                    terminalConfig.setConnectionType(connectionTypes[0]);
                } else {
                    terminalConfig.setConnectionType(connectionTypes[1]);
                }
            } else {
                terminalConfig.setConnectionType(connectionTypes[0]);
            }

            if (isScanned) {
                terminalConfig.setHost(null);
                isScanned = false;
            }
            initializeTransactionManager();

            if (transactionManager.isInitialized()) {
                Timber.d("TransactionManager isInitialized() called");
                transactionManager.connect(new ConnectionListenerImpl());
                transactionManager.updateTransactionListener(new TransactionListenerImpl());
            }
        }
    }

    /**
     * Is transaction manager connected boolean.
     *
     * @return the boolean
     */
    protected boolean isTransactionManagerConnected() {
        if (transactionManager == null) {
            transactionManager = TransactionManager.getInstance();
        }
        return transactionManager != null && transactionManager.isConnected();
    }

    @Override
    public void doTransaction(TransactionRequest transactionRequest) {
        if (transactionManager == null) {
            transactionManager = TransactionManager.getInstance();
        }

        if (!transactionManager.isInitialized()) {
            initializeTransactionManager();
        }

        transactionManager.startTransaction(transactionRequest, new TransactionListenerImpl(),
                new SafListenerImpl());
    }

    /**
     * Start an SVA (gift card) transaction. This will return the gift card data by way of the TransactionListener
     * function onTransactionComplete().
     */
    public void doSvaStartCard() {
        if (transactionManager == null) {
            transactionManager = TransactionManager.getInstance();
        }

        if (!transactionManager.isInitialized()) {
            initializeTransactionManager();
        }
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTransactionType(TransactionType.SVA);
        transactionManager.startTransaction(transactionRequest, new TransactionListenerImpl(), null);
    }

    /**
     * Cancel the current transaction. No effect if there is no transaction active.
     */
    public void cancelTransaction() {
        if (transactionManager == null) {
            transactionManager = TransactionManager.getInstance();
        }
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

        com.tsys.payments.library.enums.TerminalUpdateType updateType =
                com.tsys.payments.library.enums.TerminalUpdateType.FIRMWARE;
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

        com.tsys.payments.library.enums.TerminalUpdateType updateType =
                com.tsys.payments.library.enums.TerminalUpdateType.FIRMWARE;
        if (terminalUpdateType == TerminalUpdateType.CONFIG) {
            updateType = com.tsys.payments.library.enums.TerminalUpdateType.KERNEL;
        } else if (terminalUpdateType == TerminalUpdateType.RKI) {
            updateType = com.tsys.payments.library.enums.TerminalUpdateType.RKI;
        }

        transactionManager.updateTerminal(updateType, null, version, new UpdateTerminalListenerImpl());
    }

    public void sendCardholderInteractionResult(CardholderInteractionResult cardholderInteractionResult) {
        if (isTransactionManagerConnected()) {
            transactionManager.sendCardholderInteractionResult(map(cardholderInteractionResult));
        }
    }

    private com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo map(TerminalInfo info) {
        final com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo ti =
                new com.heartlandpaymentsystems.library.terminals.entities.TerminalInfo();
        ti.setAppName(info.getAppName());
        ti.setAppVersion(info.getAppVersion());
        ti.setBatteryLevel(info.getBatteryLevel());
        ti.setFirmwareVersion(info.getFirmwareVersion());
        ti.setKernelVersion(info.getKernelVersion());
        ti.setSerialNumber(info.getSerialNumber());
        ti.setTerminalType(info.getTerminalType());
        ti.setModel(info.getModel());
        ti.setModel(info.getManufacturer());
        return ti;
    }

    private com.heartlandpaymentsystems.library.terminals.enums.ErrorType map(
            com.tsys.payments.library.enums.ErrorType errorType) {
        com.heartlandpaymentsystems.library.terminals.enums.ErrorType result =
                com.heartlandpaymentsystems.library.terminals.enums.ErrorType.values()[errorType.ordinal()];
        return result;
    }

    private com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest map(
            CardholderInteractionRequest info) {
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

    private void showPairingDialog(final LedPairingConfirmationCallback ledPairingConfirmationCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getMobyPairingContext());
        builder.setTitle("Confirm Led Sequence");
        View dialogView;
        dialogView = LayoutInflater.from(getMobyPairingContext()).inflate(R.layout.dialog_pairing_led, null);
        pairingLedView = dialogView.findViewById(R.id.pairingLedView);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setNeutralButton("Restart",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ledPairingConfirmationCallback.restartLedPairingSequence();
                            }
                        }).start();
                    }
                });
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ledPairingConfirmationCallback.confirm();
                            }
                        }).start();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ledPairingConfirmationCallback.cancel();
                            }
                        }).start();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    /**
     * Bluetooth listener implementation.
     */
    protected class BluetoothListenerImpl implements BluetoothDiscoveryListener {

        @Override
        public void onDiscoveryStarted() {
            bluetoothDevices = new HashSet<>();
        }

        @Override
        public void onDiscoveryFinished() {
            if (deviceListener == null) {
                return;
            }
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

            if (foundDevice.getName() != null && foundDevice.getName().startsWith("MOB")) {
                bluetoothDevices.add(foundDevice);
                if (deviceListener != null) {
                    deviceListener.onBluetoothDeviceFound(foundDevice);
                }
            }
        }
    }

    /**
     * Led pairing listener implementation.
     */
    protected class LedPairingListenerImpl implements LedMobyPairingListener {

        @Override
        public void onLedPairSequence(Object led, Object confirm) {
            pairingLedView = new PairingLedView(getMobyPairingContext());
            List<LedSequence> sequenceList = (List<LedSequence>)led;
            LedPairingConfirmationCallback ledPairingConfirmationCallback
                    = (LedPairingConfirmationCallback)confirm;
            showPairingDialog(ledPairingConfirmationCallback);
            pairingLedView.show(sequenceList);
        }

        @Override
        public void onNotSupported() {
            Timber.d("MobyDevice callback :: LedPairingCallback -> notSupported");
        }

        @Override
        public void onSuccess() {
            Timber.d("MobyDevice callback :: LedPairingCallback -> success");
        }

        @Override
        public void onFail() {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (deviceListener != null) {
                java.lang.Error err = new java.lang.Error("Pairing Failed");
                deviceListener.onError(err, ErrorType.NOT_CONNECTED);
            }
            Timber.d("MobyDevice callback  :: LedPairingCallback -> failed");
        }

        @Override
        public void onCanceled() {
            if (deviceListener != null) {
                java.lang.Error err = new java.lang.Error("Pairing Canceled");
                deviceListener.onError(err, ErrorType.NOT_CONNECTED);
            }
            Timber.d("MobyDevice callback :: LedPairingCallback -> canceled");
        }
    }

    /**
     * The type Connection listener.
     */
    protected class ConnectionListenerImpl implements ConnectionListener {
        @Override
        public void onConnected(TerminalInfo terminalInfo) {
            if (deviceListener != null) {
                deviceListener.onConnected(map(terminalInfo));
            }
        }

        @Override
        public void onDisconnected() {
            if (deviceListener != null) {
                deviceListener.onDisconnected();
            }
            transactionManager = null;
        }

        @Override
        public void onError(Error error) {
            if (deviceListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                com.heartlandpaymentsystems.library.terminals.enums.ErrorType errorType = map(error.getType());
                deviceListener.onError(err, errorType);
            }
        }
    }

    /**
     * Transaction listener implementation.
     */
    protected class TransactionListenerImpl implements com.tsys.payments.library.transaction.TransactionListener {
        @Override
        public void onStatusUpdate(TransactionStatus transactionStatus) {
            if (transactionListener != null) {
                transactionListener.onStatusUpdate(
                        com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus.fromVitalSdk(
                                transactionStatus)
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
            Timber.d("onTransactionComplete - " + transactionResponse);
            Timber.d("onTransactionComplete id - " + transactionResponse.getPosReferenceNumber());
            if (transactionResponse.getTransactionResult() == TransactionResultType.SAF) {
                safIDs.add(Long.valueOf(transactionResponse.getPosReferenceNumber()));
            }
            if (transactionListener != null) {
                transactionListener.onTransactionComplete(
                        TerminalResponse.fromTransactionResponse(transactionResponse));
            }
        }

        @Override
        public void onError(Error error) {
            if (transactionListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                com.heartlandpaymentsystems.library.terminals.enums.ErrorType errorType = map(error.getType());
                transactionListener.onError(err, errorType);
            }
        }
    }

    /**
     * Terminal info listener implementation.
     */
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
                com.heartlandpaymentsystems.library.terminals.enums.ErrorType errorType = map(error.getType());
                deviceListener.onError(err, errorType);
            }
            if (transactionListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                com.heartlandpaymentsystems.library.terminals.enums.ErrorType errorType = map(error.getType());
                transactionListener.onError(err, errorType);
            }
        }
    }

    protected class SafListenerImpl implements com.tsys.payments.library.db.SafListener {
        @Override
        public void onProcessingComplete(List<TransactionResponse> responses) {
            Timber.d("onProcessingComplete - count - " + responses.size());
            for (TransactionResponse transactionResponse : responses) {
                Timber.d("response: " + transactionResponse);
            }
            if (safListener != null) {
                safListener.onProcessingComplete(responses);
            }
        }

        @Override
        public void onAllSafTransactionsRetrieved(List<SafTransaction> obfuscatedSafTransactions) {
            Timber.d("onAllSafTransactionsRetrieved - count - " + obfuscatedSafTransactions.size());
            if (safListener != null) {
                safListener.onAllSafTransactionsRetrieved(obfuscatedSafTransactions);
            }
        }

        @Override
        public void onError(Error error) {
            Timber.d("onError - " + error);
            if (safListener != null) {
                safListener.onError(new java.lang.Error(error.getMessage()));
            }
        }

        @Override
        public void onTransactionStored(String id, int totalCount, BigDecimal totalAmount) {
            Timber.d("onTransactionStored - id - " + id + ", count - " + totalCount + ", amount - " + totalAmount);
            if (safListener != null) {
                safListener.onTransactionStored(id, totalCount, totalAmount);
            }
        }

        @Override
        public void onStoredTransactionComplete(String id, TransactionResponse transactionResponse) {
            Timber.d("onStoredTransactionComplete - id - " + id + ", response - " + transactionResponse);
            if (safListener != null) {
                safListener.onStoredTransactionComplete(id, transactionResponse);
            }
        }
    };

    protected class AvailableTerminalVersionsListenerImpl
            implements com.tsys.payments.library.terminal.AvailableTerminalVersionsListener {

        @Override
        public void onAvailableTerminalVersionsReceived(com.tsys.payments.library.enums.TerminalUpdateType type,
                List<String> versions) {
            if (availableTerminalVersionsListener != null) {
                TerminalUpdateType updateType = TerminalUpdateType.FIRMWARE;
                if (type == com.tsys.payments.library.enums.TerminalUpdateType.KERNEL) {
                    updateType = TerminalUpdateType.CONFIG;
                }
                availableTerminalVersionsListener.onAvailableTerminalVersionsReceived(updateType, versions);
            }
        }

        public void onTerminalVersionInfoError(Error error) {
            if (availableTerminalVersionsListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                availableTerminalVersionsListener.onTerminalVersionInfoError(err);
            }
        }
    };

    protected class UpdateTerminalListenerImpl implements com.tsys.payments.library.terminal.UpdateTerminalListener {

        @Override
        public void onProgress(@Nullable Double completionPercentage, @Nullable String progressMessage) {
            if (updateTerminalListener != null) {
                updateTerminalListener.onProgress(completionPercentage, progressMessage);
            }
        }

        public void onTerminalUpdateSuccess() {
            if (updateTerminalListener != null) {
                updateTerminalListener.onTerminalUpdateSuccess();
            }
        }

        public void onTerminalUpdateError(Error error) {
            if (updateTerminalListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                updateTerminalListener.onTerminalUpdateError(err);
            }
        }
    };
}
