package com.aeologic.adhoc.flutter_peachpay_plugin.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aeologic.adhoc.flutter_peachpay_plugin.R;
import com.aeologic.adhoc.flutter_peachpay_plugin.common.Constants;
import com.aeologic.adhoc.flutter_peachpay_plugin.task.CheckoutIdRequestAsyncTask;
import com.aeologic.adhoc.flutter_peachpay_plugin.task.CheckoutIdRequestListener;
import com.aeologic.adhoc.flutter_peachpay_plugin.task.PaymentStatusRequestAsyncTask;
import com.aeologic.adhoc.flutter_peachpay_plugin.task.PaymentStatusRequestListener;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.WalletConstants;
import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity;
import com.oppwa.mobile.connect.checkout.dialog.GooglePayHelper;
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings;
import com.oppwa.mobile.connect.checkout.meta.CheckoutSkipCVVMode;

import com.oppwa.mobile.connect.exception.PaymentError;
import com.oppwa.mobile.connect.provider.Connect;
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.provider.TransactionType;


/**
 * Represents a base activity for making the payments with mobile sdk.
 * This activity handles payment callbacks.
 */
@SuppressLint("Registered")
public class BasePaymentActivity extends BaseActivity
        implements CheckoutIdRequestListener, PaymentStatusRequestListener {

    private static final String STATE_RESOURCE_PATH = "STATE_RESOURCE_PATH";

    protected String resourcePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            resourcePath = savedInstanceState.getString(STATE_RESOURCE_PATH);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        /* Check if the intent contains the callback scheme. */
        if (resourcePath != null && hasCallbackScheme(intent)) {
            requestPaymentStatus(resourcePath);
        }
    }

    /**
     * Returns <code>true</code> if the Intent contains one of the predefined schemes for the app.
     *
     * @param intent the incoming intent
     * @return <code>true</code> if the Intent contains one of the predefined schemes for the app;
     *         <code>false</code> otherwise
     */
    protected boolean hasCallbackScheme(Intent intent) {
        String scheme = intent.getScheme();

        return getString(R.string.checkout_ui_callback_scheme).equals(scheme) ||
                getString(R.string.payment_button_callback_scheme).equals(scheme) ||
                getString(R.string.custom_ui_callback_scheme).equals(scheme);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_RESOURCE_PATH, resourcePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* Override onActivityResult to get notified when the checkout process is done. */
        if (requestCode == CheckoutActivity.REQUEST_CODE_CHECKOUT) {
            switch (resultCode) {
                case CheckoutActivity.RESULT_OK:
                    /* Transaction completed. */
                    Transaction transaction = data.getParcelableExtra(
                            CheckoutActivity.CHECKOUT_RESULT_TRANSACTION);

                    resourcePath = data.getStringExtra(
                            CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH);

                    /* Check the transaction type. */
                    if (transaction.getTransactionType() == TransactionType.SYNC) {
                        /* Check the status of synchronous transaction. */
                        requestPaymentStatus(resourcePath);
                    } else {
                        /* Asynchronous transaction is processed in the onNewIntent(). */
                        hideProgressDialog();
                    }

                    break;
                case CheckoutActivity.RESULT_CANCELED:
                    hideProgressDialog();
                    
                    break;
                case CheckoutActivity.RESULT_ERROR:
                    hideProgressDialog();

                    PaymentError error = data.getParcelableExtra(
                            CheckoutActivity.CHECKOUT_RESULT_ERROR);

                    showAlertDialog(error.getErrorMessage());
            }
        }
    }

    protected void requestCheckoutId(String callbackScheme) {
        showProgressDialog(R.string.progress_message_checkout_id);

        new CheckoutIdRequestAsyncTask(this)
                .execute(Constants.Config.AMOUNT, Constants.Config.CURRENCY);
    }

    @Override
    public void onCheckoutIdReceived(String checkoutId) {
        hideProgressDialog();

        if (checkoutId == null) {
            showAlertDialog(R.string.error_message);
        }
    }

    @Override
    public void onErrorOccurred() {
        hideProgressDialog();
        showAlertDialog(R.string.error_message);
    }

    @Override
    public void onPaymentStatusReceived(String paymentStatus) {
        hideProgressDialog();

        if ("OK".equals(paymentStatus)) {
            showAlertDialog(R.string.message_successful_payment);

            return;
        }

        showAlertDialog(R.string.message_unsuccessful_payment);
    }

    protected void requestPaymentStatus(String resourcePath) {
        showProgressDialog(R.string.progress_message_payment_status);
        new PaymentStatusRequestAsyncTask(this).execute(resourcePath);
    }

    /**
     * Creates the new instance of {@link CheckoutSettings}
     * to instantiate the {@link CheckoutActivity}.
     *
     * @param checkoutId the received checkout id
     * @return the new instance of {@link CheckoutSettings}
     */
    protected CheckoutSettings createCheckoutSettings(String checkoutId, String callbackScheme) {
        return new CheckoutSettings(checkoutId, Constants.Config.PAYMENT_BRANDS,
                Connect.ProviderMode.TEST)
                .setSkipCVVMode(CheckoutSkipCVVMode.FOR_STORED_CARDS)
                .setWindowSecurityEnabled(false)
                .setShopperResultUrl(callbackScheme + "://callback")
                .setGooglePayPaymentDataRequest(getGooglePayRequest());
    }

    private PaymentDataRequest getGooglePayRequest() {
        return GooglePayHelper.preparePaymentDataRequestBuilder(
                Constants.Config.AMOUNT,
                Constants.Config.CURRENCY,
                Constants.MERCHANT_ID,
                getPaymentMethodsForGooglePay(),
                getDefaultCardNetworksForGooglePay()
        ).build();
    }

    private Integer[] getPaymentMethodsForGooglePay() {
        return new Integer[] {
                WalletConstants.PAYMENT_METHOD_CARD,
                WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD
        };
    }

    private Integer[] getDefaultCardNetworksForGooglePay() {
        return new Integer[] {
                WalletConstants.CARD_NETWORK_VISA,
                WalletConstants.CARD_NETWORK_MASTERCARD,
                WalletConstants.CARD_NETWORK_AMEX
        };
    }
}
