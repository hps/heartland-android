package com.heartlandpaymentsystems.library.terminals.enums;

import com.tsys.payments.library.enums.CardDataSourceType;

public enum EntryMode {
    NONE,
    MANUAL,
    SWIPE,
    CONTACTLESS,
    CHIP,
    CHIP_FALLBACK_SWIPE;

    public static EntryMode fromCardDataSourceType(CardDataSourceType cardDataSourceType) {
        if (cardDataSourceType == null) {
            return NONE;
        }

        switch (cardDataSourceType) {
            case CONTACTLESS_EMV:
            case CONTACTLESS_MSR:
                return CONTACTLESS;
            case MSR:
                return SWIPE;
            case SCR:
                return CHIP;
            case FALLBACK:
                return CHIP_FALLBACK_SWIPE;
            case INTERNET:
            case MAIL:
            case KEYED:
            case PHONE:
                return MANUAL;
        }

        return NONE;
    }
}
