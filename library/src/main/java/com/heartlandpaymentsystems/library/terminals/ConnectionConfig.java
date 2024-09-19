package com.heartlandpaymentsystems.library.terminals;

import com.heartlandpaymentsystems.library.terminals.enums.BaudRate;
import com.heartlandpaymentsystems.library.terminals.enums.ConnectionMode;
import com.heartlandpaymentsystems.library.terminals.enums.DataBits;
import com.heartlandpaymentsystems.library.terminals.enums.Environment;
import com.heartlandpaymentsystems.library.terminals.enums.Parity;
import com.heartlandpaymentsystems.library.terminals.enums.StopBits;

public class ConnectionConfig {
    private ConnectionMode connectionMode;
    private String ipAddress;
    private String port;
    private BaudRate baudRate;
    private Parity parity;
    private StopBits stopBits;
    private DataBits dataBits;
    private long timeout;

    // credentials
    private String secretApiKey;
    private String username;
    private String licenseId;
    private String siteId;
    private String password;
    private String deviceId;

    // Surcharge
    private boolean surchargeEnabled;

    // saf options
    private boolean safEnabled;
    private int safExpirationInDays;

    private Environment environment;

    public ConnectionConfig() {
        timeout = 60000L;
        environment = Environment.TEST;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public BaudRate getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(BaudRate baudRate) {
        this.baudRate = baudRate;
    }

    public Parity getParity() {
        return parity;
    }

    public void setParity(Parity parity) {
        this.parity = parity;
    }

    public StopBits getStopBits() {
        return stopBits;
    }

    public void setStopBits(StopBits stopBits) {
        this.stopBits = stopBits;
    }

    public DataBits getDataBits() {
        return dataBits;
    }

    public void setDataBits(DataBits dataBits) {
        this.dataBits = dataBits;
    }

    public long getTimeout() {
        return timeout;
    }

    @Deprecated
    public void setTimeout(String timeout) {
        this.timeout = Long.getLong(timeout);
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getSecretApiKey() {
        return secretApiKey;
    }

    public void setSecretApiKey(String secretApiKey) {
        this.secretApiKey = secretApiKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Environment getEnvironment() { return environment; }

    public void setEnvironment(Environment environment) { this.environment = environment; }

    public boolean isSafEnabled() {
        return safEnabled;
    }

    public void setSafEnabled(boolean safEnabled) {
        this.safEnabled = safEnabled;
    }

    public int getSafExpirationInDays() {
        return safExpirationInDays;
    }

    public void setSafExpirationInDays(int safExpirationInDays) {
        this.safExpirationInDays = safExpirationInDays;
    }

    public boolean isSurchargeEnabled() {
        return surchargeEnabled;
    }

    public void setSurchargeEnabled(boolean surchargeEnabled) {
        this.surchargeEnabled = surchargeEnabled;
    }

}
