package com.heartlandpaymentsystems.library.terminals.entities;

import android.util.Log;
import com.heartlandpaymentsystems.library.terminals.IDeviceResponse;
import com.heartlandpaymentsystems.library.terminals.enums.ApplicationCryptogramType;
import com.heartlandpaymentsystems.library.terminals.enums.EntryMode;
import com.tsys.payments.library.domain.Receipt;
import com.tsys.payments.library.domain.TransactionResponse;
import com.tsys.payments.library.enums.TransactionResultType;
import com.tsys.payments.library.enums.TransactionType;
import java.math.BigDecimal;
import java.util.Date;

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
    private String cardBrandTxnId;
    private String signatureStatus;

    // - TRANSACTIONAL
    private Date rspDT;
    private String transactionType;
    private String entryMethod;

    private String maskedCardNumber;
    private String cardType;
    private EntryMode entryMode;
    private String approvalCode;

    private BigDecimal transactionAmount;
    private BigDecimal amountDue;
    private BigDecimal balanceAmount;

    private String cardholderName;
    private String cardBin;
    private boolean cardPresent;
    private String svaPan;
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
    private BigDecimal origTotal;
    private BigDecimal surchargeAmount;
    private String surchargeEligibility;

    // - EMV
    private String applicationPreferredName;
    private String applicationName;
    private String applicationId;
    private String applicationCryptogram;
    private ApplicationCryptogramType applicationCryptogramType;
    private String applicationCryptogramTypeS;
    private String authorizationResponse;
    private String issuerAuthenticationData;

    private String cardHolderVerificationMethod;
    private String terminalVerificationResult;
    private String terminalSerialNumber;
    private boolean storedResponse;
    private String lastResponseTransactionId;
    private String terminalStatusIndicator;

    // - DUPLICATE DATA
    private String mOriginalGatewayTxnId;
    private String mOriginalRspDT;
    private String mOriginalClientTxnId;
    private String mOriginalAuthCode;
    private String mOriginalRefNbr;
    private long mOriginalAuthAmt;
    private String mOriginalCardType;
    private String mOriginalCardNbrLast4;

    public TerminalResponse() {
    }

    public static TerminalResponse fromTransactionResponse(TransactionResponse transactionResponse) {
        TerminalResponse response = new TerminalResponse();

        if (transactionResponse.getTransactionType() == TransactionType.SVA
                && transactionResponse.getSvaData() != null) {
            response.setTransactionType(transactionResponse.getTransactionType().toString());
            response.setSvaPan(transactionResponse.getSvaData().get("SVA"));
            response.setExpirationDate(transactionResponse.getSvaData().get("Expiration"));
            return response;
        }
        Long approvedAmount = transactionResponse.getApprovedAmount();
        if (approvedAmount == null) {
            approvedAmount = 0L;
        }
        Long origTotal = transactionResponse.getOrigTotal();
        if (origTotal == null) {
            origTotal = 0L;
        }
        Long surchargeAmount = transactionResponse.getSurchargeAmount();
        if (surchargeAmount == null) {
            surchargeAmount = 0L;
        }
        response.setApprovedAmount((new BigDecimal(approvedAmount)).movePointLeft(2));
        response.setOrigTotal((new BigDecimal(origTotal)).movePointLeft(2));
        response.setSurchargeAmount((new BigDecimal(surchargeAmount)).movePointLeft(2));
        response.setSurchargeEligibility(transactionResponse.getSurchargeEligibility());
        response.setEntryMode(EntryMode.fromCardDataSourceType(transactionResponse.getCardDataSourceType()));
        //        transactionResponse.getCardType();
        response.setApprovalCode(transactionResponse.getGatewayAuthCode());
        response.setTransactionId(transactionResponse.getGatewayTransactionId());
        response.setMaskedCardNumber(transactionResponse.getMaskedPan());
        //        transactionResponse.getPosReferenceNumber();
        if (transactionResponse.getResponseMessages() != null) {
            response.setResponseText(
                    transactionResponse.getResponseMessages().get(transactionResponse.getTransactionType()));
        }
        //        transactionResponse.getTenderType();
        if (transactionResponse.getTransactionResult() != null) {
            response.setDeviceResponseCode(transactionResponse.getTransactionResult().toString());
        } else {
            response.setDeviceResponseCode(TransactionResultType.CANCELLED.name());
        }
        if (transactionResponse.getTipAmount() != null) {
            response.setTipAmount((new BigDecimal(transactionResponse.getTipAmount())).movePointLeft(2));
        }
        if (transactionResponse.getTransactionType() != null) {
            response.setTransactionType(transactionResponse.getTransactionType().toString());
        }

        if (transactionResponse.getResponseCodes() != null && transactionResponse.getTransactionType() != null) {
            response.setAuthorizationResponse(
                    transactionResponse.getResponseCodes().get(transactionResponse.getTransactionType()));
        }

        if (transactionResponse.getReceipt() != null) {
            Receipt receipt = transactionResponse.getReceipt();
            response.setTerminalSerialNumber(receipt.getTerminalSerialNumber());
            response.setTerminalRefNumber(receipt.getPosReferenceNumber());
            response.setTerminalStatusIndicator(receipt.getTerminalStatusIndicator());
            response.setCardType(receipt.getCardType().toString());
            response.setRspDT(receipt.getTransactionDateTime());
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
            if (receipt.getTransactionId() != null && !receipt.getTransactionId().isEmpty()) {
                response.setTransactionId(receipt.getTransactionId());
            }
            if (receipt.getTransactionType() != null) {
                response.setTransactionType(receipt.getTransactionType().toString());
            }
            //            receipt.getUnpredictableNumber();
            response.setIssuerAuthenticationData(receipt.getIssuerAuthenticationData());
        }

        response.setEntryMethod(response.getEntryMode().toString());
        switch (response.getEntryMode()){
            case NONE:
            case MANUAL:
                response.setCardPresent(false);
                break;
            default:
                response.setCardPresent(true);
                break;
        }
        response.setOriginalGatewayTxnId(transactionResponse.getOriginalGatewayTxnId());
        response.setOriginalRspDT(transactionResponse.getOriginalRspDT());
        response.setOriginalClientTxnId(transactionResponse.getOriginalClientTxnId());
        response.setOriginalAuthCode(transactionResponse.getOriginalAuthCode());
        response.setOriginalRefNbr(transactionResponse.getOriginalRefNbr());
        response.setOriginalAuthAmt(transactionResponse.getOriginalAuthAmt());
        response.setOriginalCardType(transactionResponse.getOriginalCardType());
        response.setOriginalCardNbrLast4(transactionResponse.getOriginalCardNbrLast4());

        response.setToken(transactionResponse.getToken());
        response.setCardBrandTxnId(transactionResponse.getCardBrandTxnId());

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

    public String getCardBrandTxnId() {
        return cardBrandTxnId;
    }

    public void setCardBrandTxnId(String cardBrandTxnId) {
        this.cardBrandTxnId = cardBrandTxnId;
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

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
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

    public BigDecimal getOrigTotal() {
        return origTotal;
    }

    public void setOrigTotal(BigDecimal origTotal) {
        this.origTotal = origTotal;
    }

    public BigDecimal getSurchargeAmount() {
        return surchargeAmount;
    }

    public void setSurchargeAmount(BigDecimal surchargeAmount) {
        this.surchargeAmount = surchargeAmount;
    }

    public String getSurchargeEligibility() {
        return surchargeEligibility;
    }

    public void setSurchargeEligibility(String surchargeEligibility) {
        this.surchargeEligibility = surchargeEligibility;
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

    public Date getRspDT() {
        return rspDT;
    }

    public void setRspDT(Date date) {
        rspDT = date;
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

    public String getAuthorizationResponse() {
        return authorizationResponse;
    }

    public void setAuthorizationResponse(String authorizationResponse) {
        this.authorizationResponse = authorizationResponse;
    }

    public String getIssuerAuthenticationData() {
        return issuerAuthenticationData;
    }

    public void setIssuerAuthenticationData(String issuerAuthenticationData) {
        this.issuerAuthenticationData = issuerAuthenticationData;
    }

    public String getTerminalStatusIndicator() {
        return terminalStatusIndicator;
    }

    public void setTerminalStatusIndicator(String terminalStatusIndicator) {
        this.terminalStatusIndicator = terminalStatusIndicator;
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

    public String getOriginalGatewayTxnId() {
        return mOriginalGatewayTxnId;
    }

    public void setOriginalGatewayTxnId(String originalGatewayTxnId) {
        mOriginalGatewayTxnId = originalGatewayTxnId;
    }

    public String getOriginalRspDT() {
        return mOriginalRspDT;
    }

    public void setOriginalRspDT(String originalRspDT) {
        mOriginalRspDT = originalRspDT;
    }

    public String getOriginalClientTxnId() {
        return mOriginalClientTxnId;
    }

    public void setOriginalClientTxnId(String originalClientTxnId) {
        mOriginalClientTxnId = originalClientTxnId;
    }

    public String getOriginalAuthCode() {
        return mOriginalAuthCode;
    }

    public void setOriginalAuthCode(String originalAuthCode) {
        mOriginalAuthCode = originalAuthCode;
    }

    public String getOriginalRefNbr() {
        return mOriginalRefNbr;
    }

    public void setOriginalRefNbr(String originalRefNbr) {
        mOriginalRefNbr = originalRefNbr;
    }

    public long getOriginalAuthAmt() {
        return mOriginalAuthAmt;
    }

    public void setOriginalAuthAmt(long originalAuthAmt) {
        mOriginalAuthAmt = originalAuthAmt;
    }

    public String getOriginalCardType() {
        return mOriginalCardType;
    }

    public void setOriginalCardType(String originalCardType) {
        mOriginalCardType = originalCardType;
    }

    public String getOriginalCardNbrLast4() {
        return mOriginalCardNbrLast4;
    }

    public void setOriginalCardNbrLast4(String originalCardNbrLast4) {
        mOriginalCardNbrLast4 = originalCardNbrLast4;
    }

    public void setSvaPan(String pan) {
        svaPan = pan;
    }

    public String getSvaPan(){
        return svaPan;
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
                ", entryMode='" + entryMode + '\''+
                ", approvalCode='" + approvalCode + '\'' +
                ", transactionAmount='" + transactionAmount + '\'' +
                ", amountDue='" + amountDue + '\'' +
                ", balanceAmount='" + balanceAmount + '\'' +
                ", cardholderName='" + cardholderName + '\'' +
                ", cardBin='" + cardBin + '\'' +
                ", cardPresent='" + cardPresent + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", tipAmount='" + tipAmount + '\'' +
                ", cashBackAmount='" + cashBackAmount + '\'' +
                ", avsResultCode='" + avsResultCode + '\'' +
                ", avsResultText='" + avsResultText + '\'' +
                ", cvvResponseCode='" + cvvResponseCode + '\'' +
                ", cvvResponseText='" + cvvResponseText + '\'' +
                ", taxExempt='" + taxExempt + '\'' +
                ", taxExemptId='" + taxExemptId + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", approvedAmount='" + approvedAmount + '\'' +
                ", origTotal='" + origTotal + '\'' +
                ", surchargeEligibility='" + surchargeEligibility + '\'' +
                ", surchargeAmount='" + surchargeAmount + '\'' +
                ", applicationPreferredName='" + applicationPreferredName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", applicationCryptogram='" + applicationCryptogram + '\'' +
                ", applicationCryptogramType='" + applicationCryptogramType +
                ", applicationCryptogramTypeS='" + applicationCryptogramTypeS + '\'' +
                ", cardHolderVerificationMethod='" + cardHolderVerificationMethod + '\'' +
                ", terminalVerificationResult='" + terminalVerificationResult + '\'' +
                ", authorizationResponse='" + authorizationResponse + '\'' +
                ", issuerAuthenticationData='" + issuerAuthenticationData + '\'' +
                ", terminalSerialNumber='" + terminalSerialNumber + '\'' +
                ", terminalStatusIndicator='" + terminalStatusIndicator + '\'' +
                ", storedResponse='" + storedResponse + '\'' +
                ", lastResponseTransactionId='" + lastResponseTransactionId + '\'' +
                '}';
    }
}
