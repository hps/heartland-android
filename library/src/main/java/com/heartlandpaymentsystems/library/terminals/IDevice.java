package com.heartlandpaymentsystems.library.terminals;

import com.tsys.payments.library.domain.TransactionRequest;

public interface IDevice {
    void doTransaction(TransactionRequest transactionRequest);
    void cancelTransaction();
}
