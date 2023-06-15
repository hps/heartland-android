package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.heartlandpaymentsystems.library.entities.TransactionDetails;
import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.heartlandpaymentsystems.library.terminals.TransactionListener;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionResult;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus;
import com.heartlandpaymentsystems.library.terminals.transactions.CreditAuthBuilder;
import com.heartlandpaymentsystems.library.terminals.transactions.CreditCaptureBuilder;
import java.math.BigDecimal;
import static com.example.app.Dialogs.hideProgress;
import static com.example.app.Dialogs.showProgress;

public class CreditCaptureActivity extends BaseTransactionActivity {

    private static final String TAG = "CreditCaptureActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_capture);

        executeButton = findViewById(R.id.execute_button);

        if(MainActivity.c2XDevice != null) {
            MainActivity.c2XDevice.setTransactionListener(transactionListener);
        } else if (MainActivity.mobyDevice != null) {
            MainActivity.mobyDevice.setTransactionListener(transactionListener);
        }

        executeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.c2XDevice != null && !MainActivity.c2XDevice.isConnected()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_connected));
                    return;
                } else if(MainActivity.mobyDevice != null && !MainActivity.mobyDevice.isConnected()){
                    showAlertDialog(getString(R.string.error), getString(R.string.error_device_not_connected));
                    return;
                }

                String amount = ((EditText) findViewById(R.id.amount)).getText().toString();
                String transactionId = ((EditText) findViewById(R.id.transaction_id)).getText().toString();

                if (transactionId.isEmpty()) {
                    showAlertDialog(getString(R.string.error), getString(R.string.error_no_transaction_id));
                    return;
                }

                String gratuity = ((EditText) findViewById(R.id.gratuity_amount)).getText().toString();
                String clientTransactionId = ((EditText) findViewById(R.id.client_transaction_id)).getText().toString();
                boolean allowDuplicates = ((CheckBox) findViewById(R.id.creditadjust_allowduplicates)).isChecked();

                IDevice device = MainActivity.c2XDevice != null ? MainActivity.c2XDevice : MainActivity.mobyDevice;
                CreditCaptureBuilder creditCaptureBuilder = new CreditCaptureBuilder(device);
                creditCaptureBuilder.setTransactionId(transactionId);
                if (amount != null && !amount.isEmpty()) {
                    creditCaptureBuilder.setAmount(new BigDecimal(amount));
                }
                if (gratuity != null && !gratuity.isEmpty()) {
                    creditCaptureBuilder.setGratuity(new BigDecimal(gratuity));
                }
                if (clientTransactionId != null) {
                    creditCaptureBuilder.setReferenceNumber(clientTransactionId);
                }
                creditCaptureBuilder.setAllowDuplicates(allowDuplicates);
                try {
                    creditCaptureBuilder.execute();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        updateTransactionStatus();
    }
}