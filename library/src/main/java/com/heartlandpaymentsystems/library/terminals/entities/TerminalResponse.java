package com.heartlandpaymentsystems.library.terminals.entities;

import com.heartlandpaymentsystems.library.terminals.IDeviceResponse;
import com.heartlandpaymentsystems.library.terminals.enums.ApplicationCryptogramType;
import com.heartlandpaymentsystems.library.terminals.enums.EntryMode;
import com.tsys.payments.library.domain.Receipt;
import com.tsys.payments.library.domain.TransactionResponse;

import java.math.BigDecimal;

public class TerminalResponse implements IDeviceResponse {
    // - INTERNAL
    private String status;
    private String command;
    private String version;

    // - FUNCTIONAL
    private String deviceResponseCode;
    private String deviceResponseMessage;
    private String responseText;
    private String transactionId;
    private String terminalRefNumber;
    private String token;
    private String signatureStatus;

    // - TRANSACTIONAL
    private String transactionType;
    private String entryMethod;

    private String maskedCardNumber;
    private EntryMode entryMode;
    private String approvalCode;

    private BigDecimal transactionAmount;
    private BigDecimal amountDue;
    private BigDecimal balanceAmount;

    private String cardholderName;
    private String cardBin;
    private boolean cardPresent;
    private String expirationDate;
    private BigDecimal tipAmount;
    private BigDecimal cashBackAmount;
    private String avsResultCode;
    private String avsResultText;
    private String cvvResponseCode;
    private String cvvResponseText;
    private boolean taxExempt;
    private String taxExemptId;
    private String paymentType;

    private BigDecimal approvedAmount;

    // - EMV
    private String applicationPreferredName;
    private String applicationName;
    private String applicationId;
    private String applicationCryptogram;
    private ApplicationCryptogramType applicationCryptogramType;
    private String applicationCryptogramTypeS;

    private String cardHolderVerificationMethod;
    private String terminalVerificationResult;
    private String terminalSerialNumber;
    private boolean storedResponse;
    private String lastResponseTransactionId;

    public TerminalResponse() {}

