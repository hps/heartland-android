package com.heartlandpaymentsystems.library.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Card {
    private static final Pattern AmexRegex = Pattern.compile("^3[47][0-9]{13}$");
    private static final Pattern MasterCardRegex = Pattern.compile("^5[1-5][0-9]{14}$");
    private static final Pattern VisaRegex = Pattern.compile("^4[0-9]{12}(?:[0-9]{3})?$");
    private static final Pattern DinersClubRegex = Pattern.compile("^3(?:0[0-5]|[68][0-9])[0-9]{11}$");
    private static final Pattern RouteClubRegex = Pattern.compile("^(2014|2149)");
    private static final Pattern DiscoverRegex = Pattern.compile("^6(?:011|5[0-9]{2})[0-9]{12}$");
    private static final Pattern JcbRegex = Pattern.compile("^(?:2131|1800|35\\d{3})\\d{11}$");

    private Map<String, Pattern> regexMap;
    private String number;
    public String cvv;
    private Integer expMonth;
    private Integer expYear;
    private String cardType;

    public Card() {
        regexMap = getRegexMap();
    }

    public Card(String number, String cvv, Integer expMonth, Integer expYear) {
        this.number = number;
        this.cvv = cvv;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Integer getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(Integer expMonth) {
        this.expMonth = expMonth;
    }

    public Integer getExpYear() {
        return expYear;
    }

    public void setExpYear(Integer expYear) {
        this.expYear = expYear;
    }

    public String getCardType() {
        if (this.cardType != null) {
            return this.cardType;
        }

        this.cardType = parseCardType(this.number);

        return this.cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public static String parseCardType(String cardNumber) {
        String cardType = "Unknown";
        HashMap<String, Pattern> regexMap = getRegexMap();

        try {
            String cardNum = cardNumber.replace(" ", "").replace("-", "");
            for (Map.Entry<String, Pattern> kvp : regexMap.entrySet()) {
                if (kvp.getValue().matcher(cardNum).find()) {
                    cardType = kvp.getKey();
                    break;
                }
            }

        } catch (Exception e) {
            cardType = "Unknown";
        }

        return cardType;
    }

    public static HashMap<String, Pattern> getRegexMap() {
        HashMap<String, Pattern> regexMap = new HashMap<String, Pattern>();
        regexMap.put("Amex", AmexRegex);
        regexMap.put("MasterCard", MasterCardRegex);
        regexMap.put("Visa", VisaRegex);
        regexMap.put("DinersClub", DinersClubRegex);
        regexMap.put("EnRoute", RouteClubRegex);
        regexMap.put("Discover", DiscoverRegex);
        regexMap.put("Jcb", JcbRegex);
        return regexMap;
    }

}
