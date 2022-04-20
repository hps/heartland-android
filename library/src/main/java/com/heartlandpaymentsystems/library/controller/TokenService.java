package com.heartlandpaymentsystems.library.controller;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.heartlandpaymentsystems.library.entities.Card;
import com.heartlandpaymentsystems.library.entities.Token;
import com.heartlandpaymentsystems.library.utilities.TLSSocketFactory;
import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class TokenService {

    public static final String TAG = "HPSTokenService";
    private String mPublicKey;
    private String mApiUrl;
    public TokenService(String publicKey) {
        this.mPublicKey = publicKey;
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey can not be null");
        }

        String[] components = mPublicKey.split("_");

        if (components.length < 3) {
            throw new IllegalArgumentException("PublicKey format invalid, please make sure you have set the public key to the constructor");
        }

        String env = components[1].toLowerCase();

        if (env.equals("prod")) {
            mApiUrl = "https://api2.heartlandportico.com/SecureSubmit.v1/api/token";
        } else {
            mApiUrl = "https://cert.api2.heartlandportico.com/Hps.Exchange.PosGateway.Hpf.v1/api/token";
        }

    }

    public void getToken(Card card, TokenCallback callback) {
        TokenAsyncTask asyncTask = new TokenAsyncTask();
        asyncTask.execute(new TokenTaskInput(card, callback));
    }

    public interface TokenCallback {
        Token onComplete(Token response);
    }

    private class TokenAsyncTask extends AsyncTask<TokenTaskInput, Void, Token> {
        private TokenTaskInput taskInput;

        @Override
        protected Token doInBackground(TokenTaskInput... inputParams) {
            this.taskInput = inputParams[0];
            Token tokenObject = null;
            try {
                TLSSocketFactory sf = new TLSSocketFactory();
                HttpsURLConnection conn = (HttpsURLConnection) new URL(mApiUrl).openConnection();
                conn.setSSLSocketFactory(sf);

                //converting the publickey to base64 format and adding as Basic Authorization.
                byte[] creds = String.format("%s:", mPublicKey).getBytes();
                String auth = String.format("Basic %s", Base64.encodeToString(creds, Base64.URL_SAFE));

                Gson gson = new Gson();
                String payload = gson.toJson(new Token(taskInput.getCard()));
                byte[] bytes = payload.getBytes();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.addRequestProperty("Authorization", auth.trim());
                conn.addRequestProperty("Content-Type", "application/json");
                conn.addRequestProperty("Content-Length", String.format("%s", bytes.length));

                DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());
                requestStream.write(bytes);
                requestStream.flush();
                requestStream.close();

                try {
                    InputStreamReader responseStream = new InputStreamReader(conn.getInputStream());
                    tokenObject = gson.fromJson(responseStream, Token.class);
                    if (taskInput.getCard() != null) {
                        tokenObject.getCard().setCardType(Card.parseCardType(taskInput.getCard().getNumber()));
                        tokenObject.getCard().setExpMonth(taskInput.getCard().getExpMonth());
                        tokenObject.getCard().setExpYear(taskInput.getCard().getExpYear());
                    }
                    responseStream.close();
                } catch (IOException e) {

                    if (conn.getResponseCode() == 400) {
                        InputStreamReader errorStream = new InputStreamReader(conn.getErrorStream());
                        tokenObject = gson.fromJson(errorStream, Token.class);
                        errorStream.close();
                    } else {
                        Log.d(TAG, "IOException occured " + e.toString());
                        throw new IOException(e);
                    }
                }

            } catch (Exception e) {
                Log.d(TAG, "Exception occured " + e.toString());
            }

            return tokenObject;
        }

        @Override
        protected void onPostExecute(Token tokenObject) {
            TokenCallback callback = taskInput.getCallback();
            callback.onComplete(tokenObject);
        }
    }

    public class TokenTaskInput {
        private Card card;
        private TokenCallback callback;

        public TokenTaskInput(Card card, TokenCallback callback) {
            this.card = card;
            this.callback = callback;
        }

        public Card getCard() {
            return card;
        }

        public TokenCallback getCallback() {
            return callback;
        }
    }
}
