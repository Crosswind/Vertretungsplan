package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TabFragment extends Fragment {
    List<Subject> subjectsToDisplay = new ArrayList<>();
    OnSwipeRefreshListener mCallback;

    public void setSubjectsToDisplay(List<Subject> subjectsToDisplay) {
        this.subjectsToDisplay = subjectsToDisplay;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnSwipeRefreshListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interface!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View customView = inflater.inflate(R.layout.fragment_tab, container, false);
        final RecyclerView mainRecyclerView = (RecyclerView) customView.findViewById(R.id.mainRecyclerView);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), subjectsToDisplay);
        mainRecyclerView.setAdapter(adapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mainRecyclerView.setLayoutManager(linearLayoutManager);

        mainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // disable refreshing when the list is not scrolled all the way up
                mCallback.toggleRefreshing((linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0));
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        return customView;
    }

    public interface OnSwipeRefreshListener {
        void toggleRefreshing(boolean enabled);
    }
}
