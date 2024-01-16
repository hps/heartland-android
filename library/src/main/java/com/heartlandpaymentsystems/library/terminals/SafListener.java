package com.heartlandpaymentsystems.library.terminals;

import com.tsys.payments.library.db.entity.SafTransaction;
import com.tsys.payments.library.domain.TransactionResponse;
import java.math.BigDecimal;
import java.util.List;

public interface SafListener {
    void onProcessingComplete(List<TransactionResponse> responses);
    void onAllSafTransactionsRetrieved(List<SafTransaction> obfuscatedSafTransactions);
    void onError(Error error);
    void onTransactionStored(String id, int totalCount, BigDecimal totalAmount);
    void onStoredTransactionComplete(String id, TransactionResponse transactionResponse);
}
