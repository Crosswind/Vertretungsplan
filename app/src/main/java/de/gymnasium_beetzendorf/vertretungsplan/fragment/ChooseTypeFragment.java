package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import de.gymnasium_beetzendorf.vertretungsplan.activity.WelcomeActivity;


public class ChooseTypeFragment extends ChooseFragment implements WelcomeActivity.WelcomeActivityContent {
    @Override
    protected String getNextButtonText() {
        return null;
    }

    @Override
    protected ChooseFragmentOnclickListener getNextButtonOnclickListener() {
        return null;
    }

    @Override
    protected BroadcastReceiver getBroadcastReceiver() {
        return null;
    }

    @Override
    protected IntentFilter getIntentFilter() {
        return null;
    }

    @Override
    public boolean shouldDisplay(Context context) {
        return false;
    }
}
