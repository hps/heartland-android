package com.example.app;

import static com.example.app.Dialogs.hideProgress;
import static com.example.app.Dialogs.showProgress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.heartlandpaymentsystems.library.terminals.TransactionListener;
import com.heartlandpaymentsystems.library.terminals.c2x.CreditAdjustBuilder;
import com.heartlandpaymentsystems.library.terminals.c2x.CreditReturnBuilder;
import com.heartlandpaymentsystems.library.terminals.c2x.CreditSaleBuilder;
import com.heartlandpaymentsystems.library.terminals.c2x.CreditVoidBuilder;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionResult;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus;

import java.math.BigDecimal;

public class C2XTransactionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "C2XTransactionActivity";

    private Button creditSaleButton;
    private Button tipAdjustButton;
    private Button creditReturnButton;
    private Button creditVoidButton;
    private static MainActivity.TransactionState transactionState = MainActivity.TransactionState.None;
    private static String transactionId;
    private static String cardReaderStatus;
    private static String transactionResult;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c2x_transaction);

        MainActivity.c2XDevice.setTransactionListener(transactionListener);

        creditSaleButton = findViewById(R.id.creditsale_button);
        tipAdjustButton = findViewById(R.id.tipadjust_button);
        creditReturnButton = findViewById(R.id.creditreturn_button);
        creditVoidButton = findViewById(R.id.creditvoid_button);

        findViewById(R.id.creditsale_button).setOnClickListener(this);
        findViewById(R.id.tipadjust_button).setOnClickListener(this);
        findViewById(R.id.creditreturn_button).setOnClickListener(this);
        findViewById(R.id.creditvoid_button).setOnClickListener(this);

        updateTransactionStatus();
    }

    private void updateTransactionStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView transactionStatus = findViewById(R.id.transaction_status);
                switch (transactionState) {
                    case None:
                        transactionStatus.setText(transactionState.toString());
                        break;
                    case Processing:
                        transactionStatus.setText(transactionState + " - " + cardReaderStatus + " - " + transactionId);
                        break;
                    case Complete:
                        transactionStatus.setText(transactionState + " - " + transactionResult + " - " + transactionId);
                        break;
                }
                creditSaleButton.setEnabled(transactionState != MainActivity.TransactionState.Processing);
                tipAdjustButton.setEnabled(transactionState != MainActivity.TransactionState.Processing);
                creditReturnButton.setEnabled(transactionState != MainActivity.TransactionState.Processing);
                creditVoidButton.setEnabled(transactionState != MainActivity.TransactionState.Processing);
            }
        });
    }

    private TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void onStatusUpdate(TransactionStatus transactionStatus) {
            Log.d(TAG, "onStatusUpdate - " + transactionStatus.toString());
            cardReaderStatus = transactionStatus.toString().replaceAll("_", " ");
            transactionState = MainActivity.TransactionState.Processing;
            updateTransactionStatus();
            if(!transactionStatus.name().equals(TransactionStatus.NONE.name())) {
                showProgress(C2XTransactionActivity.this, "Status", transactionStatus.name());
            }
        }

        @Override
        public void onCardholderInteractionRequested(CardholderInteractionRequest cardholderInteractionRequest) {
            showProgress(C2XTransactionActivity.this, "Status", "Processing...");
            Log.d(TAG, "onCardholderInteractionRequested - " + cardholderInteractionRequest.getCardholderInteractionType());
            // prompt user for action
            CardholderInteractionResult result;
            switch (cardholderInteractionRequest.getCardholderInteractionType()) {
                case EMV_APPLICATION_SELECTION:
                    String[] applications =
                            cardholderInteractionRequest.getSupportedApplications();
                    // prompt user to select desired application
                    // send result
                    result = new CardholderInteractionResult(
                            cardholderInteractionRequest.getCardholderInteractionType()
                    );
                    result.setSelectedAidIndex(0);
                    MainActivity.c2XDevice.sendCardholderInteractionResult(result);
                    break;
                case FINAL_AMOUNT_CONFIRMATION:
                    // prompt user to confirm final amount
                    result = new CardholderInteractionResult(
                            cardholderInteractionRequest.getCardholderInteractionType()
                    );
                    result.setFinalAmountConfirmed(true);
                    MainActivity.c2XDevice.sendCardholderInteractionResult(result);
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onTransactionComplete(TerminalResponse transaction) {
            Log.d(TAG, "onTransactionComplete - " + transaction.toString());
            hideProgress();
            transactionId = transaction.getTransactionId();
            transactionResult = transaction.getDeviceResponseCode();
            transactionState = MainActivity.TransactionState.Complete;
            updateTransactionStatus();

            showAlertDialog(getString(R.string.transaction_complete), Dialogs.constructTransactionMessage(transaction));
        }

        @Override
        public void onError(Error error) {
            Log.e(TAG, "onError - " + error.getMessage());
            hideProgress();
            transactionResult = error.getMessage();
            transactionState = MainActivity.TransactionState.Complete;
            updateTransactionStatus();

            showAlertDialog(getString(R.string.transaction_error), error.getMessage());
        }
    };



    @Override
    public void onClick(View view) {
        String amount;
        switch(view.getId()) {
            case R.id.creditsale_button:
                if (!MainActivity.c2XDevice.isConnected()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_connected));
                    return;
                }

                amount = ((EditText) findViewById(R.id.transaction_amount)).getText().toString();

                if (amount.isEmpty()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_no_amount));
                    return;
                }

                CreditSaleBuilder creditSaleBuilder = new CreditSaleBuilder(MainActivity.c2XDevice);
                creditSaleBuilder.setAmount(new BigDecimal(amount));
                try {
                    creditSaleBuilder.execute();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tipadjust_button:
                if (transactionId == null) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_no_transaction_id));
                    return;
                }

                amount = ((EditText) findViewById(R.id.transaction_amount)).getText().toString();
                String gratuity = ((EditText) findViewById(R.id.gratuity_amount)).getText().toString();

                if (amount.isEmpty()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_no_amount));
                    return;
                }
                if (gratuity.isEmpty()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_no_gratuity));
                    return;
                }

                CreditAdjustBuilder creditAdjustBuilder = new CreditAdjustBuilder(MainActivity.c2XDevice);
                creditAdjustBuilder.setTransactionId(transactionId);
                creditAdjustBuilder.setAmount(new BigDecimal(amount));
                creditAdjustBuilder.setGratuity(new BigDecimal(gratuity));
                try {
                    creditAdjustBuilder.execute();
                    showProgress(C2XTransactionActivity.this, "Status", "Processing...");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case R.id.creditreturn_button:
                if (transactionId == null) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_no_transaction_id));
                    return;
                }

                amount = ((EditText) findViewById(R.id.transaction_amount)).getText().toString();

                if (amount.isEmpty()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_no_amount));
                    return;
                }

                CreditReturnBuilder creditReturnBuilder = new CreditReturnBuilder(MainActivity.c2XDevice);
                creditReturnBuilder.setTransactionId(transactionId);
                creditReturnBuilder.setAmount(new BigDecimal(amount));
                try {
                    creditReturnBuilder.execute();
                    showProgress(C2XTransactionActivity.this, "Status", "Processing...");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case R.id.creditvoid_button:
                if (transactionId == null) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_no_transaction_id));
                    return;
                }
                CreditVoidBuilder creditVoidBuilder = new CreditVoidBuilder(MainActivity.c2XDevice);
                creditVoidBuilder.setTransactionId(transactionId);
                try {
                    creditVoidBuilder.execute();
                    showProgress(C2XTransactionActivity.this, "Status", "Processing...");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void showAlertDialog(String title, String message) {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            Log.e(TAG, "showAlertDialog - dialog is already showing");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
    }

    private void hideAlertDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.hide();
        }
    }
}