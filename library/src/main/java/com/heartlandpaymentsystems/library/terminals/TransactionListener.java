package com.heartlandpaymentsystems.library.terminals;

import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.ErrorType;
import com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus;

public interface TransactionListener {
    void onStatusUpdate(TransactionStatus transactionStatus);
    void onCardholderInteractionRequested(CardholderInteractionRequest cardholderInteractionRequest);
    void onTransactionComplete(TerminalResponse transaction);
    void onError(Error error, ErrorType errorType);
}
