package com.heartlandpaymentsystems.library;

import com.heartlandpaymentsystems.library.entities.Token;

public interface CardFragmentInteractionListener {
    void onTokenSuccess(Token response);
    void onTokenFailure(String response);
}
