package de.gymnasium_beetzendorf.vertretungsplan;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TabFragment extends Fragment {
    List<Subject> subjectsToDisplay = new ArrayList<>();
    private SwipeRefreshLayout mainSwipeContainer;

    public List<Subject> getSubjectsToDisplay() {
        return subjectsToDisplay;
    }

    public void setSubjectsToDisplay(List<Subject> subjectsToDisplay) {
        this.subjectsToDisplay = subjectsToDisplay;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View customView = inflater.inflate(R.layout.fragment_tab, container, false);
        RecyclerView mainRecyclerView = (RecyclerView) customView.findViewById(R.id.mainRecyclerView);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), subjectsToDisplay);
        mainRecyclerView.setAdapter(adapter);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final Helper helper = new Helper(getContext(), customView);

        mainSwipeContainer = (SwipeRefreshLayout) customView.findViewById(R.id.mainSwipeContainer);
        mainSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                helper.refresh();
                //mainSwipeContainer.setRefreshing(false);
            }
        });
        mainSwipeContainer.setColorSchemeColors(android.R.color.holo_blue_dark, android.R.color.holo_green_dark, android.R.color.holo_orange_dark); // , android.R.color.holo_red_dark

        return customView;
    }
}
