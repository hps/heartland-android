package com.example.app;

import static com.example.app.Dialogs.hideProgress;
import static com.example.app.Dialogs.showProgress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.heartlandpaymentsystems.library.CardFragmentInteractionListener;
import com.heartlandpaymentsystems.library.controller.TokenService;
import com.heartlandpaymentsystems.library.entities.Card;
import com.heartlandpaymentsystems.library.entities.Token;
import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.heartlandpaymentsystems.library.terminals.TransactionListener;
import com.heartlandpaymentsystems.library.terminals.enums.ErrorType;
import com.heartlandpaymentsystems.library.terminals.transactions.CreditSaleBuilder;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionResult;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus;

import java.math.BigDecimal;

public class CardEntryActivity extends BaseActivity implements CardFragmentInteractionListener, View.OnClickListener {

    private static final String TAG = "CardEntryActivity";

    private Button creditSaleManualButton;
    private TextView resultTextView;
    private ProgressDialog mProgressDialog;
    private static MainActivity.TransactionState transactionState = MainActivity.TransactionState.None;
    private static String transactionId;
    private static String transactionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_entry);

        if (MainActivity.c2XDevice != null) {
            MainActivity.c2XDevice.setTransactionListener(transactionListener);
        } else if (MainActivity.mobyDevice != null) {
            MainActivity.mobyDevice.setTransactionListener(transactionListener);
        }

        creditSaleManualButton = findViewById(R.id.creditsale_manual_button);
        resultTextView = findViewById(R.id.tokenizeResult);
        final EditText cardNo = findViewById(com.heartlandpaymentsystems.library.R.id.card_no_edt);
        final EditText cardExpDate = findViewById(com.heartlandpaymentsystems.library.R.id.card_exp_date_edt);
        final EditText cardExpYear = findViewById(com.heartlandpaymentsystems.library.R.id.card_exp_yr_edt);
        final EditText cardCvv = findViewById(com.heartlandpaymentsystems.library.R.id.card_cvv_edt);
        final Button submitBtn = findViewById(com.heartlandpaymentsystems.library.R.id.submit);

        findViewById(R.id.creditsale_manual_button).setOnClickListener(this);

        updateTransactionStatus();

        if (isConnected()) {
            submitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view == submitBtn) {

                        hideKeyboard(CardEntryActivity.this, submitBtn);

                        try {
                            showProgressDialog();
                            TokenService tokenService = new TokenService(MainActivity.PUBLIC_KEY);
                            final Card card = new Card();
                            card.setNumber(cardNo.getText().toString());
                            card.setExpMonth(Integer.valueOf(cardExpDate.getText().toString()));
                            card.setExpYear(Integer.valueOf(cardExpYear.getText().toString()));
                            card.setCvv(cardCvv.getText().toString());

                            tokenService.getToken(card, new TokenService.TokenCallback() {
                                @Override
                                public Token onComplete(Token response) {
                                    hideProgressDialog();
                                    Log.d(TAG, "token service response: " + response.toString());
                                    if (response == null || response.getError() != null) {
                                        onTokenFailure(response.getError().getMessage());
                                    } else {
                                        onTokenSuccess(response);
                                    }
                                    return response;
                                }
                            });
                        }catch (NumberFormatException e) {
                            e.printStackTrace();
                            hideProgressDialog();
                        }
                    }
                }

            });
        } else {
            Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    protected void updateTransactionStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView transactionStatus = findViewById(R.id.transaction_status);
                switch (transactionState) {
                    case None:
                    case Processing:
                        transactionStatus.setText(transactionState.toString());
                        break;
                    case Complete:
                        transactionStatus.setText(transactionState + " - " + transactionResult + " - " + transactionId);
                        break;
                }
                creditSaleManualButton.setEnabled(transactionState != MainActivity.TransactionState.Processing);
            }
        });
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    public void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, getString(com.heartlandpaymentsystems.library.R.string.loading_msg),
                    getString(com.heartlandpaymentsystems.library.R.string.tokenizing));
            mProgressDialog.setCancelable(false);
        } else {
            mProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onTokenSuccess(Token response) {
        resultTextView.setText(response.getTokenValue());
    }

    @Override
    public void onTokenFailure(String response) {
        resultTextView.setText(response);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.creditsale_manual_button:
                String cardNo = ((EditText) findViewById(R.id.card_no_edt)).getText().toString();
                String month = ((EditText) findViewById(R.id.card_exp_date_edt)).getText().toString();
                String year = ((EditText) findViewById(R.id.card_exp_yr_edt)).getText().toString();
                String cvv = ((EditText) findViewById(R.id.card_cvv_edt)).getText().toString();
                int expMonth = 0;
                int expYear = 0;
                try {
                    expMonth = Integer.parseInt(month);
                    expYear = Integer.parseInt(year);
                }catch (NumberFormatException e) {
                    e.printStackTrace();
                    showAlertDialog(getString(R.string.error), getString(R.string.error_enter_valid_expiration));
                }

                //check for missing fields
                if (cardNo.isEmpty() || cvv.isEmpty()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_enter_valid_card));
                    return;
                }

                Card card = new Card();
                card.setNumber(cardNo);
                card.setExpMonth(expMonth);
                card.setExpYear(expYear);
                card.setCvv(cvv);
                IDevice device = MainActivity.c2XDevice != null ? MainActivity.c2XDevice : MainActivity.mobyDevice;
                CreditSaleBuilder builder = new CreditSaleBuilder(device);
                builder.setAmount(new BigDecimal("10.00"));
                builder.setCreditCard(card);
                try {
                    builder.execute();
                } catch (Throwable e) {
                    e.printStackTrace();
                    showAlertDialog(getString(R.string.error), getString(R.string.error_enter_valid_card));
                }
                break;
        }
    }

    private TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void onStatusUpdate(TransactionStatus transactionStatus) {
            Log.d(TAG, "onStatusUpdate - " + transactionStatus.toString());
            transactionState = MainActivity.TransactionState.Processing;
            updateTransactionStatus();
            if(!transactionStatus.name().equals(TransactionStatus.NONE.name())) {
                showProgress(CardEntryActivity.this, "Status", transactionStatus.name(), null);
            }
        }

        @Override
        public void onCardholderInteractionRequested(CardholderInteractionRequest cardholderInteractionRequest) {
            showProgress(CardEntryActivity.this, "Status", "Processing...", null);
            Log.d(TAG, "onCardholderInteractionRequested");
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
                    if (MainActivity.c2XDevice != null) {
                        MainActivity.c2XDevice.sendCardholderInteractionResult(result);
                    } else if (MainActivity.mobyDevice != null) {
                        MainActivity.mobyDevice.sendCardholderInteractionResult(result);
                    }
                    break;
                case FINAL_AMOUNT_CONFIRMATION:
                    // prompt user to confirm final amount
                    result = new CardholderInteractionResult(
                            cardholderInteractionRequest.getCardholderInteractionType()
                    );
                    result.setFinalAmountConfirmed(true);
                    if (MainActivity.c2XDevice != null) {
                        MainActivity.c2XDevice.sendCardholderInteractionResult(result);
                    } else if (MainActivity.mobyDevice != null) {
                        MainActivity.mobyDevice.sendCardholderInteractionResult(result);
                    }
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
        public void onError(Error error, ErrorType errorType) {
            Log.e(TAG, "onError - " + error.getMessage() + ", " + errorType);
            hideProgress();
            transactionResult = error.getMessage();
            transactionState = MainActivity.TransactionState.Complete;
            updateTransactionStatus();

            showAlertDialog(getString(R.string.transaction_error), error.getMessage());
        }
    };
}