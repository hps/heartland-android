package com.heartlandpaymentsystems.library.entities;

public class Token {
    private String object;
    private String token_type;
    private String token_value;
    private String token_expire;
    private Card card;
    private Error error;

    public Token(Card card) {
        this.object = "token";
        this.token_type = "supt";
        this.card = new Card(card.getNumber(), card.getCvv(), card.getExpMonth(), card.getExpYear());
    }

    public Error getError() {
        return error;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String token_type) {
        this.token_type = token_type;
    }

    public String getTokenValue() {
        return token_value;
    }

    public void setTokenValue(String token_value) {
        this.token_value = token_value;
    }

    public String getTokenExpire() {
        return token_expire;
    }

    public void setTokenExpire(String token_expire) {
        this.token_expire = token_expire;
    }

}
