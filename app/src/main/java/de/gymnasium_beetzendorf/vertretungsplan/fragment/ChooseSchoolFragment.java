package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.RefreshService;
import de.gymnasium_beetzendorf.vertretungsplan.activity.WelcomeActivity;
import de.gymnasium_beetzendorf.vertretungsplan.adapter.CustomListAdapter;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data1.School;

public class ChooseSchoolFragment extends ChooseFragment implements WelcomeActivity.WelcomeActivityContent {

    private final String TAG = ChooseSchoolFragment.class.getSimpleName();
    SharedPreferences mSharedPreferences;
    ListView listView;
    CustomListAdapter adapter;
    int selected = -1;


    @Override
    protected String getNextButtonText() {
        return getResourceString(R.string.welcome_next);
    }

    @Override
    protected ChooseFragmentOnclickListener getNextButtonOnclickListener() {
        return new ChooseFragmentOnclickListener(activity) {
            @Override
            public void onClick(View v) {
                if (selected >= 0) {
                    mSharedPreferences.edit().putInt(Constants.PREFERENCE_SCHOOL, selected).apply();
                    doNext();
                } else {
                    Snackbar.make(getCoordinatorLayout(), "Bitte Schule auswählen", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            }
        };
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
        mSharedPreferences = getSharedPreferences(context);
        int school;

        try {
            school = mSharedPreferences.getInt(Constants.PREFERENCE_SCHOOL, -1);
        } catch (NullPointerException e) {
            Log.e(TAG, "school not set yet", e);
            return true;
        }
        return school < 0;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.welcome_school_fragment, container, false);

        final List<String> schools = School.schoolListNames();
        adapter = new CustomListAdapter(getActivity(), R.layout.welcome_list_item, schools);

        listView = (ListView) view.findViewById(R.id.schoolListView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
            }
        });

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);

        return view;
    }
}
