package com.heartlandpaymentsystems.library;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.heartlandpaymentsystems.library.entities.Token;

//This Activity is used for Testing
public class CardActivity extends AppCompatActivity implements CardFragmentInteractionListener {

    private static final String TAG = "HPSCardActivity";
    TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        resultTextView = findViewById(R.id.result);
        String publicKey = "pkapi_cert_P6dRqs1LzfWJ6HgGVZ";

        CardFragment cardFragment = CardFragment.newInstance(publicKey);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container, cardFragment).commit();
    }

    @Override
    public void onTokenSuccess(Token response) {
        resultTextView.setText(response.getTokenValue());
    }

    @Override
    public void onTokenFailure(String response) {
        resultTextView.setText(response);
    }
}
