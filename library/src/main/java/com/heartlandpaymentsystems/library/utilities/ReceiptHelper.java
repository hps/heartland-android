package com.heartlandpaymentsystems.library.utilities;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.enums.EntryMode;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiptHelper {

    private static float sFontSize = 20f;
    private static float sFontSizeLarge = 26f;
    private static float sMargin = 30f;

    public static Bitmap createReceiptImage(TerminalResponse transaction) {
        int width = 550;
        int height = 750;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(sFontSize);
        paint.setTextAlign(Paint.Align.CENTER);

        //draw the header
        paint.setTextSize(sFontSizeLarge);
        canvas.drawText("HPS Test", (width / 2.f) , sMargin, paint);
        paint.setTextSize(sFontSize);
        canvas.drawText("1 Heartland Way", (width / 2.f) , sMargin + sFontSize, paint);
        canvas.drawText("Jeffersonville, IN 47136", (width / 2.f) , sMargin + sFontSize*2, paint);
        canvas.drawText("888-798-3133", (width / 2.f) , sMargin + sFontSize*3, paint);

        //draw date and time
        paint.setTextAlign(Align.LEFT);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
        String date;
        if (transaction.getRspDT() != null) {
            date = format.format(transaction.getRspDT());
        } else {
            date = format.format(new Date());
        }
        canvas.drawText(date, sMargin, sMargin + sFontSize*5, paint);
        paint.setTextAlign(Align.RIGHT);
        format = new SimpleDateFormat("hh:mm aa");
        String time = format.format(new Date());
        canvas.drawText(time, width - sMargin, sMargin + sFontSize*5, paint);

        //draw transaction type
        paint.setTextSize(sFontSizeLarge);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("CREDIT CARD", (width / 2.f) , sMargin + sFontSize*6, paint);
        canvas.drawText(transaction.getTransactionType(), (width / 2.f) , sMargin + sFontSize*7, paint);

        //draw detail list
        paint.setTextSize(sFontSize);
        paint.setTextAlign(Paint.Align.LEFT);

        if (transaction.getEntryMode() != EntryMode.SWIPE && transaction.getEntryMode() != EntryMode.CHIP_FALLBACK_SWIPE
                && transaction.getEntryMode() != EntryMode.NONE) {
            canvas.drawText(transaction.getCardType(), sMargin, sMargin + sFontSize * 9, paint);
            canvas.drawText("ACCT:", sMargin, sMargin + sFontSize * 10, paint);
            canvas.drawText("APP NAME:", sMargin, sMargin + sFontSize * 11, paint);
            canvas.drawText("AID:", sMargin, sMargin + sFontSize * 12, paint);
            canvas.drawText("ARQC:", sMargin, sMargin + sFontSize * 13, paint);
            canvas.drawText("ENTRY:", sMargin, sMargin + sFontSize * 14, paint);
            canvas.drawText("APPROVAL:", sMargin, sMargin + sFontSize * 15, paint);
            canvas.drawText("TXN ID:", sMargin, sMargin + sFontSize * 16, paint);

            String maskedCardNumber = transaction.getMaskedCardNumber();
            maskedCardNumber = "xxxx" + maskedCardNumber.substring(4);
            canvas.drawText(maskedCardNumber, 200, sMargin + sFontSize * 10, paint);
            canvas.drawText(transaction.getApplicationName(), 200, sMargin + sFontSize * 11, paint);
            canvas.drawText(transaction.getApplicationId(), 200, sMargin + sFontSize * 12, paint);
            canvas.drawText(transaction.getApplicationCryptogram(), 200, sMargin + sFontSize * 13, paint);
            canvas.drawText(transaction.getEntryMode().toString(), 200, sMargin + sFontSize * 14, paint);
            canvas.drawText(transaction.getApprovalCode(), 200, sMargin + sFontSize * 15, paint);
            canvas.drawText(transaction.getTransactionId(), 200, sMargin + sFontSize * 16, paint);
        } else if (transaction.getEntryMode() == EntryMode.SWIPE || transaction.getEntryMode() == EntryMode.CHIP_FALLBACK_SWIPE) {
            canvas.drawText(transaction.getCardType(), sMargin, sMargin + sFontSize * 9, paint);
            canvas.drawText("ACCT:", sMargin, sMargin + sFontSize * 10, paint);
            canvas.drawText("APP NAME:", sMargin, sMargin + sFontSize * 11, paint);
            canvas.drawText("ENTRY:", sMargin, sMargin + sFontSize * 12, paint);
            canvas.drawText("APPROVAL:", sMargin, sMargin + sFontSize * 13, paint);
            canvas.drawText("TXN ID:", sMargin, sMargin + sFontSize * 14, paint);

            if (transaction.getMaskedCardNumber() != null) {
                canvas.drawText(transaction.getMaskedCardNumber(), 200, sMargin + sFontSize * 10, paint);
            }
            canvas.drawText("US CREDIT", 200, sMargin + sFontSize * 11, paint);
            canvas.drawText(transaction.getEntryMode().toString(), 200, sMargin + sFontSize * 12, paint);
            canvas.drawText(transaction.getApprovalCode(), 200, sMargin + sFontSize * 13, paint);
            canvas.drawText(transaction.getTransactionId(), 200, sMargin + sFontSize * 14, paint);
        } else {
            canvas.drawText("APPROVAL:", sMargin, sMargin + sFontSize * 9, paint);

            canvas.drawText(transaction.getApprovalCode(), 200, sMargin + sFontSize * 9, paint);
        }

        //draw description
        canvas.drawText("DESCRIPTION: Merchandise", sMargin, sMargin + sFontSize * 18, paint);

        //draw total
        paint.setTextSize(sFontSizeLarge);
        canvas.drawText("TOTAL", sMargin, sMargin + sFontSize*20, paint);
        paint.setTextAlign(Align.RIGHT);
        canvas.drawText("USD $ " + transaction.getApprovedAmount(), width - sMargin, sMargin + sFontSize*20, paint);

        if (transaction.getEntryMode() != EntryMode.NONE) {
            if ((transaction.getEntryMode() == EntryMode.SWIPE || transaction.getEntryMode() == EntryMode.CHIP_FALLBACK_SWIPE)
                || transaction.getCardType().equalsIgnoreCase("AMERICAN_EXPRESS") || transaction.getCardType().equalsIgnoreCase("DISCOVER")) {
                //draw agreement
                paint.setTextSize(sFontSize);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText("I agree to pay above total amount according to card", sMargin,
                        sMargin + sFontSize * 23, paint);
                canvas.drawText("issuer agreement.", sMargin, sMargin + sFontSize * 24, paint);

                //draw signature area

                paint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
                canvas.drawText(
                        "X                                                                                              ",
                        sMargin, sMargin + sFontSize * 27, paint);
                paint.setFlags(paint.getFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("SIGNATURE", width / 2,
                        sMargin + sFontSize * 28, paint);
            }

            paint.setTextSize(sFontSize);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No Refunds", width / 2,
                    sMargin + sFontSize * 30, paint);
            canvas.drawText("Store Credit Only", width / 2,
                    sMargin + sFontSize * 31, paint);
            canvas.drawText("Merchant Copy", width / 2,
                    sMargin + sFontSize * 33, paint);
        }

        //draw status
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(sFontSizeLarge);
        canvas.drawText(transaction.getDeviceResponseCode(), width / 2, sMargin + sFontSize*35, paint);

        return bitmap;
    }
}
