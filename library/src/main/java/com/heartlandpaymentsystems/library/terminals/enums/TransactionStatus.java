package com.heartlandpaymentsystems.library.terminals.enums;

public enum TransactionStatus {
    NONE,
    WAITING_FOR_CARD,
    BAD_READ,
    ICC_SWIPED,
    FALLBACK_INITIATED,
    MULTIPLE_CARDS_DETECTED,
    CARD_READ,
    TECHNICAL_FALLBACK_INITIATED,
    CARD_READ_ERROR,
    CARD_REMOVED_AFTER_TRANSACTION_COMPLETE,
    CONTACTLESS_CARD_STILL_IN_FIELD,
    CONTACTLESS_INTERFACE_FAILED_TRY_CONTACT,
    DO_NOT_REMOVE_CARD,
    DEVICE_BUSY,
    ENTER_PIN,
    LAST_PIN_ATTEMPT,
    PIN_ACCEPTED,
    RETRY_PIN,
    REMOVE_CARD,
    CONFIGURING,
    SEE_PHONE;

    public static TransactionStatus fromVitalSdk(com.tsys.payments.library.enums.TransactionStatus status) {
        switch (status) {
            case WAITING_FOR_CARD: return WAITING_FOR_CARD;
            case BAD_READ: return BAD_READ;
            case ICC_SWIPED: return ICC_SWIPED;
            case FALLBACK_INITIATED: return FALLBACK_INITIATED;
            case MULTIPLE_CARDS_DETECTED: return MULTIPLE_CARDS_DETECTED;
            case CARD_READ: return CARD_READ;
            case TECHNICAL_FALLBACK_INITIATED: return TECHNICAL_FALLBACK_INITIATED;
            case CARD_READ_ERROR: return CARD_READ_ERROR;
            case CARD_REMOVED_AFTER_TRANSACTION_COMPLETE: return CARD_REMOVED_AFTER_TRANSACTION_COMPLETE;
            case CONTACTLESS_CARD_STILL_IN_FIELD: return CONTACTLESS_CARD_STILL_IN_FIELD;
            case CONTACTLESS_INTERFACE_FAILED_TRY_CONTACT: return CONTACTLESS_INTERFACE_FAILED_TRY_CONTACT;
            case DO_NOT_REMOVE_CARD: return DO_NOT_REMOVE_CARD;
            case DEVICE_BUSY: return DEVICE_BUSY;
            case ENTER_PIN: return ENTER_PIN;
            case LAST_PIN_ATTEMPT: return LAST_PIN_ATTEMPT;
            case PIN_ACCEPTED: return PIN_ACCEPTED;
            case RETRY_PIN: return RETRY_PIN;
            case REMOVE_CARD: return REMOVE_CARD;
            case CONFIGURING: return CONFIGURING;
            case SEE_PHONE: return SEE_PHONE;
        }

        return NONE;
    }
}
