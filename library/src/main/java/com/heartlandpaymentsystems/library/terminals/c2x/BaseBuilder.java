package com.heartlandpaymentsystems.library.terminals.c2x;

import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.tsys.payments.library.domain.TransactionRequest;
import com.tsys.payments.library.utils.LibraryConfigHelper;

public abstract class BaseBuilder {
    private IDevice device;
    private boolean allowDuplicates;
    public BaseBuilder(IDevice device) {
        this.device = device;
    }

    public void execute() throws Exception {
        if (device == null) {
            throw new Exception("Missing configured device");
        }

        //set the value for allowing duplicates
        LibraryConfigHelper.setAllowDuplicates(allowDuplicates);

        device.doTransaction(buildRequest());
    }

    protected TransactionRequest buildRequest() {
        TransactionRequest request = new TransactionRequest();

        return request;
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }
}
