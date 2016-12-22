package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.DatabaseHandler;
import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.activity.WelcomeActivity;
import de.gymnasium_beetzendorf.vertretungsplan.adapter.CustomListAdapter;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;


public class ChooseClassFragment extends ChooseFragment implements WelcomeActivity.WelcomeActivityContent {

    private final String TAG = ChooseSchoolFragment.class.getSimpleName();
    SharedPreferences mSharedPreferences;
    ListView listView;
    CustomListAdapter adapter;
    int selected = -1;
    List<String> classes;

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
                    mSharedPreferences.edit().putString(Constants.PREFERENCE_CLASS_YEAR_LETTER, classes.get(selected)).apply();
                    doNext();
                } else {
                    Toast.makeText(getActivity(), "Bitte Klasse auswählen.", Toast.LENGTH_SHORT).show();

                }
            }
        };
    }

    @Override
    public boolean shouldDisplay(Context context) {
        mSharedPreferences = getSharedPreferences(context);
        String classYearLetter;

        try {
            classYearLetter = mSharedPreferences.getString(Constants.PREFERENCE_CLASS_YEAR_LETTER, "");
        } catch (NullPointerException e) {
            Log.e(TAG, "class not set yet", e);
            return true;
        }
        return classYearLetter.equalsIgnoreCase("");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.welcome_school_fragment, container, false);

        TextView textView = (TextView) view.findViewById(R.id.selectTextView);
        textView.setText(R.string.welcome_choose_class);

        DatabaseHandler databaseHandler = new DatabaseHandler(getActivity(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
        classes = databaseHandler.getClassList();
        adapter = new CustomListAdapter(getActivity(), R.layout.welcome_list_item, classes);

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
