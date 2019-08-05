package com.aeologic.adhoc.flutter_peachpay_plugin.task;


public interface PaymentStatusRequestListener {

    void onErrorOccurred();
    void onPaymentStatusReceived(String paymentStatus);
}
