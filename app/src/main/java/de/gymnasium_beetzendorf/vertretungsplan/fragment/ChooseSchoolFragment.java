package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.activity.WelcomeActivity;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data1.School;

/**
 * Created by David on 27.09.16.
 */
public class ChooseSchoolFragment extends ChooseFragment implements WelcomeActivity.WelcomeActivityContent {

    private final String TAG = ChooseSchoolFragment.class.getSimpleName();
    SharedPreferences mSharedPreferences;
    Spinner mSpinner;

    @Override
    protected String getNextButtonText() {
        return getResourceString(R.string.welcome_next);
    }

    @Override
    protected View.OnClickListener getNextButtonOnclickListener() {
        return new ChooseFragmentOnclickListener(activity) {
            @Override
            public void onClick(View v) {
                if (mSpinner != null) {
                    Log.i(TAG, "schule ausgewählt: " + mSpinner.getSelectedItem().toString());
                    if (mSpinner.getSelectedItemPosition() != Spinner.INVALID_POSITION && !mSpinner.getSelectedItem().toString().equalsIgnoreCase("[Schule]")) {
                        mSharedPreferences.edit().putInt(Constants.PREFERENCE_SCHOOL, mSpinner.getSelectedItemPosition() + 1).apply();  // +1 because school start with 1 and the first index in spinner is 0
                        doNext();
                    } else {
                        Toast.makeText(activity, "Bitte wähle deine Schule aus!", Toast.LENGTH_LONG).show();
                        //doFinish();
                    }
                }
            }
        };
    }

    @Override
    public boolean shouldDisplay(Context context) {
        mSharedPreferences = getSharedPreferences(context);
        int school;

        try {
            school = mSharedPreferences.getInt(Constants.PREFERENCE_SCHOOL, 0);
        } catch (NullPointerException e) {
            Log.e(TAG, "school not set yet", e);
            return true;
        }
        return school <= 0;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.welcome_school_fragment, container, false);

        final List<String> schools = School.schoolListNames();
        schools.add("[Schule]");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, schools) {
            @Override
            public int getCount() {
                return schools.size();
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner = (Spinner) view.findViewById(R.id.schoolDropdown);
        mSpinner.setAdapter(adapter);


        return view;
    }
}
