package com.aeologic.adhoc.flutter_peachpay_plugin.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.aeologic.adhoc.flutter_peachpay_plugin.R;
import com.aeologic.adhoc.flutter_peachpay_plugin.common.Constants;
import com.oppwa.mobile.connect.exception.PaymentError;
import com.oppwa.mobile.connect.exception.PaymentException;
import com.oppwa.mobile.connect.payment.BrandsValidation;
import com.oppwa.mobile.connect.payment.CheckoutInfo;
import com.oppwa.mobile.connect.payment.ImagesRequest;
import com.oppwa.mobile.connect.payment.PaymentParams;
import com.oppwa.mobile.connect.payment.card.CardPaymentParams;
import com.oppwa.mobile.connect.provider.Connect;
import com.oppwa.mobile.connect.provider.ITransactionListener;
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.provider.TransactionType;
import com.oppwa.mobile.connect.service.ConnectService;
import com.oppwa.mobile.connect.service.IProviderBinder;


/**
 * Represents an activity for show the integration of mobile sdk and custom UI.
 */
public class CustomUIActivity extends BasePaymentActivity implements ITransactionListener {

    private EditText cardHolderEditText;
    private EditText cardNumberEditText;
    private EditText cardExpiryMonthEditText;
    private EditText cardExpiryYearEditText;
    private EditText cardCvvEditText;

    private String checkoutId;

    private IProviderBinder providerBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /* we have a connection to the service */
            providerBinder = (IProviderBinder) service;
            providerBinder.addTransactionListener(CustomUIActivity.this);

            try {
                providerBinder.initializeProvider(Connect.ProviderMode.TEST);
            } catch (PaymentException ee) {
	            showErrorDialog(ee.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            providerBinder = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_ui);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, ConnectService.class);

        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(serviceConnection);
        stopService(new Intent(this, ConnectService.class));
    }

    private void initViews() {
        cardHolderEditText = findViewById(R.id.holder_edit_text);
        cardHolderEditText.setText(Constants.Config.CARD_HOLDER_NAME);

        cardNumberEditText = findViewById(R.id.number_edit_text);
        cardNumberEditText.setText(Constants.Config.CARD_NUMBER);

        cardExpiryMonthEditText = findViewById(R.id.expiry_month_edit_text);
        cardExpiryMonthEditText.setText(Constants.Config.CARD_EXPIRY_MONTH);

        cardExpiryYearEditText = findViewById(R.id.expiry_year_edit_text);
        cardExpiryYearEditText.setText(Constants.Config.CARD_EXPIRY_YEAR);

        cardCvvEditText = findViewById(R.id.cvv_edit_text);
        cardCvvEditText.setText(Constants.Config.CARD_CVV);

        findViewById(R.id.button_pay_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (providerBinder != null && checkFields()) {
                    requestCheckoutId(getString(R.string.custom_ui_callback_scheme));
                }
            }
        });
    }

    private boolean checkFields() {
        if (cardHolderEditText.getText().length() == 0 ||
                cardNumberEditText.getText().length() == 0 ||
                cardExpiryMonthEditText.getText().length() == 0 ||
                cardExpiryYearEditText.getText().length() == 0 ||
                cardCvvEditText.getText().length() == 0) {
            showAlertDialog(R.string.error_empty_fields);

            return false;
        }

        return true;
    }

    @Override
    public void onCheckoutIdReceived(String checkoutId) {
        super.onCheckoutIdReceived(checkoutId);

        if (checkoutId != null) {
            this.checkoutId = checkoutId;

            requestCheckoutInfo(checkoutId);
        }
    }

    private void requestCheckoutInfo(String checkoutId) {
        if (providerBinder != null) {
            try {
                providerBinder.requestCheckoutInfo(checkoutId);
                showProgressDialog(R.string.progress_message_checkout_info);
            } catch (PaymentException e) {
                showAlertDialog(e.getMessage());
            }
        }
    }

    private void pay(String checkoutId) {
        try {
            PaymentParams paymentParams = createPaymentParams(checkoutId);
            paymentParams.setShopperResultUrl(getString(R.string.custom_ui_callback_scheme) + "://callback");
            Transaction transaction = new Transaction(paymentParams);

            providerBinder.submitTransaction(transaction);
            showProgressDialog(R.string.progress_message_processing_payment);
        } catch (PaymentException e) {
            showErrorDialog(e.getError());
        }
    }

    private PaymentParams createPaymentParams(String checkoutId) throws PaymentException {
        String cardHolder = cardHolderEditText.getText().toString();
        String cardNumber = cardNumberEditText.getText().toString();
        String cardExpiryMonth = cardExpiryMonthEditText.getText().toString();
        String cardExpiryYear = cardExpiryYearEditText.getText().toString();
        String cardCVV = cardCvvEditText.getText().toString();

        return new CardPaymentParams(
                checkoutId,
                Constants.Config.CARD_BRAND,
                cardNumber,
                cardHolder,
                cardExpiryMonth,
                "20" + cardExpiryYear,
                cardCVV
        );
    }

    @Override
    public void brandsValidationRequestSucceeded(BrandsValidation brandsValidation) {
        
    }

    @Override
    public void brandsValidationRequestFailed(PaymentError paymentError) {

    }

    @Override
    public void imagesRequestSucceeded(ImagesRequest imagesRequest) {

    }

    @Override
    public void imagesRequestFailed() {

    }

    @Override
    public void paymentConfigRequestSucceeded(final CheckoutInfo checkoutInfo) {
        hideProgressDialog();

        if (checkoutInfo == null) {
            showErrorDialog(getString(R.string.error_message));

            return;
        }

        /* Get the resource path from checkout info to request the payment status later. */
        resourcePath = checkoutInfo.getResourcePath();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showConfirmationDialog(
                        String.valueOf(checkoutInfo.getAmount()),
                        checkoutInfo.getCurrencyCode()
                );
            }
        });
    }

    private void showConfirmationDialog(String amount, String currency) {
        new AlertDialog.Builder(this)
                .setMessage(String.format(getString(R.string.message_payment_confirmation), amount, currency))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pay(checkoutId);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .setCancelable(false)
                .show();
    }

    @Override
    public void paymentConfigRequestFailed(PaymentError paymentError) {
        hideProgressDialog();
        showErrorDialog(paymentError);
    }

    @Override
    public void transactionCompleted(Transaction transaction) {
        hideProgressDialog();

        if (transaction == null) {
            showErrorDialog(getString(R.string.error_message));

            return;
        }

        if (transaction.getTransactionType() == TransactionType.SYNC) {
            /* check the status of synchronous transaction */
            requestPaymentStatus(resourcePath);
        } else {
            /* wait for the callback in the onNewIntent() */
            showProgressDialog(R.string.progress_message_please_wait);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(transaction.getRedirectUrl())));
        }
    }

    @Override
    public void transactionFailed(Transaction transaction, PaymentError paymentError) {
        hideProgressDialog();
        showErrorDialog(paymentError);
    }

    private void showErrorDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAlertDialog(message);
            }
        });
    }

    private void showErrorDialog(PaymentError paymentError) {
        showErrorDialog(paymentError.getErrorMessage());
    }
}