    public static TerminalResponse fromTransactionResponse(TransactionResponse transactionResponse) {
        TerminalResponse response = new TerminalResponse();

        Long approvedAmount = transactionResponse.getApprovedAmount();
        if (approvedAmount == null) {
            approvedAmount = 0L;
        }
        response.setApprovedAmount((new BigDecimal(approvedAmount)).movePointLeft(2));
        response.setEntryMode(EntryMode.fromCardDataSourceType(transactionResponse.getCardDataSourceType()));
//        transactionResponse.getCardType();
        response.setApprovalCode(transactionResponse.getGatewayAuthCode());
        response.setTransactionId(transactionResponse.getGatewayTransactionId());
        response.setMaskedCardNumber(transactionResponse.getMaskedPan());
//        transactionResponse.getPosReferenceNumber();
        response.setResponseText(transactionResponse.getResponseMessages().get(transactionResponse.getTransactionType()));
//        transactionResponse.getTenderType();
        response.setDeviceResponseCode(transactionResponse.getTransactionResult().toString());
        if (transactionResponse.getTipAmount() != null) {
            response.setTipAmount((new BigDecimal(transactionResponse.getTipAmount())).movePointLeft(2));
        }
        if (transactionResponse.getTransactionType() != null) {
            response.setTransactionType(transactionResponse.getTransactionType().toString());
        }

        if (transactionResponse.getReceipt() != null) {
            Receipt receipt = transactionResponse.getReceipt();
            response.setApplicationId(receipt.getAid());
//            receipt.getApplicationEffectiveDate();
//            receipt.getApplicationExpiryDate();
//            receipt.getApplicationInterchangeProfile();
            response.setApplicationName(receipt.getApplicationLabel());
//            receipt.getApplicationTransactionCounter();
//            receipt.getApplicationVersionNumber();
            response.setApprovalCode(receipt.getAuthorizationCode());
            response.setCardholderName(receipt.getCardholderName());
//            receipt.getCardType();
            if (receipt.getCashBackAmount() != null) {
                response.setCashBackAmount((new BigDecimal(receipt.getCashBackAmount())).movePointLeft(2));
            }
            response.setApplicationCryptogram(receipt.getCryptogram());
//            receipt.getCryptogramInformationData();
//            receipt.getCurrencyCode();
//            receipt.getCvmResult();
//            receipt.getIfdSerialNumber();
//            receipt.getInvoiceNumber();
//            receipt.getIssuerApplicationData();
            response.setMaskedCardNumber(receipt.getMaskedPan());
//            receipt.getPanSequenceNumber();
//            receipt.getPinStatement();
            response.setEntryMode(EntryMode.fromCardDataSourceType(receipt.getPosEntryMode()));
//            receipt.getTerminalCountryCode();
//            receipt.getTerminalType();
            response.setTerminalVerificationResult(receipt.getTerminalVerificationResult());
            if (receipt.getTransactionAmount() != null) {
                response.setTransactionAmount((new BigDecimal(receipt.getTransactionAmount())).movePointLeft(2));
            }
//            receipt.getTransactionDateTime();
            response.setTransactionId(receipt.getTransactionId());
            if (receipt.getTransactionType() != null) {
                response.setTransactionType(receipt.getTransactionType().toString());
            }
//            receipt.getUnpredictableNumber();
        }

        return response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDeviceResponseCode() {
        return deviceResponseCode;
    }

    public void setDeviceResponseCode(String deviceResponseCode) {
        this.deviceResponseCode = deviceResponseCode;
    }

    public String getDeviceResponseMessage() {
        return deviceResponseMessage;
    }

    public void setDeviceResponseMessage(String deviceResponseMessage) {
        this.deviceResponseMessage = deviceResponseMessage;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTerminalRefNumber() {
        return terminalRefNumber;
    }

    public void setTerminalRefNumber(String terminalRefNumber) {
        this.terminalRefNumber = terminalRefNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSignatureStatus() {
        return signatureStatus;
    }

    public void setSignatureStatus(String signatureStatus) {
        this.signatureStatus = signatureStatus;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getEntryMethod() {
        return entryMethod;
    }

    public void setEntryMethod(String entryMethod) {
        this.entryMethod = entryMethod;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public EntryMode getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(EntryMode entryMode) {
        this.entryMode = entryMode;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getCardBin() {
        return cardBin;
    }

    public void setCardBin(String cardBin) {
        this.cardBin = cardBin;
    }

    public boolean isCardPresent() {
        return cardPresent;
    }

    public void setCardPresent(boolean cardPresent) {
        this.cardPresent = cardPresent;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BigDecimal getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(BigDecimal tipAmount) {
        this.tipAmount = tipAmount;
    }

    public BigDecimal getCashBackAmount() {
        return cashBackAmount;
    }

    public void setCashBackAmount(BigDecimal cashBackAmount) {
        this.cashBackAmount = cashBackAmount;
    }

    public String getAvsResultCode() {
        return avsResultCode;
    }

    public void setAvsResultCode(String avsResultCode) {
        this.avsResultCode = avsResultCode;
    }

    public String getAvsResultText() {
        return avsResultText;
    }

    public void setAvsResultText(String avsResultText) {
        this.avsResultText = avsResultText;
    }

    public String getCvvResponseCode() {
        return cvvResponseCode;
    }

    public void setCvvResponseCode(String cvvResponseCode) {
        this.cvvResponseCode = cvvResponseCode;
    }

    public String getCvvResponseText() {
        return cvvResponseText;
    }

    public void setCvvResponseText(String cvvResponseText) {
        this.cvvResponseText = cvvResponseText;
    }

    public boolean isTaxExempt() {
        return taxExempt;
    }

    public void setTaxExempt(boolean taxExempt) {
        this.taxExempt = taxExempt;
    }

    public String getTaxExemptId() {
        return taxExemptId;
    }

    public void setTaxExemptId(String taxExemptId) {
        this.taxExemptId = taxExemptId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public String getApplicationPreferredName() {
        return applicationPreferredName;
    }

    public void setApplicationPreferredName(String applicationPreferredName) {
        this.applicationPreferredName = applicationPreferredName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationCryptogram() {
        return applicationCryptogram;
    }

    public void setApplicationCryptogram(String applicationCryptogram) {
        this.applicationCryptogram = applicationCryptogram;
    }

    public ApplicationCryptogramType getApplicationCryptogramType() {
        return applicationCryptogramType;
    }

    public void setApplicationCryptogramType(ApplicationCryptogramType applicationCryptogramType) {
        this.applicationCryptogramType = applicationCryptogramType;
    }

    public String getApplicationCryptogramTypeS() {
        return applicationCryptogramTypeS;
    }

    public void setApplicationCryptogramTypeS(String applicationCryptogramTypeS) {
        this.applicationCryptogramTypeS = applicationCryptogramTypeS;
    }

    public String getCardHolderVerificationMethod() {
        return cardHolderVerificationMethod;
    }

    public void setCardHolderVerificationMethod(String cardHolderVerificationMethod) {
        this.cardHolderVerificationMethod = cardHolderVerificationMethod;
    }

    public String getTerminalVerificationResult() {
        return terminalVerificationResult;
    }

    public void setTerminalVerificationResult(String terminalVerificationResult) {
        this.terminalVerificationResult = terminalVerificationResult;
    }

    public String getTerminalSerialNumber() {
        return terminalSerialNumber;
    }

    public void setTerminalSerialNumber(String terminalSerialNumber) {
        this.terminalSerialNumber = terminalSerialNumber;
    }

    public boolean isStoredResponse() {
        return storedResponse;
    }

    public void setStoredResponse(boolean storedResponse) {
        this.storedResponse = storedResponse;
    }

    public String getLastResponseTransactionId() {
        return lastResponseTransactionId;
    }

    public void setLastResponseTransactionId(String lastResponseTransactionId) {
        this.lastResponseTransactionId = lastResponseTransactionId;
    }

    @Override
    public String toString() {
        return "TerminalResponse{" +
                "status='" + status + '\'' +
                ", command='" + command + '\'' +
                ", version='" + version + '\'' +
                ", deviceResponseCode='" + deviceResponseCode + '\'' +
                ", deviceResponseMessage='" + deviceResponseMessage + '\'' +
                ", responseText='" + responseText + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", terminalRefNumber='" + terminalRefNumber + '\'' +
                ", token='" + token + '\'' +
                ", signatureStatus='" + signatureStatus + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", entryMethod='" + entryMethod + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", entryMode=" + entryMode +
                ", approvalCode='" + approvalCode + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", amountDue=" + amountDue +
                ", balanceAmount=" + balanceAmount +
                ", cardholderName='" + cardholderName + '\'' +
                ", cardBin='" + cardBin + '\'' +
                ", cardPresent=" + cardPresent +
                ", expirationDate='" + expirationDate + '\'' +
                ", tipAmount=" + tipAmount +
                ", cashBackAmount=" + cashBackAmount +
                ", avsResultCode='" + avsResultCode + '\'' +
                ", avsResultText='" + avsResultText + '\'' +
                ", cvvResponseCode='" + cvvResponseCode + '\'' +
                ", cvvResponseText='" + cvvResponseText + '\'' +
                ", taxExempt=" + taxExempt +
                ", taxExemptId='" + taxExemptId + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", approvedAmount=" + approvedAmount +
                ", applicationPreferredName='" + applicationPreferredName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", applicationCryptogram='" + applicationCryptogram + '\'' +
                ", applicationCryptogramType=" + applicationCryptogramType +
                ", applicationCryptogramTypeS='" + applicationCryptogramTypeS + '\'' +
                ", cardHolderVerificationMethod='" + cardHolderVerificationMethod + '\'' +
                ", terminalVerificationResult='" + terminalVerificationResult + '\'' +
                ", terminalSerialNumber='" + terminalSerialNumber + '\'' +
                ", storedResponse=" + storedResponse +
                ", lastResponseTransactionId='" + lastResponseTransactionId + '\'' +
                '}';
    }
}
