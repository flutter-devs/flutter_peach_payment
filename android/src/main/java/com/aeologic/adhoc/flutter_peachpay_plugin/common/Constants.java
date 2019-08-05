package com.aeologic.adhoc.flutter_peachpay_plugin.common;

import java.util.LinkedHashSet;
import java.util.Set;


public class Constants {

    /* The configuration values to change across the app */
    public static class Config {

        /* The payment brands for Ready-to-Use UI and Payment Button */
        public static final Set<String> PAYMENT_BRANDS;

        static {
            PAYMENT_BRANDS = new LinkedHashSet<>();

            PAYMENT_BRANDS.add("VISA");
            PAYMENT_BRANDS.add("MASTER");
            //PAYMENT_BRANDS.add("PAYPAL");
            PAYMENT_BRANDS.add("GOOGLEPAY");
        }

        /* The default payment brand for payment button */
        public static final String PAYMENT_BUTTON_BRAND = "GOOGLEPAY";

        /* The default amount and currency */
        public static        String AMOUNT = "49.99";
        public static final String CURRENCY = "EUR";

        /* The card info for SDK & Your Own UI*/
        public static final String CARD_BRAND = "VISA";
        public static final String CARD_HOLDER_NAME = "JOHN DOE";
        public static final String CARD_NUMBER = "4200000000000000";
        public static final String CARD_EXPIRY_MONTH = "07";
        public static final String CARD_EXPIRY_YEAR = "21";
        public static final String CARD_CVV = "123";
    }

    public static final int CONNECTION_TIMEOUT = 5000;

    public static final String BASE_URL = "http://52.59.56.185";
    public static final String MERCHANT_ID = "ff80808138516ef4013852936ec200f2";
    public static final String LOG_TAG = "msdk.demo";
}
