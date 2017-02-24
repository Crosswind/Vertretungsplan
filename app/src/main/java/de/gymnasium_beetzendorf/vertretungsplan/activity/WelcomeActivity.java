package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.RefreshService;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseClassFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseCourseFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseSchoolFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseTypeFragment;

public class WelcomeActivity extends AppCompatActivity implements ChooseFragment.ChooseFragmentContainer {

    private final String TAG = WelcomeActivity.class.getSimpleName();
    WelcomeActivityContent mWelcomeFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "in Welcome Activity now");
        setContentView(R.layout.activity_choose_coord);

        mWelcomeFragment = getWelcomeFragment(this);

        if (mWelcomeFragment == null) {
            finish();
        }

        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.welcome_content, (android.app.Fragment) mWelcomeFragment);
        fragmentTransaction.commit();
    }

    public static boolean shouldDisplay(Context context) {
        WelcomeActivityContent fragment = getWelcomeFragment(context);

        return fragment != null;

    }

    private static List<WelcomeActivityContent> getWelcomeFragments() {
        return new ArrayList<WelcomeActivityContent>(Arrays.asList(
                new ChooseSchoolFragment(),
                new ChooseClassFragment(),
                new ChooseCourseFragment(),
                new ChooseTypeFragment()
        ));
    }

    private static WelcomeActivityContent getWelcomeFragment(Context context) {
        List<WelcomeActivityContent> welcomeActivityContents = getWelcomeFragments();

        for (WelcomeActivityContent fragment : welcomeActivityContents) {
            if (fragment.shouldDisplay(context)) {
                return fragment;
            }
        }

        return null;
    }

    @Override
    public Button getNextButton() {
        return (Button) findViewById(R.id.nextButton);
    }

    @Override
    public void setNextButtonEnabled(Boolean enabled) {
        try {
            getNextButton().setEnabled(enabled);
        } catch (NullPointerException e) {
            Log.e(TAG, "there is no button to enable/disable");
        }
    }

    public interface WelcomeActivityContent {
        // WelcomeFragments need to implement this
        boolean shouldDisplay(Context context);
    }
}
