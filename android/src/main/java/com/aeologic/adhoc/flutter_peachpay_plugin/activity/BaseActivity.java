package com.aeologic.adhoc.flutter_peachpay_plugin.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.aeologic.adhoc.flutter_peachpay_plugin.R;


@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    protected void showProgressDialog(int messageId) {
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }

        progressDialog.setMessage(getString(messageId));
        progressDialog.show();
    }

    protected void hideProgressDialog() {
        if (progressDialog == null) {
            return;
        }

        progressDialog.dismiss();
    }

    protected void showAlertDialog(final String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (message.equalsIgnoreCase("The payment was successful")) {
                            Intent intent = new Intent();
                            intent.putExtra("value",message);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                        else {
                            Intent intent = new Intent();
                            intent.putExtra("value",message);
                            setResult(RESULT_CANCELED,intent);
                            finish();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    protected void showAlertDialog(int messageId) {
        showAlertDialog(getString(messageId));
    }
}
