package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.activity.MainActivity;

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
        if (getBroadcastReceiver() != null && getIntentFilter() != null) {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(getBroadcastReceiver());
        }
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getBroadcastReceiver() != null && getIntentFilter() != null) {
            LocalBroadcastManager.getInstance(activity).registerReceiver(getBroadcastReceiver(), getIntentFilter());
        }

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

    protected CoordinatorLayout getCoordinatorLayout() {
        return (CoordinatorLayout) activity.findViewById(R.id.welcome_coordinator);
    }

    protected abstract String getNextButtonText();

    protected abstract ChooseFragmentOnclickListener getNextButtonOnclickListener();

    protected String getResourceString(int id) {
        if (activity != null) {
            return activity.getResources().getString(id);
        }
        return null;
    }

    abstract class ChooseFragmentOnclickListener implements View.OnClickListener {
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

    protected abstract BroadcastReceiver getBroadcastReceiver();

    protected abstract IntentFilter getIntentFilter();

    public interface ChooseFragmentContainer {
        Button getNextButton();

        void setNextButtonEnabled(Boolean enabled);
    }
}

