package com.heartlandpaymentsystems.library.terminals.c2x;

import java.math.BigDecimal;

import com.tsys.payments.library.domain.TransactionRequest;
import com.tsys.payments.library.enums.TransactionType;

import com.heartlandpaymentsystems.library.terminals.IDevice;

public class CreditCaptureBuilder extends BaseBuilder {
    private String referenceNumber;
    private BigDecimal amount;
    private BigDecimal gratuity;
    private String transactionId;

    public CreditCaptureBuilder(C2XDevice device) {
        super((IDevice) device);
    }

    @Override
    protected TransactionRequest buildRequest() {
        TransactionRequest request = super.buildRequest();

        request.setTransactionType(TransactionType.CAPTURE);
        request.setGatewayTransactionId(transactionId);

        if (amount != null) {
            request.setTotal(amount.movePointRight(2).longValue());
        }

        if (gratuity != null) {
            request.setTip(gratuity.movePointRight(2).longValue());
        }

        request.setPosReferenceNumber(referenceNumber);

        return request;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getGratuity() {
        return gratuity;
    }

    public void setGratuity(BigDecimal gratuity) {
        this.gratuity = gratuity;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
