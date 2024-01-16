package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.heartlandpaymentsystems.library.terminals.IDevice;

public class TransactionListActivity extends BaseTransactionActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        findViewById(R.id.creditsale_button).setOnClickListener(this);
        findViewById(R.id.creditadjust_button).setOnClickListener(this);
        findViewById(R.id.creditauth_button).setOnClickListener(this);
        findViewById(R.id.creditcapture_button).setOnClickListener(this);
        findViewById(R.id.creditreturn_button).setOnClickListener(this);
        findViewById(R.id.creditvoid_button).setOnClickListener(this);
        findViewById(R.id.batchclose_button).setOnClickListener(this);
        findViewById(R.id.giftcard_button).setOnClickListener(this);
        findViewById(R.id.uploadsaf_button).setOnClickListener(this);
        findViewById(R.id.forcesaf_button).setOnClickListener(this);
        findViewById(R.id.test_cancel_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        IDevice device;
        switch (view.getId()) {
            case R.id.creditsale_button:
                intent = new Intent(this, CreditSaleActivity.class);
                startActivity(intent);
                break;
            case R.id.creditadjust_button:
                intent = new Intent(this, CreditAdjustActivity.class);
                startActivity(intent);
                break;
            case R.id.creditauth_button:
                intent = new Intent(this, CreditAuthActivity.class);
                startActivity(intent);
                break;
            case R.id.creditcapture_button:
                intent = new Intent(this, CreditCaptureActivity.class);
                startActivity(intent);
                break;
            case R.id.creditreturn_button:
                intent = new Intent(this, CreditReturnActivity.class);
                startActivity(intent);
                break;
            case R.id.creditvoid_button:
                intent = new Intent(this, CreditVoidActivity.class);
                startActivity(intent);
                break;
            case R.id.batchclose_button:
                intent = new Intent(this, BatchCloseActivity.class);
                startActivity(intent);
                break;
            case R.id.giftcard_button:
                intent = new Intent(this, GiftCardActivity.class);
                startActivity(intent);
                break;
            case R.id.uploadsaf_button:
                device = MainActivity.c2XDevice != null ? MainActivity.c2XDevice : MainActivity.mobyDevice;
                if (device != null) {
                    device.uploadSAF();
                }
                break;
            case R.id.forcesaf_button:
                device = MainActivity.c2XDevice != null ? MainActivity.c2XDevice : MainActivity.mobyDevice;
                if (device != null) {
                    boolean currentSetting = device.isForcedSafEnabled();
                    device.setForcedSafEnabled(!currentSetting);

                    boolean newSetting = device.isForcedSafEnabled();
                    ((Button)view).setText(
                            newSetting ? getString(R.string.forcesaf_on) : getString(R.string.forcesaf_off));
                }
            case R.id.test_cancel_button:
                MainActivity.mobyDevice.cancelTransaction();
                break;
        }
    }
}