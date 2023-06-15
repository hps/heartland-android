package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.heartlandpaymentsystems.library.entities.Card;
import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.heartlandpaymentsystems.library.terminals.TransactionListener;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionResult;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.ErrorType;
import com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus;
import com.heartlandpaymentsystems.library.utilities.ReceiptHelper;
import com.tsys.payments.library.enums.CardholderInteractionType;
import java.text.NumberFormat;
import static com.example.app.Dialogs.hideProgress;
import static com.example.app.Dialogs.showProgress;

public abstract class BaseTransactionActivity extends BaseActivity {

    private static final String TAG = "BaseTransactionActivity";

    protected Button executeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void updateTransactionStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView transactionStatus = findViewById(R.id.transaction_status);
                switch (MainActivity.transactionState) {
                    case None:
                        transactionStatus.setText(MainActivity.transactionState.toString());
                        break;
                    case Processing:
                        transactionStatus.setText(MainActivity.transactionState + " - " + MainActivity.cardReaderStatus + " - " + MainActivity.transactionId);
                        break;
                    case Complete:
                        transactionStatus.setText(MainActivity.transactionState + " - " + MainActivity.transactionResult + " - " + MainActivity.transactionId);
                        break;
                }
                executeButton.setEnabled(MainActivity.transactionState != MainActivity.TransactionState.Processing);
            }
        });
    }

    protected TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void onStatusUpdate(TransactionStatus transactionStatus) {
            Log.d(TAG, "onStatusUpdate - " + transactionStatus.toString());
            MainActivity.cardReaderStatus = transactionStatus.toString().replaceAll("_", " ");
            MainActivity.transactionState = MainActivity.TransactionState.Processing;
            updateTransactionStatus();
            if(!transactionStatus.name().equals(TransactionStatus.NONE.name())) {
                showProgress(BaseTransactionActivity.this, "Status", transactionStatus.name(), new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        IDevice device = MainActivity.c2XDevice != null ? MainActivity.c2XDevice : MainActivity.mobyDevice;
                        device.cancelTransaction();
                    }
                });
            }
        }

        @Override
        public void onCardholderInteractionRequested(CardholderInteractionRequest cardholderInteractionRequest) {
            Log.d(TAG, "onCardholderInteractionRequested - " + cardholderInteractionRequest.getCardholderInteractionType());
            // prompt user for action
            CardholderInteractionResult result;
            switch (cardholderInteractionRequest.getCardholderInteractionType()) {
                case EMV_APPLICATION_SELECTION:
                    String[] applications =
                            cardholderInteractionRequest.getSupportedApplications();
                    // send result
                    result = new CardholderInteractionResult(
                            cardholderInteractionRequest.getCardholderInteractionType()
                    );
                    result.setSelectedAidIndex(0);
                    if(MainActivity.c2XDevice != null) {
                        MainActivity.c2XDevice.sendCardholderInteractionResult(result);
                    } else {
                        MainActivity.mobyDevice.sendCardholderInteractionResult(result);
                    }
                    // prompt user to select desired application
                    /*Dialogs.showListDialog("Select", BaseTransactionActivity.this, applications, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // send result
                            result.setSelectedAidIndex(i);
                            if(MainActivity.c2XDevice != null) {
                                MainActivity.c2XDevice.sendCardholderInteractionResult(result);
                            } else {
                                MainActivity.mobyDevice.sendCardholderInteractionResult(result);
                            }
                        }
                    });*/

                    break;
                case FINAL_AMOUNT_CONFIRMATION:
                    // prompt user to confirm final amount
                    result = new CardholderInteractionResult(
                            cardholderInteractionRequest.getCardholderInteractionType()
                    );
                    result.setFinalAmountConfirmed(true);
                    if(MainActivity.c2XDevice != null) {
                        MainActivity.c2XDevice.sendCardholderInteractionResult(result);
                    } else {
                        MainActivity.mobyDevice.sendCardholderInteractionResult(result);
                    }
                    /*String finalAmount = NumberFormat.getCurrencyInstance().format((float)cardholderInteractionRequest.getFinalTransactionAmount()/100);
                    Dialogs.showListDialog("Confirm final amount of " + finalAmount,
                            BaseTransactionActivity.this, new String[] {"Yes", "No"},
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            result.setFinalAmountConfirmed(i == 0);
                            if(MainActivity.c2XDevice != null) {
                                MainActivity.c2XDevice.sendCardholderInteractionResult(result);
                            } else {
                                MainActivity.mobyDevice.sendCardholderInteractionResult(result);
                            }
                        }
                    });*/

                    break;
                default:
                    break;
            }

        }

        @Override
        public void onTransactionComplete(TerminalResponse transaction) {
            Log.d(TAG, "onTransactionComplete - " + transaction.toString());
            hideProgress();
            MainActivity.transactionId = transaction.getTransactionId();
            MainActivity.transactionResult = transaction.getDeviceResponseCode();
            MainActivity.transactionState = MainActivity.TransactionState.Complete;
            updateTransactionStatus();

            boolean showReceiptOption = (transaction.getAuthorizationResponse() != null &&
                    transaction.getAuthorizationResponse().equals("00")) &&
                    (transaction.getTransactionType().equals("SALE") ||
                    transaction.getTransactionType().equals("AUTH") ||
                    transaction.getTransactionType().equals("REFUND"));

            showAlertDialog(getString(R.string.transaction_complete), Dialogs.constructTransactionMessage(transaction), showReceiptOption, transaction);
        }

        @Override
        public void onError(Error error, ErrorType errorType) {
            Log.e(TAG, "onError - " + error.getMessage() + ", " + errorType);
            hideProgress();
            MainActivity.transactionResult = error.getMessage();
            MainActivity.transactionState = MainActivity.TransactionState.Complete;
            updateTransactionStatus();

            showAlertDialog(getString(R.string.transaction_error), error.getMessage());
        }
    };
}