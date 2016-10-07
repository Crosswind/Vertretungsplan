package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import java.util.ArrayList;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.data.Lesson;

public class SubstitutionTabFragment extends BaseTabFragment {

    List<Lesson> lessonsToDisplay = new ArrayList<>();

    public void setLessonsToDisplay(List<Lesson> lessonsToDisplay) {
        this.lessonsToDisplay = lessonsToDisplay;
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
    protected List<Lesson> getLessonsToDisplay() {
        return lessonsToDisplay;
    }

    @Override
    protected String getListType() {
        return "substitution";
    }

}
