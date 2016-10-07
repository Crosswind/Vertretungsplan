package de.gymnasium_beetzendorf.vertretungsplan.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.Schoolday;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ScheduleTabFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.SubstitutionTabFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numberOfTabs;
    List<Schoolday> resultsToDisplay = new ArrayList<>();
    String type;

    public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs, String type, List<Schoolday> resultsToDisplay) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
        this.resultsToDisplay = resultsToDisplay;
        this.type = type;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (type) {
            case "substitution":
                SubstitutionTabFragment substitutionTabFragment = new SubstitutionTabFragment();
                substitutionTabFragment.setLessonsToDisplay(resultsToDisplay.get(position).getLessons());
                return substitutionTabFragment;
            case "schedule":
                ScheduleTabFragment scheduleTabFragment = new ScheduleTabFragment();
                return scheduleTabFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
