package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import java.util.ArrayList;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.data.Lesson;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Substitution;

public class SubstitutionTabFragment extends BaseTabFragment {

    List<Substitution> substitutionsToDisplay = new ArrayList<>();

    public void setLessonsToDisplay(List<Substitution> substitutionsToDisplay) {
        this.substitutionsToDisplay = substitutionsToDisplay;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tab;
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.mainRecyclerView;
    }

    @Override
    protected List<Substitution> getSubstitutionsToDisplay() {
        return substitutionsToDisplay;
    }

    @Override
    protected String getListType() {
        return "substitution";
    }

}
