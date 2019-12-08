package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.util.IabHelper;
import de.gymnasium_beetzendorf.vertretungsplan.util.IabResult;
import de.gymnasium_beetzendorf.vertretungsplan.util.Inventory;
import de.gymnasium_beetzendorf.vertretungsplan.util.Purchase;

public class DonateActivity extends BaseActivity implements Constants {

    static final String ITEM_DONATE_ONE = "de.gymnasium_beetzendorf.vertretungsplan.donate_one_euro";
    static final String ITEM_DONATE_TWO = "de.gymnasium_beetzendorf.vertretungsplan.donate_two_euro";
    static final String ITEM_DONATE_FIVE = "de.gymnasium_beetzendorf.vertretungsplan.donate_five_euro";
    static String ITEM_SKU;

    private IabHelper mHelper;


    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = (purchase, result) -> {
        if (result.isSuccess()) {
            // mButton.setEnabled(true);
        } else {
            Log.i(TAG, "something went wrong in consumefinishedlistener");
        }
    };

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {

            if (result.isFailure()) {
                Log.i(TAG, "something went wrong in on queryinventoryfinished");
            } else {
                mHelper.consumeAsync(inv.getPurchase(ITEM_SKU), mConsumeFinishedListener);
                // mButton.setEnabled(true);
            }
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (result.isFailure()) {
                Log.i(TAG, "something went wrong here while purchasing");
            } else if (info.getSku().equals(ITEM_SKU)) {
                consumeItem();
                // mButton.setEnabled(true);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_donate;
    }

    @Override
    protected Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.mainToolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwq6nvTxbdANQu4J1ru2fEx3DGB3xbEuHP6PcWl6zcLNwhPwjhZeu6Dvgpj/f1NxvehaT0c4US5BEu9XBC16k9hTf/FFHw/9OHr+hC9UtAsMlq07705pdreNVj/J9SYISPFWWMcoMAaRUyFj2ujLdTvs//bI5TO5lgxHqOcK4FeTGTLw4d4LyX10sz+CtDhFukbAqQG7PwkSON+wRJm/9NzXutXkWyFtMFmpsj+dHoQfbLwF82VYej135aZMPRmpd4f2+aScU2BKolJKq3uxYT2RCohmcqj1ZWYGf0mnl3yKi5o9Jnj9uDkeO6u+H7YUKGZMWHw54KlNIZX/OLGSe+QIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true, TAG);
        mHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                Log.i(TAG, "IAP setup failed: " + result);
            } else {
                Log.i(TAG, "IAP setup successful");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
        }
        mHelper = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void donateOne(View view) {
        ITEM_SKU = ITEM_DONATE_ONE;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchase token");
    }

    public void donateTwo(View view) {
        ITEM_SKU = ITEM_DONATE_TWO;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchase token");

    }

    public void donateFive(View view) {
        ITEM_SKU = ITEM_DONATE_FIVE;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchase token");
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
        Toast.makeText(this, "Danke für die Spende!", Toast.LENGTH_LONG).show();
    }
}
