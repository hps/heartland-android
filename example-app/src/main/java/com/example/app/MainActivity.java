package com.example.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.heartlandpaymentsystems.library.CardFragment;
import com.heartlandpaymentsystems.library.CardFragmentInteractionListener;
import com.heartlandpaymentsystems.library.entities.Token;

public class MainActivity extends AppCompatActivity implements CardFragmentInteractionListener {

    public static final String TAG = "MainActivity";
    TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = findViewById(R.id.tokenizeResult);
        String publicKey = "";//Enter public key

        if (publicKey.length() != 0) {
            if (isConnected()) {
                CardFragment hpsCardFragment = CardFragment.newInstance(publicKey);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.main_container, hpsCardFragment).commit();
            } else {
                Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please provide public key", Toast.LENGTH_SHORT).show();
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
}
