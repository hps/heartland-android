package com.heartlandpaymentsystems.library.terminals;

public interface IDeviceInterface {
    public String getStatus();
    public void setStatus(String status);
    public String getCommand();
    public void setCommand(String command);
    public String getVersion();
    public void setVersion(String version);
    public String getDeviceResponseCode();
    public void setDeviceResponseCode(String deviceResponseCode);
    public String getDeviceResponseMessage();
    public void setDeviceResponseMessage(String deviceResponseMessage);
    public boolean getStoredResponse();
    public void setStoredResponse(boolean storedResponse);
    public String getTransactionId();
    public void setTransactionId(String transactionId);
    public String getLastResponseTransactionId();
    public void setLastResponseTransactionId(String lastResponseTransactionId);
    public String toString();
}
