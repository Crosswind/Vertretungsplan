package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.Context;

import de.gymnasium_beetzendorf.vertretungsplan.activity.WelcomeActivity;

/**
 * Created by David on 27.09.16.
 */
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
    public boolean shouldDisplay(Context context) {
        return false;
    }
}
