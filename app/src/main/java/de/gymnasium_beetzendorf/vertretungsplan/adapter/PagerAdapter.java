package de.gymnasium_beetzendorf.vertretungsplan.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.SubstitutionDay;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.ScheduleTabFragment;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.SubstitutionTabFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private final List<String> tabTitles;
    private final List<SubstitutionDay> resultsToDisplay;
    private final String type;

    public PagerAdapter(FragmentManager fragmentManager, List<String> tabTitles, String type, List<SubstitutionDay> resultsToDisplay) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.tabTitles = tabTitles;
        this.resultsToDisplay = resultsToDisplay;
        this.type = type;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
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
