package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.gymnasium_beetzendorf.vertretungsplan.activity.MainActivity;
import de.gymnasium_beetzendorf.vertretungsplan.activity.WelcomeActivity;

/**
 * Created by David on 19.09.16.
 */
public abstract class ChooseFragment extends Fragment {

    protected Activity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (activity instanceof ChooseFragmentContainer) {
            ChooseFragmentContainer activity = (ChooseFragmentContainer) this.activity;

            attachToNextButton(activity.getNextButton());
        }

        return view;
    }

    protected void attachToNextButton(Button button) {
        button.setText(getNextButtonText());

        button.setOnClickListener(getNextButtonOnclickListener());
    }

    protected SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    protected abstract String getNextButtonText();

    protected abstract ChooseFragmentOnclickListener getNextButtonOnclickListener();

    protected String getResourceString(int id) {
        if (activity != null) {
            return activity.getResources().getString(id);
        }
        return null;
    }

    protected abstract class ChooseFragmentOnclickListener implements View.OnClickListener {
        Activity activity;

        ChooseFragmentOnclickListener(Context context) {
            activity = (Activity) context;
        }

        void doNext() {
            Intent intent = new Intent(activity, MainActivity.class);
            startActivity(intent);
            activity.finish();
        }

        void doFinish() {
            activity.finish();
        }
    }

    public interface ChooseFragmentContainer {
        Button getNextButton();

        void setNextButtonEnabled(Boolean enabled);
    }
}

