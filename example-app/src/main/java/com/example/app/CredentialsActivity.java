package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class CredentialsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);

        findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //get values from fields
                String publicKey = ((EditText) findViewById(R.id.public_key)).getText().toString();
                String username = ((EditText) findViewById(R.id.username)).getText().toString();
                String password = ((EditText) findViewById(R.id.password)).getText().toString();
                String siteId = ((EditText) findViewById(R.id.site_id)).getText().toString();
                String deviceId = ((EditText) findViewById(R.id.device_id)).getText().toString();
                String licenseId = ((EditText) findViewById(R.id.license_id)).getText().toString();

                //set the new values
                MainActivity.PUBLIC_KEY = publicKey;
                MainActivity.USERNAME = username;
                MainActivity.PASSWORD = password;
                MainActivity.SITE_ID = siteId;
                MainActivity.DEVICE_ID = deviceId;
                MainActivity.LICENSE_ID = licenseId;

                //save the new values
                SharedPreferences.Editor editor =
                        getSharedPreferences(MainActivity.SAVED_PREFS, MODE_PRIVATE).edit();
                editor.putString(MainActivity.SAVED_PUBLIC_KEY, publicKey);
                editor.putString(MainActivity.SAVED_USERNAME, username);
                editor.putString(MainActivity.SAVED_PASSWORD, password);
                editor.putString(MainActivity.SAVED_SITE_ID, siteId);
                editor.putString(MainActivity.SAVED_DEVICE_ID, deviceId);
                editor.putString(MainActivity.SAVED_LICENSE_ID, licenseId);
                editor.commit();

                finish();
            }
        });
    }
}