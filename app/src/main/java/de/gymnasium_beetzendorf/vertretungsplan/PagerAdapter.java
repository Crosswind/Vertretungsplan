package de.gymnasium_beetzendorf.vertretungsplan;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numberOfTabs;
    List<Schoolday> resultsToDisplay = new ArrayList<>();

    public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs, List<Schoolday> resultsToDisplay) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
        this.resultsToDisplay = resultsToDisplay;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        TabFragment tabFragment = new TabFragment();
        tabFragment.setSubjectsToDisplay(resultsToDisplay.get(position).getSubjects());
        //Log.i(MainActivity.TAG, "fragment id: " + String.valueOf(tabFragment.getId()));
        return tabFragment;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
