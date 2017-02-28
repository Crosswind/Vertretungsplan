package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.data.Substitution;

public class SubstitutionTabFragment extends BaseTabFragment {

    List<Substitution> substitutionsToDisplay = new ArrayList<>();

    public void setLessonsToDisplay(List<Substitution> substitutionsToDisplay) {
        this.substitutionsToDisplay = substitutionsToDisplay;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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
