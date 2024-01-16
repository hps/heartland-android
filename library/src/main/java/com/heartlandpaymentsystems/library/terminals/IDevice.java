package com.heartlandpaymentsystems.library.terminals;

import com.tsys.payments.library.domain.TransactionRequest;

public interface IDevice {
    void doTransaction(TransactionRequest transactionRequest);
    void cancelTransaction();
    void uploadSAF();
    boolean isForcedSafEnabled();
    void setForcedSafEnabled(boolean forcedSaf);
    void acknowledgeSAFTransaction(String uniqueSafId);
}
