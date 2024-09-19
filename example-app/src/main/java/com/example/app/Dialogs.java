package com.example.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.tsys.payments.library.domain.TransactionResponse;
import java.util.List;

public class Dialogs {

    static AlertDialog pd;

    public static AlertDialog showListDialog(final String title, final Context context,
                                             final String[] list,
                                             final DialogInterface.OnClickListener onClickListener) {
        // LayoutInflater inflater = context.getLayoutInflater();
        // View dialog = inflater.inflate(R.layout.custom_dialog, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setItems(list, onClickListener);
        return builder.show();
    }

    public static AlertDialog showDialogOK(final Activity context, final String title, String message) {
        LayoutInflater inflater = context.getLayoutInflater();
        View dialog = inflater.inflate(R.layout.custom_dialog, null, false);
        TextView txtView = dialog.findViewById(R.id.dialog_txt);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        txtView.setText(message.replace("_", " "));
        builder.setView(dialog);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        return builder.show();
    }

    public static void showProgress(Activity context, String title, String message,
            View.OnClickListener dialogClickListener) {
        TextView pdtext;
        Button pdbutton;
        if (pd != null) {
            try {
                pd.dismiss();
            }catch(Throwable e) {
                e.printStackTrace();
            }
            pd = null;
        }

        LayoutInflater inflater = context.getLayoutInflater();
        View dialog = inflater.inflate(R.layout.progress_dialog, null, false);

        pdtext = dialog.findViewById(R.id.progress_Dialog_txt);
        pdtext.setText(message.replace("_", " "));

        pdbutton = dialog.findViewById(R.id.progress_button);
        if (dialogClickListener != null) {
            pdbutton.setOnClickListener(dialogClickListener);
        } else {
            pdbutton.setVisibility(View.INVISIBLE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialog);

        pd = builder.create();
        pd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        pd.show();
    }

    public static void showProgress(Context context, String title, String message, int percent) {
        TextView pdtext;
        ProgressBar progressBar;
        if (pd != null) {
            try {
                pd.dismiss();
            }catch(Throwable e) {
                e.printStackTrace();
            }
            pd = null;
        }

        pd = ProgressDialog.show(context, title, message.replace("_", " "));
        pd.setContentView(R.layout.progress_dialog_horizontal);
        pd.setCancelable(false);

        pdtext = pd.findViewById(R.id.progress_Dialog_txt);
        pdtext.setText(message.replace("_", " "));
        progressBar = pd.findViewById(R.id.progress_bar);
        progressBar.setProgress(percent);
        pd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public static void updateProgress(double percent) {
        if (pd == null) {
            return;
        }
        ProgressBar progressBar = pd.findViewById(R.id.progress_bar);
        progressBar.setProgress((int) percent);
    }

    public static void hideProgress(){
        if(pd != null){
            pd.dismiss();
            pd.hide();
            pd = null;
        }
    }

    public static String constructTransactionMessage(TerminalResponse terminalResponse) {
        String message = "";

        message += "Device Response Code: " + terminalResponse.getDeviceResponseCode() + "\n";
        message += "Response Text: " + terminalResponse.getResponseText() + "\n";
        message += "Authorization Response: " + terminalResponse.getAuthorizationResponse() +"\n";
        if(terminalResponse.getEntryMode() != null) {
            switch (terminalResponse.getEntryMode()) {
                case CHIP:
                case CONTACTLESS:{
                    message += "Issuer Authentication Data: " + terminalResponse.getIssuerAuthenticationData() +"\n";
                    break;
                }
                default:
                    break;
            }
        }

        message += "Transaction ID: " + terminalResponse.getTransactionId() + "\n";
        message += "Transaction Type: " + terminalResponse.getTransactionType() + "\n";
        message += "Entry Mode: " + terminalResponse.getEntryMode() + "\n";
        message += "Card Number: " + terminalResponse.getMaskedCardNumber() + "\n";
        message += "Cardholder Name: " + terminalResponse.getCardholderName();
        if (terminalResponse.getOrigTotal() != null && terminalResponse.getOrigTotal().floatValue() > 0) {
            message += "\nOrig Total: " + terminalResponse.getOrigTotal();
        }
        if (terminalResponse.getApprovedAmount() != null) {
            message += "\nAuth Amount: " + terminalResponse.getApprovedAmount();
        }
        if (terminalResponse.getSurchargeEligibility() != null) {
            message += "\nSurcharge Eligibility: " + terminalResponse.getSurchargeEligibility();
        }
        if (terminalResponse.getSurchargeAmount() != null && terminalResponse.getSurchargeAmount().floatValue() > 0) {
            message += "\nSurcharge Amount: " + terminalResponse.getSurchargeAmount();
        }
        if(terminalResponse.getTransactionType() == "SVA"){
            message += "\nSVA PAN: " + terminalResponse.getSvaPan();
            message += "\nExpiration: " + terminalResponse.getExpirationDate();
        }
        if (terminalResponse.getToken() != null) {
            message += "\nToken: " + terminalResponse.getToken();
            message += "\nCardBrandTxnId: " + terminalResponse.getCardBrandTxnId();
        }
        if (terminalResponse.getTerminalStatusIndicator() != null) {
            message += "\nTerminal Status Indicator: " + terminalResponse.getTerminalStatusIndicator();
        }

        return message.replace("_", " ");
    }

    public static String constructSAFTransactionMessage(List<TransactionResponse> responses) {
        String message = "";

        for (int i = 0; i < responses.size(); i++) {
            message += "SAF Item " + (i + 1) + "\n";
            message += "Device Response Code: " + responses.get(i).getTransactionResult() + "\n";
            message += "Transaction ID: " + responses.get(i).getGatewayTransactionId() + "\n";
            message += "Transaction Type: " + responses.get(i).getTransactionType() + "\n";
            message += "Amount: " + (responses.get(i).getApprovedAmount() / 100f) + "\n\n";
        }

        return message.replace("_", " ");
    }
}
