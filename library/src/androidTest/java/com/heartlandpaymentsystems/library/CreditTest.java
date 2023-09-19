package com.heartlandpaymentsystems.library;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.heartlandpaymentsystems.library.entities.Card;
import com.heartlandpaymentsystems.library.terminals.ConnectionConfig;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionResult;
import com.heartlandpaymentsystems.library.terminals.entities.TerminalResponse;
import com.heartlandpaymentsystems.library.terminals.TransactionListener;
import com.heartlandpaymentsystems.library.terminals.c2x.C2XDevice;
import com.heartlandpaymentsystems.library.terminals.entities.CardholderInteractionRequest;
import com.heartlandpaymentsystems.library.terminals.enums.Environment;
import com.heartlandpaymentsystems.library.terminals.enums.ErrorType;
import com.heartlandpaymentsystems.library.terminals.enums.TransactionStatus;
import com.heartlandpaymentsystems.library.terminals.transactions.BaseBuilder;
import com.heartlandpaymentsystems.library.terminals.transactions.CreditAuthBuilder;
import com.heartlandpaymentsystems.library.terminals.transactions.CreditSaleBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import timber.log.Timber;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CreditTest {
    @Before
    public void setUp() {
        Timber.plant(new Timber.DebugTree());
    }
    @Test
    public void CreditAuth_WithManualEntry_Returns() throws Exception {
        ConnectionConfig connectionConfig = new ConnectionConfig();
//        connectionConfig.setSecretApiKey("skapi_cert_MW7lAQBmFl8AkVAvcx9QUJtP5YKtmHrOZVGN20PYQw");
        connectionConfig.setUsername("777700004597");
        connectionConfig.setPassword("$Test1234");
        connectionConfig.setSiteId("20904");
        connectionConfig.setDeviceId("1520053");
        connectionConfig.setLicenseId("20903");
        connectionConfig.setEnvironment(Environment.TEST);

        C2XDevice device = new C2XDevice(InstrumentationRegistry.getTargetContext(), connectionConfig);

        Card card = new Card();
        card.setNumber("4242424242424242");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvv("123");

        CreditAuthBuilder builder = new CreditAuthBuilder(device);
        builder.setAmount(new BigDecimal("10.00"));
        builder.setCreditCard(card);

        runTestAndAssert(device, builder, (TerminalResponse response) -> {
            assertEquals("APPROVED", response.getDeviceResponseCode());
        });
    }

    @Test
    public void CreditSale_WithManualEntry_Returns() throws Exception {
        ConnectionConfig connectionConfig = new ConnectionConfig();
//        connectionConfig.setSecretApiKey("skapi_cert_MW7lAQBmFl8AkVAvcx9QUJtP5YKtmHrOZVGN20PYQw");
        connectionConfig.setUsername("777700004597");
        connectionConfig.setPassword("$Test1234");
        connectionConfig.setSiteId("20904");
        connectionConfig.setDeviceId("1520053");
        connectionConfig.setLicenseId("20903");
        connectionConfig.setEnvironment(Environment.TEST);

        C2XDevice device = new C2XDevice(InstrumentationRegistry.getTargetContext(), connectionConfig);

        Card card = new Card();
        card.setNumber("4242424242424242");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvv("123");

        CreditSaleBuilder builder = new CreditSaleBuilder(device);
        builder.setAmount(new BigDecimal("10.00"));
        builder.setCreditCard(card);

        runTestAndAssert(device, builder, (TerminalResponse response) -> {
            assertEquals("APPROVED", response.getDeviceResponseCode());
        });
    }

    interface AssertionsFuncInterface {
        // An abstract function
        void run(TerminalResponse x);
    }

    private void runTestAndAssert(C2XDevice device, BaseBuilder builder, final AssertionsFuncInterface assertions) throws Exception {
        final Object syncObject = new Object();
        device.setTransactionListener(new TransactionListener() {
            @Override
            public void onStatusUpdate(TransactionStatus transactionStatus) {

            }

            @Override
            public void onCardholderInteractionRequested(CardholderInteractionRequest cardholderInteractionRequest) {
                CardholderInteractionResult result;
                switch (cardholderInteractionRequest.getCardholderInteractionType()) {
                    case EMV_APPLICATION_SELECTION:
                        String[] applications = cardholderInteractionRequest.getSupportedApplications();

                        // prompt user to select desired application

                        // send result
                        result = new CardholderInteractionResult(cardholderInteractionRequest.getCardholderInteractionType());
                        result.setSelectedAidIndex(0);
                        device.sendCardholderInteractionResult(result);
                        break;
                    case FINAL_AMOUNT_CONFIRMATION:
                        // prompt user to confirm final amount
                        result = new CardholderInteractionResult(cardholderInteractionRequest.getCardholderInteractionType());
                        result.setFinalAmountConfirmed(true);
                        device.sendCardholderInteractionResult(result);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTransactionComplete(TerminalResponse response) {
                assertions.run(response);
                synchronized (syncObject){
                    syncObject.notify();
                }
            }

            @Override
            public void onError(Error error, ErrorType errorType) {
                assertTrue(error.getMessage(), false);
                synchronized (syncObject){
                    syncObject.notify();
                }
            }
        });

        builder.execute();

        synchronized (syncObject){
            syncObject.wait();
        }
    }
}
