package com.heartlandpaymentsystems.library.terminals.c2x;

import com.tsys.payments.library.domain.TransactionRequest;
import com.tsys.payments.library.enums.TransactionType;

import com.heartlandpaymentsystems.library.terminals.IDevice;

public class BatchCloseBuilder extends BaseBuilder {
    public BatchCloseBuilder(IDevice device) {
        super(device);
    }

    @Override
    protected TransactionRequest buildRequest() {
        TransactionRequest request = super.buildRequest();

        request.setTransactionType(TransactionType.BATCH_CLOSE);

        return request;
    }
}
