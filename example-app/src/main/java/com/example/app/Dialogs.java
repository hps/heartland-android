package com.example.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;

public class Dialogs {
    static ProgressDialog pd;
    public static AlertDialog showListDialog(final String title, final Context context,
                                             final String[] list,
                                             final DialogInterface.OnClickListener onClickListener) {
        // LayoutInflater inflater = context.getLayoutInflater();
        // View dialog = inflater.inflate(R.layout.custom_dialog, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(true);
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

    public static void showProgress(Context context, String title, String message) {
        TextView pdtext;
        if(pd == null) {
            pd = ProgressDialog.show(context, title, message.replace("_", " "));
            pd.setContentView(R.layout.progress_dialog);
        }
        pdtext = pd.findViewById(R.id.progress_Dialog_txt);
        pdtext.setText(message.replace("_", " "));
        pd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public static void showProgress(Context context, String title, String message, int percent) {
        TextView pdtext;
        ProgressBar progressBar;
        if(pd == null) {
            pd = ProgressDialog.show(context, title, message.replace("_", " "));
            pd.setContentView(R.layout.progress_dialog_horizontal);
            pd.setCancelable(false);
        }
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

        return message;
    }
}
