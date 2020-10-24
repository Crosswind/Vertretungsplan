package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.DatabaseHandler;
import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.RefreshService;
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
    ProgressDialog mProgressDialog;
    DatabaseHandler mDatabaseHandler;

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
                    mSharedPreferences.edit().putBoolean(Constants.PREFERENCE_SHOW_WHOLE_PLAN, false).apply();
                    mSharedPreferences.edit().putBoolean(Constants.PREFERENCE_NOTIFICATIONS_ENABLED, true).apply();
                    doNext();
                } else {
                    Snackbar.make(getCoordinatorLayout(), "Bitte Klasse auswählen", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                }
            }
        };
    }

    @Override
    protected BroadcastReceiver getBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                fillListBox();
            }
        };
    }

    @Override
    protected IntentFilter getIntentFilter() {
        return new IntentFilter("classlist_updated");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Intent intent = new Intent(activity, RefreshService.class);
        intent.putExtra(RefreshService.INSTRUCTION, RefreshService.CLASSLIST_REFRESH);
        activity.startService(intent);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.welcome_school_fragment, container, false);

        TextView textView = view.findViewById(R.id.selectTextView);
        textView.setText(R.string.welcome_choose_class);

        listView = view.findViewById(R.id.schoolListView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mDatabaseHandler = new DatabaseHandler(getActivity(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
        classes = mDatabaseHandler.getClassList();

        if (classes.size() == 0) {
            mProgressDialog = new ProgressDialog(view.getContext());
            mProgressDialog.setCancelable(true);
            mProgressDialog.setMessage("Klassen werden geladen");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();
        } else {
            fillListBox();
        }


        listView.setOnItemClickListener((parent, view1, position, id) -> selected = position);

        return view;
    }

    private void fillListBox() {
        classes = mDatabaseHandler.getClassList();
        adapter = new CustomListAdapter(getActivity(), R.layout.welcome_list_item, classes);
        listView.setAdapter(adapter);

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
