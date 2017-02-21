package de.gymnasium_beetzendorf.vertretungsplan.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.Schoolday;
import de.gymnasium_beetzendorf.vertretungsplan.data1.SubstitutionDay;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ScheduleTabFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.SubstitutionTabFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private List<String> tabTitles;
    private List<SubstitutionDay> resultsToDisplay = new ArrayList<>();
    private String type;

    public PagerAdapter(FragmentManager fragmentManager, List<String> tabTitles, String type, List<SubstitutionDay> resultsToDisplay) {
        super(fragmentManager);
        this.tabTitles = tabTitles;
        this.resultsToDisplay = resultsToDisplay;
        this.type = type;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (type) {
            case "substitution":
                SubstitutionTabFragment substitutionTabFragment = new SubstitutionTabFragment();
                substitutionTabFragment.setLessonsToDisplay(resultsToDisplay.get(position).getSubstitutionList());
                return substitutionTabFragment;
            case "schedule":
                return new ScheduleTabFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }
}
