package com.aeologic.adhoc.flutter_peachpay_plugin.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.aeologic.adhoc.flutter_peachpay_plugin.R;
import com.aeologic.adhoc.flutter_peachpay_plugin.common.Constants;
import com.aeologic.adhoc.flutter_peachpay_plugin.receiver.CheckoutBroadcastReceiver;
import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity;
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings;


/**
 * Represents an activity for making payments via {@link CheckoutActivity}.
 */
public class CheckoutUIActivity extends BasePaymentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.activity_checkout_ui);

        requestCheckoutId(getString(R.string.checkout_ui_callback_scheme));

      /*  String amount = Constants.Config.AMOUNT + " " + Constants.Config.CURRENCY;

        ((TextView) findViewById(R.id.amount_text_view)).setText(amount);

        findViewById(R.id.button_proceed_to_checkout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
    }

    @Override
    public void onCheckoutIdReceived(String checkoutId) {
        super.onCheckoutIdReceived(checkoutId);

            if (checkoutId != null) {
                openCheckoutUI(checkoutId);
            }
    }

    private void openCheckoutUI(String checkoutId) {
        CheckoutSettings checkoutSettings = createCheckoutSettings(checkoutId, getString(R.string.checkout_ui_callback_scheme));

        /* Set componentName if you want to receive callbacks from the checkout */
        ComponentName componentName = new ComponentName(
                getPackageName(), CheckoutBroadcastReceiver.class.getName());

        /* Set up the Intent and start the checkout activity. */
        Intent intent = checkoutSettings.createCheckoutActivityIntent(this, componentName);

        startActivityForResult(intent, CheckoutActivity.REQUEST_CODE_CHECKOUT);
    }
}
