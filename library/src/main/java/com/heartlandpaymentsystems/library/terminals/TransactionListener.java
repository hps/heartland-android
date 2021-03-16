package com.heartlandpaymentsystems.library.terminals;

import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus;
import com.tsys.payments.library.domain.CardholderInteractionRequest;

public interface TransactionListener {
    void onStatusUpdate(TransactionStatus transactionStatus);
    void onCardholderInteractionRequested(CardholderInteractionRequest cardholderInteractionRequest);
    void onTransactionComplete(TerminalResponse transaction);
    void onError(Error error);
}
