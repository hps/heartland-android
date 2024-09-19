package com.heartlandpaymentsystems.library.terminals.c2x;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.heartlandpaymentsystems.library.BuildConfig;
import com.heartlandpaymentsystems.library.terminals.SafListener;
import com.heartlandpaymentsystems.library.terminals.UpdateTerminalListener;
import com.heartlandpaymentsystems.library.terminals.AvailableTerminalVersionsListener;
import com.heartlandpaymentsystems.library.terminals.ConnectionConfig;
import com.heartlandpaymentsystems.library.terminals.DeviceListener;
import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.heartlandpaymentsystems.library.terminals.TransactionListener;
import com.heartlandpaymentsystems.library.terminals.UpdateTerminalListener;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionResult;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.Environment;
import com.heartlandpaymentsystems.library.terminals.enums.ErrorType;
import com.heartlandpaymentsystems.library.terminals.enums.TerminalUpdateType;
import com.heartlandpaymentsystems.library.terminals.receivers.BluetoothDiscoveryListener;
import com.heartlandpaymentsystems.library.terminals.receivers.BluetoothReceiver;
import com.tsys.payments.library.connection.ConnectionListener;
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
import com.tsys.payments.library.enums.CardholderInteractionType;
import com.tsys.payments.library.enums.ConnectionType;
import com.tsys.payments.library.enums.CurrencyCode;
import com.tsys.payments.library.enums.TerminalAuthenticationCapability;
import com.tsys.payments.library.enums.TerminalInputCapability;
import com.tsys.payments.library.enums.TerminalOperatingEnvironment;
import com.tsys.payments.library.enums.TerminalOutputCapability;
import com.tsys.payments.library.enums.TerminalType;
import com.tsys.payments.library.enums.TransactionResultType;
import com.tsys.payments.library.enums.TransactionStatus;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * C2X Device Implementation
 * Device: BBPOS C2X
 */
public class C2XDevice implements IDevice {

    private static final String TAG = C2XDevice.class.getSimpleName();

    private static boolean timberPlanted;
    private Context applicationContext;
    private ConnectionConfig connectionConfig;
    private TransactionConfiguration transactionConfig;
    private TerminalConfiguration terminalConfig;
    private GatewayConfiguration gatewayConfig;
    private DatabaseConfig databaseConfig;
    private TransactionManager transactionManager;
    private TerminalInfo connectedTerminalInfo;
    private DeviceListener deviceListener;
    private TransactionListener transactionListener;
    private SafListener safListener;
    private AvailableTerminalVersionsListener availableTerminalVersionsListener;
    private UpdateTerminalListener updateTerminalListener;
    private HashSet<BluetoothDevice> bluetoothDevices;
    private BluetoothReceiver bluetoothReceiver;

    private List<Long> safIDs;

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

