package com.heartlandpaymentsystems.library.terminals.transactions;

import java.math.BigDecimal;
import java.util.HashMap;

import com.tsys.payments.library.domain.AutoSubstantiation;
import com.tsys.payments.library.domain.CardData;
import com.tsys.payments.library.domain.TransactionRequest;
import com.tsys.payments.library.enums.AvsType;
import com.tsys.payments.library.enums.CardDataSourceType;
import com.tsys.payments.library.enums.TransactionType;

import com.heartlandpaymentsystems.library.entities.Address;
import com.heartlandpaymentsystems.library.entities.TransactionDetails;
import com.heartlandpaymentsystems.library.entities.Card;
import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.tsys.payments.library.utils.LibraryConfigHelper;

public class CreditSaleBuilder extends BaseBuilder {
    private String referenceNumber;
    private BigDecimal amount;
    private Card creditCard;
    private Address address;
    private TransactionDetails details;
    private BigDecimal gratuity;
    private String cardHolderName;
    private boolean requestToken;
    private String token;
    private String cardBrandTxnId;
    private BigDecimal taxAmount;

   /* public CreditSaleBuilder(C2XDevice device) {
        super((IDevice) device);
    }*/

    /**
     * Build Sale Transaction
     * @param device
     */
    public CreditSaleBuilder(IDevice device){
        super(device);
    }

    @Override
    public void execute() throws Exception {
        //check if the healthcare total is larger than the transaction amount
        if (getTotalHealthcareAmount() != null) {
            BigDecimal healthcareAmount = getTotalHealthcareAmount().multiply(BigDecimal.valueOf(100));
            if (healthcareAmount.compareTo(amount) == 1) {
                //amount cannot be less than healthcare total
                throw new Exception("Amount cannot be less than healthcare total");
            }
        }
        super.execute();
    }

    @Override
    protected TransactionRequest buildRequest() {
        TransactionRequest request = super.buildRequest();

        request.setTransactionType(TransactionType.SALE);

        if (amount != null) {
            request.setTotal(amount.movePointRight(2).longValue());
        }

        if (gratuity != null) {
            request.setTip(gratuity.movePointRight(2).longValue());
        }

        request.setPosReferenceNumber(referenceNumber);

        if (details != null) {
            request.setInvoiceNumber(details.getInvoiceNumber());
        }

        request.setGenerateToken(requestToken);

        if (token != null && !token.isEmpty()) {
            request.setToken(token);
            request.setCardBrandTxnId(cardBrandTxnId);
        }

        if (taxAmount != null) {
            request.setTax(taxAmount.movePointRight(2).longValue());
        }

        if (creditCard != null) {
            CardData card = new CardData();
            card.setPan(creditCard.getNumber());
            String expMonth = String.format("%02d", creditCard.getExpMonth());
            card.setExpirationDate(expMonth + creditCard.getExpYear().toString());
            card.setCvv2(creditCard.getCvv());
            card.setCardDataSource(CardDataSourceType.KEYED);

            if (address != null) {
                AvsType avsType = AvsType.NONE;

                if (address.getPostalCode() != null && address.getStreetAddress() != null) {
                    avsType = AvsType.ZIP_ADDRESS;
                } else if (address.getPostalCode() != null) {
                    avsType = AvsType.ZIP;
                }

                com.tsys.payments.library.domain.Address add = new com.tsys.payments.library.domain.Address();
                add.setAddressLine1(address.getStreetAddress());
                add.setCity(address.getCity());
                add.setState(address.getState());
                add.setPostalCode(address.getPostalCode());
                card.setPostalCode(address.getPostalCode());

                card.setAvsType(avsType);
                card.setCardholderAddress(add);
                card.setCardholderName(cardHolderName);
            }

            request.setKeyedCardData(card);
        }

        return request;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Set the Amount for the transaction
     * @param amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Card getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(Card creditCard) {
        this.creditCard = creditCard;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public TransactionDetails getDetails() {
        return details;
    }

    public void setDetails(TransactionDetails details) {
        this.details = details;
    }

    public BigDecimal getGratuity() {
        return gratuity;
    }

    public void setGratuity(BigDecimal gratuity) {
        this.gratuity = gratuity;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public boolean isRequestToken() {
        return requestToken;
    }

    public void setRequestToken(boolean requestToken) {
        this.requestToken = requestToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCardBrandTxnId() {
        return cardBrandTxnId;
    }

    public void setCardBrandTxnId(String cardBrandTxnId) {
        this.cardBrandTxnId = cardBrandTxnId;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
}
