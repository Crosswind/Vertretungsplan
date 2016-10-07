package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseClassFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseCourseFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseSchoolFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ChooseTypeFragment;

/**
 * Created by David on 19.09.16.
 */
public class WelcomeActivity extends AppCompatActivity implements ChooseFragment.ChooseFragmentContainer {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static boolean shouldDisplay(Context context) {
        return false;
    }

    private static List<WelcomeActivityContent> getWelcomeFragments() {
        return new ArrayList<WelcomeActivityContent>(Arrays.asList(
                new ChooseSchoolFragment(),
                new ChooseClassFragment(),
                new ChooseCourseFragment(),
                new ChooseTypeFragment()
        ));
    }

    @Override
    public Button getNextButton() {
        return null;
    }

    @Override
    public void setNextButtonEnabled(Boolean enabled) {

    }

    public interface WelcomeActivityContent {
        // WelcomeFragments need to implement this
        boolean shouldDisplay(Context context);
    }
}
