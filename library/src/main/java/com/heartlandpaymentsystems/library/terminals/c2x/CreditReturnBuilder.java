package com.heartlandpaymentsystems.library.terminals.c2x;

import java.math.BigDecimal;

import com.tsys.payments.library.domain.TransactionRequest;
import com.tsys.payments.library.enums.TransactionType;

import com.heartlandpaymentsystems.library.terminals.IDevice;

public class CreditReturnBuilder extends BaseBuilder {
    private String referenceNumber;
    private String transactionId;
    private BigDecimal amount;

    public CreditReturnBuilder(C2XDevice device) {
        super((IDevice) device);
    }

    @Override
    protected TransactionRequest buildRequest() {
        TransactionRequest request = super.buildRequest();

        request.setTransactionType(TransactionType.REFUND);

        if (amount != null) {
            request.setTotal(amount.longValue());
        }

        request.setGatewayTransactionId(transactionId);
        request.setPosReferenceNumber(referenceNumber);

        return request;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
