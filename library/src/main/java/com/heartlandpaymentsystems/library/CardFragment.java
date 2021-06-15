package com.heartlandpaymentsystems.library;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.heartlandpaymentsystems.library.entities.Card;
import com.heartlandpaymentsystems.library.entities.Token;
import com.heartlandpaymentsystems.library.controller.TokenService;

public class CardFragment extends Fragment {

    public static final String TAG = "HPSCardFragment";
    String publicKey;
    private ProgressDialog mProgressDialog;
    private CardFragmentInteractionListener mListener;

    public CardFragment() {
        // Required empty public constructor
    }

    public static CardFragment newInstance(String publicKey) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putString("public_key", publicKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicKey = getArguments().getString("public_key");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card, container, false);

        final EditText cardNo = view.findViewById(R.id.card_no_edt);
        final EditText cardExpDate = view.findViewById(R.id.card_exp_date_edt);
        final EditText cardExpYear = view.findViewById(R.id.card_exp_yr_edt);
        final EditText cardCvv = view.findViewById(R.id.card_cvv_edt);
        final Button submitBtn = view.findViewById(R.id.submit);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == submitBtn) {

                    hideKeyboard(getContext(), submitBtn);
                    showProgressDialog();
                    TokenService tokenService = new TokenService(publicKey);
                    final Card card = new Card();
                    card.setNumber(cardNo.getText().toString());
                    card.setExpMonth(Integer.valueOf(cardExpDate.getText().toString()));
                    card.setExpYear(Integer.valueOf(cardExpYear.getText().toString()));
                    card.setCvv(cardCvv.getText().toString());

                    tokenService.getToken(card, new TokenService.TokenCallback() {
                        @Override
                        public Token onComplete(Token response) {
                            hideProgressDialog();
                            Log.e(TAG, response.toString());
                            if (response == null || response.getError() != null) {
                                mListener.onTokenFailure(response.getError().getMessage());
                            } else {
                                mListener.onTokenSuccess(response);
                            }
                            return response;
                        }
                    });
                }
            }

        });

        return view;
    }

    public void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getContext(), getString(R.string.loading_msg), getString(R.string.tokenizing));
            mProgressDialog.setCancelable(false);
        } else {
            mProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
            mProgressDialog = null;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CardFragmentInteractionListener) {
            mListener = (CardFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
