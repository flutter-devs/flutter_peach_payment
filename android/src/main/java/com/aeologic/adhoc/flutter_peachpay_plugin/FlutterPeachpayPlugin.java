package com.aeologic.adhoc.flutter_peachpay_plugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.aeologic.adhoc.flutter_peachpay_plugin.activity.BasePaymentActivity;
import com.aeologic.adhoc.flutter_peachpay_plugin.activity.CheckoutUIActivity;
import com.aeologic.adhoc.flutter_peachpay_plugin.common.Constants;
import com.aeologic.adhoc.flutter_peachpay_plugin.receiver.CheckoutBroadcastReceiver;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/** FlutterPeachpayPlugin */
public class FlutterPeachpayPlugin implements MethodCallHandler ,PluginRegistry.ActivityResultListener,
        CheckoutIdRequestListener, PaymentStatusRequestListener,PluginRegistry.NewIntentListener {
  private float amount;
  private final PluginRegistry.Registrar registrar;
  Result  result;
  /** Plugin registration. */
  private static final String STATE_RESOURCE_PATH = "STATE_RESOURCE_PATH";

  protected String resourcePath;

  private ProgressDialog progressDialog;

  private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;


  protected void showProgressDialog(int messageId) {
    if (progressDialog != null && progressDialog.isShowing()) {
      return;
    }

    if (progressDialog == null) {
      progressDialog = new ProgressDialog(registrar.activity());
      progressDialog.setCancelable(false);
    }

    progressDialog.setMessage(registrar.activity().getString(messageId));
    progressDialog.show();
  }

  protected void hideProgressDialog() {
    if (progressDialog == null) {
      return;
    }

    progressDialog.dismiss();
  }

  protected void showAlertDialog(final String message) {
    new AlertDialog.Builder(registrar.activity())
            .setMessage(message)
            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

                result.success(message);
              }
            })
            .setCancelable(false)
            .show();
  }

  protected void showAlertDialog(int messageId) {
    showAlertDialog(registrar.activity().getString(messageId));
  }
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_peachpay_plugin");
    FlutterPeachpayPlugin paymentGatewayPlugin = new FlutterPeachpayPlugin(registrar);
    registrar.addActivityResultListener(paymentGatewayPlugin);
     registrar.addNewIntentListener(paymentGatewayPlugin);
    channel.setMethodCallHandler(paymentGatewayPlugin);
  }


  public FlutterPeachpayPlugin(final  PluginRegistry.Registrar registrar ) {

    this.registrar=registrar;

    this.activityLifecycleCallbacks =
            new Application.ActivityLifecycleCallbacks() {
              @Override
              public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                if (savedInstanceState != null) {
                  resourcePath = savedInstanceState.getString(STATE_RESOURCE_PATH);
                }
              }

              @Override
              public void onActivityStarted(Activity activity) {}

              @Override
              public void onActivityResumed(Activity activity) {}

              @Override
              public void onActivityPaused(Activity activity) {}

              @Override
              public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                if (activity == registrar.activity()) {
                  outState.putString(STATE_RESOURCE_PATH, resourcePath);
                }
              }

              @Override
              public void onActivityDestroyed(Activity activity) {}

              @Override
              public void onActivityStopped(Activity activity) {}
            };

    if (this.registrar != null
            && this.registrar.activity() != null
            && this.registrar.activity().getApplication() != null) {
      this.registrar
              .activity()
              .getApplication()
              .registerActivityLifecycleCallbacks(this.activityLifecycleCallbacks);
    }



  }
  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("checkoutActivity")) {

      this.result=result;

      amount = Float.parseFloat(call.argument("amt").toString());
      Constants.Config.AMOUNT=amount+"";

      requestCheckoutId(registrar.activity().getString(R.string.checkout_ui_callback_scheme));
    } else {
      result.notImplemented();
    }
  }


  protected void requestCheckoutId(String callbackScheme) {
    showProgressDialog(R.string.progress_message_checkout_id);

    new CheckoutIdRequestAsyncTask(this)
            .execute(Constants.Config.AMOUNT, Constants.Config.CURRENCY);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

   /* if (resultCode == RESULT_CANCELED&&requestCode==2000) {

      Log.i("mobile.connect.checkout", "Transaction was attempted, but unsuccessful");

       String data=intent.getStringExtra("value");
      result.success(data);

    }
    else if (resultCode == RESULT_OK&&requestCode==2000) {
      String data=intent.getStringExtra("value");
      result.success(data);
    }

*/
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

    return false;
  }
  @Override
  public void onCheckoutIdReceived(String checkoutId) {
    hideProgressDialog();

    if (checkoutId == null) {
      showAlertDialog(R.string.error_message);
    }
    else  if (checkoutId != null) {
      openCheckoutUI(checkoutId);
    }
  }


  private void openCheckoutUI(String checkoutId) {
    CheckoutSettings checkoutSettings = createCheckoutSettings(checkoutId, registrar.activity().getString(R.string.checkout_ui_callback_scheme));

    /* Set componentName if you want to receive callbacks from the checkout */
    ComponentName componentName = new ComponentName(
            registrar.activity(). getPackageName(), CheckoutBroadcastReceiver.class.getName());

    /* Set up the Intent and start the checkout activity. */
    Intent intent = checkoutSettings.createCheckoutActivityIntent(registrar.activity(), componentName);

    registrar.activity().startActivityForResult(intent, CheckoutActivity.REQUEST_CODE_CHECKOUT);
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


  protected boolean hasCallbackScheme(Intent intent) {
    String scheme = intent.getScheme();

    return registrar.activity().getString(R.string.checkout_ui_callback_scheme).equals(scheme) ||
            registrar.activity(). getString(R.string.payment_button_callback_scheme).equals(scheme) ||
            registrar.activity(). getString(R.string.custom_ui_callback_scheme).equals(scheme);
  }
  @Override
  public boolean onNewIntent(Intent intent) {

    registrar.activity().setIntent(intent);
    /* Check if the intent contains the callback scheme. */
    if (resourcePath != null && hasCallbackScheme(intent)) {
      requestPaymentStatus(resourcePath);
    }
    return true;
  }
}
