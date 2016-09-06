package de.gymnasium_beetzendorf.vertretungsplan.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.Schoolday;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.TabFragment;

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
        return tabFragment;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
