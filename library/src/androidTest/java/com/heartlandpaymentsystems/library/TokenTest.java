package com.heartlandpaymentsystems.library;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import com.heartlandpaymentsystems.library.entities.Card;
import com.heartlandpaymentsystems.library.entities.Token;
import com.heartlandpaymentsystems.library.controller.TokenService;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class TokenTest {
    String publicKey = "pkapi_cert_P6dRqs1LzfWJ6HgGVZ";

    @Rule
    public ActivityTestRule<CardActivity> hpsCardActivityRule = new ActivityTestRule<>(CardActivity.class);

    private CardActivity hpsCardActivity;
    Card card = new Card();

    @Test
    public void testToken() {
        TokenService tokenService = new TokenService(publicKey);

        String cardNo = "4242424242424242";
        String cardExpMonth = "12";
        String cardExpYear = "2015";
        String cardCVV = "123";

        Espresso.onView(withId(R.id.card_no_edt)).perform(typeText(cardNo));
        Espresso.onView(withId(R.id.card_exp_date_edt)).perform(typeText(cardExpMonth));
        Espresso.onView(withId(R.id.card_exp_yr_edt)).perform(typeText(cardExpYear));
        Espresso.onView(withId(R.id.card_cvv_edt)).perform(typeText(cardCVV));
        Espresso.closeSoftKeyboard();
        getbuttonView();

        card.setNumber(cardNo);
        card.setExpMonth(Integer.valueOf(cardExpMonth));
        card.setExpYear(Integer.valueOf(cardExpYear));
        card.setCvv(cardCVV);

        tokenService.getToken(card, new TokenService.TokenCallback() {
            @Override
            public Token onComplete(Token response) {
                return response;
            }
        });
    }

    @Test
    public void testInvalidNumber() {
        TokenService tokenService = new TokenService(publicKey);

        String cardNo = "12";
        String cardExpMonth = "12";
        String cardExpYear = "2015";
        String cardCVV = "123";

        Espresso.onView(withId(R.id.card_no_edt)).perform(typeText(cardNo));
        Espresso.onView(withId(R.id.card_exp_date_edt)).perform(typeText(cardExpMonth));
        Espresso.onView(withId(R.id.card_exp_yr_edt)).perform(typeText(cardExpYear));
        Espresso.onView(withId(R.id.card_cvv_edt)).perform(typeText(cardCVV));
        Espresso.closeSoftKeyboard();
        getbuttonView();

        card.setNumber(cardNo);
        card.setExpMonth(Integer.valueOf(cardExpMonth));
        card.setExpYear(Integer.valueOf(cardExpYear));
        card.setCvv(cardCVV);

        tokenService.getToken(card, new TokenService.TokenCallback() {
            @Override
            public Token onComplete(Token response) {
                return response;
            }
        });
    }

    @Test
    public void testNotNull() {
        TokenService tokenService = new TokenService(publicKey);

        String cardNo = "";
        String cardExpMonth = "12";
        String cardExpYear = "2015";
        String cardCVV = "123";

        Espresso.onView(withId(R.id.card_no_edt)).perform(typeText(cardNo));
        Espresso.onView(withId(R.id.card_exp_date_edt)).perform(typeText(cardExpMonth));
        Espresso.onView(withId(R.id.card_exp_yr_edt)).perform(typeText(cardExpYear));
        Espresso.onView(withId(R.id.card_cvv_edt)).perform(typeText(cardCVV));
        Espresso.closeSoftKeyboard();
        getbuttonView();

        card.setNumber(cardNo);
        card.setExpMonth(Integer.valueOf(cardExpMonth));
        card.setExpYear(Integer.valueOf(cardExpYear));
        card.setCvv(cardCVV);

        tokenService.getToken(card, new TokenService.TokenCallback() {
            @Override
            public Token onComplete(Token response) {
                return response;
            }
        });
    }

    @Test
    public void testLongCardNumber() {
        TokenService tokenService = new TokenService(publicKey);

        String cardNo = "123456789012345678990";
        String cardExpMonth = "12";
        String cardExpYear = "2015";
        String cardCVV = "123";

        Espresso.onView(withId(R.id.card_no_edt)).perform(typeText(cardNo));
        Espresso.onView(withId(R.id.card_exp_date_edt)).perform(typeText(cardExpMonth));
        Espresso.onView(withId(R.id.card_exp_yr_edt)).perform(typeText(cardExpYear));
        Espresso.onView(withId(R.id.card_cvv_edt)).perform(typeText(cardCVV));
        Espresso.closeSoftKeyboard();
        getbuttonView();

        card.setNumber(cardNo);
        card.setExpMonth(Integer.valueOf(cardExpMonth));
        card.setExpYear(Integer.valueOf(cardExpYear));
        card.setCvv(cardCVV);

        tokenService.getToken(card, new TokenService.TokenCallback() {
            @Override
            public Token onComplete(Token response) {
                return response;
            }
        });
    }

    @Test
    public void testExpMonthLow() {
        TokenService tokenService = new TokenService(publicKey);

        String cardNo = "4242424242424242";
        String cardExpMonth = "0";
        String cardExpYear = "2015";
        String cardCVV = "123";

        Espresso.onView(withId(R.id.card_no_edt)).perform(typeText(cardNo));
        Espresso.onView(withId(R.id.card_exp_date_edt)).perform(typeText(cardExpMonth));
        Espresso.onView(withId(R.id.card_exp_yr_edt)).perform(typeText(cardExpYear));
        Espresso.onView(withId(R.id.card_cvv_edt)).perform(typeText(cardCVV));
        Espresso.closeSoftKeyboard();
        getbuttonView();

        card.setNumber(cardNo);
        card.setExpMonth(Integer.valueOf(cardExpMonth));
        card.setExpYear(Integer.valueOf(cardExpYear));
        card.setCvv(cardCVV);

        tokenService.getToken(card, new TokenService.TokenCallback() {
            @Override
            public Token onComplete(Token response) {
                return response;
            }
        });
    }

    @Test
    public void testExpMonthHigh() {
        TokenService tokenService = new TokenService(publicKey);

        String cardNo = "4242424242424242";
        String cardExpMonth = "1235";
        String cardExpYear = "2015";
        String cardCVV = "123";

        Espresso.onView(withId(R.id.card_no_edt)).perform(typeText(cardNo));
        Espresso.onView(withId(R.id.card_exp_date_edt)).perform(typeText(cardExpMonth));
        Espresso.onView(withId(R.id.card_exp_yr_edt)).perform(typeText(cardExpYear));
        Espresso.onView(withId(R.id.card_cvv_edt)).perform(typeText(cardCVV));
        Espresso.closeSoftKeyboard();
        getbuttonView();

        card.setNumber(cardNo);
        card.setExpMonth(Integer.valueOf(cardExpMonth));
        card.setExpYear(Integer.valueOf(cardExpYear));
        card.setCvv(cardCVV);

        tokenService.getToken(card, new TokenService.TokenCallback() {
            @Override
            public Token onComplete(Token response) {
                return response;
            }
        });
    }

    @Test
    public void testInvalidExpYear() {
        TokenService tokenService = new TokenService(publicKey);

        String cardNo = "4242424242424242";
        String cardExpMonth = "1235";
        String cardExpYear = "0";
        String cardCVV = "123";

        Espresso.onView(withId(R.id.card_no_edt)).perform(typeText(cardNo));
        Espresso.onView(withId(R.id.card_exp_date_edt)).perform(typeText(cardExpMonth));
        Espresso.onView(withId(R.id.card_exp_yr_edt)).perform(typeText(cardExpYear));
        Espresso.onView(withId(R.id.card_cvv_edt)).perform(typeText(cardCVV));
        Espresso.closeSoftKeyboard();
        getbuttonView();

        card.setNumber(cardNo);
        card.setExpMonth(Integer.valueOf(cardExpMonth));
        card.setExpYear(Integer.valueOf(cardExpYear));
        card.setCvv(cardCVV);

        tokenService.getToken(card, new TokenService.TokenCallback() {
            @Override
            public Token onComplete(Token response) {
                return response;
            }
        });
    }

    private void getbuttonView() {
        Espresso.onView(withId(R.id.submit)).perform(click());
    }

    @Before
    public void setUp() throws Exception {
        hpsCardActivity = hpsCardActivityRule.getActivity();
    }

    @After
    public void tearDown() throws Exception {
        hpsCardActivity = null;
    }
}