    public void setSafListener(SafListener safListener) {
        this.safListener = safListener;
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

            initializeTransactionManager();

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

        transactionManager.startTransaction(transactionRequest, new TransactionListenerImpl(),
                new SafListenerImpl());
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
                    gatewayConfig,
                    databaseConfig
            );
        } catch (InitializationException ex) {
            ex.printStackTrace();
        }
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

    public void cancelScan() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;

        LibraryConfigHelper.setDebugMode(connectionConfig.getEnvironment().equals(Environment.TEST));
        LibraryConfigHelper.setSdkNameVersion("android;version=" + BuildConfig.VERSION_NAME);
        if (connectionConfig.getEnvironment().equals(Environment.TEST) && !timberPlanted) {
            Timber.plant(new Timber.DebugTree());
            timberPlanted = true;
        }

        if(connectionConfig.isSurchargeEnabled()){
            LibraryConfigHelper.setSurchargeEnabled(connectionConfig.isSurchargeEnabled());
        }

        transactionConfig = new TransactionConfiguration();
        transactionConfig.setChipEnabled(true);
        transactionConfig.setQuickChipEnabled(true);
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

        SafDatabaseConfig safDatabaseConfig = new SafDatabaseConfig(connectionConfig.isSafEnabled(),
                connectionConfig.getSafExpirationInDays(), TimeUnit.DAYS);
        databaseConfig = new DatabaseConfig(applicationContext, "c2xDB", null,
                safDatabaseConfig, GatewayType.PORTICO);
        safIDs = new ArrayList<Long>();
    }

    protected boolean isTransactionManagerConnected() {
        return transactionManager != null && transactionManager.isConnected();
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

    /**
     * Remote Key Injection
     */
    public void remoteKeyInjection() {
        updateTerminal(TerminalUpdateType.RKI, "");
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
        cr.setSurchargeAmount(info.getFinalSurchargeAmount());
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

    protected class ConnectionListenerImpl implements ConnectionListener {
        @Override
        public void onConnected(TerminalInfo terminalInfo) {
            connectedTerminalInfo = terminalInfo;

            if (deviceListener != null) {
                deviceListener.onConnected(map(terminalInfo));
            }
        }

        @Override
        public void onDisconnected() {
            connectedTerminalInfo = null;

            if (deviceListener != null) {
                deviceListener.onDisconnected();
            }
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

            if (foundDevice.getName() != null && foundDevice.getName().startsWith("CHB3")) {
                if (foundDevice.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC &&
                        foundDevice.getType() != BluetoothDevice.DEVICE_TYPE_LE &&
                        foundDevice.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
                    return;
                }
            } else {
                if (foundDevice.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC &&
                        foundDevice.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
                    return;
                }
            }

            if (foundDevice.getName() != null && foundDevice.getName().startsWith("CHB")) {
                bluetoothDevices.add(foundDevice);
                if (deviceListener != null) {
                    deviceListener.onBluetoothDeviceFound(foundDevice);
                }
            }
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
                if(cardholderInteractionRequest.getCardholderInteractionType() ==
                        CardholderInteractionType.SURCHARGE_REQUESTED){
                    Long amountBefore = cardholderInteractionRequest.getFinalTransactionAmount();
                    float surcharge = amountBefore * 0.03f;
                    Long finalAmount = (long)(amountBefore + surcharge);
                    cardholderInteractionRequest.setSurchargeAmount((long)surcharge);
                    cardholderInteractionRequest.setFinalTransactionAmount(finalAmount);
                }
                boolean interactionHandled = transactionListener.onCardholderInteractionRequested(map(cardholderInteractionRequest));
                if (!interactionHandled) {
                    CardholderInteractionResult result;
                    switch (cardholderInteractionRequest.getCardholderInteractionType()) {
                        case EMV_APPLICATION_SELECTION:
                            String[] applications =
                                    cardholderInteractionRequest.getSupportedApplications();
                            // send result
                            result = new CardholderInteractionResult(
                                    cardholderInteractionRequest.getCardholderInteractionType()
                            );
                            result.setSelectedAidIndex(0);
                            sendCardholderInteractionResult(result);
                            break;
                        case SURCHARGE_REQUESTED:
                            result = new CardholderInteractionResult(
                                    CardholderInteractionType.CARDHOLDER_SURCHARGE_CONFIRMATION);
                            result.setFinalAmountConfirmed(false);
                            sendCardholderInteractionResult(result);
                            Timber.e("Surcharge confirmation was not handled by client application, cancelling transaction");
                            break;
                        case FINAL_AMOUNT_CONFIRMATION:
                            result = new CardholderInteractionResult(
                                    cardholderInteractionRequest.getCardholderInteractionType()
                            );
                            result.setFinalAmountConfirmed(true);
                            sendCardholderInteractionResult(result);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        @Override
        public void onTransactionComplete(TransactionResponse transactionResponse) {
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
            if (deviceListener != null) {
                java.lang.Error err = new java.lang.Error(error.getMessage());
                com.heartlandpaymentsystems.library.terminals.enums.ErrorType errorType = map(error.getType());
                deviceListener.onError(err, errorType);
            }
        }
    }

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
}
