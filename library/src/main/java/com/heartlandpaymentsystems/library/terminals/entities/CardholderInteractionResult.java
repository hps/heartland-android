package com.heartlandpaymentsystems.library.terminals.entities;

import com.tsys.payments.library.enums.CardholderInteractionType;

public class CardholderInteractionResult extends com.tsys.payments.library.domain.CardholderInteractionResult {
    public CardholderInteractionResult(CardholderInteractionType cardholderInteractionType) {
        super(cardholderInteractionType);
    }
}
