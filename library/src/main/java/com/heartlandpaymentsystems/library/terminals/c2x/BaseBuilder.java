package com.heartlandpaymentsystems.library.terminals.c2x;

import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.tsys.payments.library.domain.TransactionRequest;

public abstract class BaseBuilder {
    private IDevice device;
    public BaseBuilder(IDevice device) {
        this.device = device;
    }

    public void execute() throws Exception {
        if (device == null) {
            throw new Exception("Missing configured device");
        }

        device.doTransaction(buildRequest());
    }

    protected TransactionRequest buildRequest() {
        TransactionRequest request = new TransactionRequest();

        return request;
    }
}
