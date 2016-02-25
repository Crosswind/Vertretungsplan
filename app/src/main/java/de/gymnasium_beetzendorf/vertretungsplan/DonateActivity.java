package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import de.gymnasium_beetzendorf.vertretungsplan.util.IabHelper;
import de.gymnasium_beetzendorf.vertretungsplan.util.IabResult;
import de.gymnasium_beetzendorf.vertretungsplan.util.Inventory;
import de.gymnasium_beetzendorf.vertretungsplan.util.Purchase;

public class DonateActivity extends AppCompatActivity {

    private Button mButton;
    private IabHelper mHelper;
    static String ITEM_SKU;
    static final String ITEM_DONATE_ONE = "de.gymnasium_beetzendorf.vertretungsplan.donate_one_euro";
    static final String ITEM_DONATE_TWO = "de.gymnasium_beetzendorf.vertretungsplan.donate_two_euro";
    static final String ITEM_DONATE_FIVE = "de.gymnasium_beetzendorf.vertretungsplan.donate_five_euro";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        String base64DecodedPublicKey = "miibijanbgkqhkig9w0baqefaaocaq8amiibcgkcaqeawq6nvtxbdanqu4j1ru2fex3dgb3xbeuhp6pcwl6zclnwhpwjhzeu6dvgpj/f1nxvehat0c4us5beu9xbc16k9htf/ffhw/9ohr+hc9utasmlq07705pdrenvj/j9syispfwwmcomaaruyfj2ujldtvs//bi5to5lgxhqock4fetgtlw4d4lyx10sz+ctdhfukbaqqg7pwkson+wrjm/9nzxutxkwyftmfmpsj+dhoqfblwf82vyej135azmprmpd4f2+ascu2bkoljkq3uxyt2rcohmcqj1zwygf0mnl3yki5o9jnj9udkeo6u+h7yukgzmwhw54klnizx/olgse+qidaqab/f1NxvehaT0c4US5BEu9XBC16k9hTf/FFHw/9OHr+hC9UtAsMlq07705pdreNVj/J9SYISPFWWMcoMAaRUyFj2ujLdTvs//bI5TO5lgxHqOcK4FeTGTLw4d4LyX10sz+CtDhFukbAqQG7PwkSON+wRJm/9NzXutXkWyFtMFmpsj+dHoQfbLwF82VYej135aZMPRmpd4f2+aScU2BKolJKq3uxYT2RCohmcqj1ZWYGf0mnl3yKi5o9Jnj9uDkeO6u+H7YUKGZMWHw54KlNIZX/OLGSe+QIDAQAB";

        mHelper = new IabHelper(this, encodePublicKey(base64DecodedPublicKey));
        mHelper.enableDebugLogging(true, MainActivity.TAG);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.i(MainActivity.TAG, "IAP setup failed: " + result);
                } else {
                    Log.i(MainActivity.TAG, "IAP setup successful");
                }
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

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (result.isFailure()) {
                Log.i(MainActivity.TAG, "something went wrong here while purchasing");
            } else if (info.getSku().equals(ITEM_SKU)) {
                mButton.setEnabled(false);
                consumeItem();
            }
        }
    };

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {

            if (result.isFailure()) {
                Log.i(MainActivity.TAG, "something went wrong in on queryinventoryfinished");
            } else {
                mHelper.consumeAsync(inv.getPurchase(ITEM_SKU), mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                mButton.setEnabled(true);
            } else {
                Log.i(MainActivity.TAG, "something went wrong in consumefinishedlistener");
            }
        }
    };

    public void donateOne(View view) {
        mButton = (Button) findViewById(R.id.donateOneButton);
        ITEM_SKU = ITEM_DONATE_ONE;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchase token");
    }

    public void donateTwo(View view) {
        mButton = (Button) findViewById(R.id.donateTwoButton);
        ITEM_SKU = ITEM_DONATE_TWO;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchase token");

    }

    public void donateFive(View view) {
        mButton = (Button) findViewById(R.id.donateFiveButton);
        ITEM_SKU = ITEM_DONATE_FIVE;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, "purchase token");
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
        Toast.makeText(this, "Danke für die Spende!", Toast.LENGTH_LONG).show();
    }

    private String encodePublicKey (String base64DecodedPublicKey) {
        byte[] bytes = base64DecodedPublicKey.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] >= 'A' && bytes[i] <= 'Z') {
                bytes[i] = (byte) ('a' + (bytes[i] - 'A'));
            } else if (bytes[i] >= 'a' && bytes[i] <= 'z') {
                bytes[i] = (byte) ('A' + (bytes[i] - 'a'));
            }
        }

        return new String(bytes);
    }
}
