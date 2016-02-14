package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TabFragment extends Fragment {
    List<Subject> subjectsToDisplay = new ArrayList<>();
    OnSwipeRefreshListener mCallback;

    public List<Subject> getSubjectsToDisplay() {
        return subjectsToDisplay;
    }

    public void setSubjectsToDisplay(List<Subject> subjectsToDisplay) {
        this.subjectsToDisplay = subjectsToDisplay;
    }

    public interface OnSwipeRefreshListener {
        void refresh ();
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: implement a request to new data
        DatabaseHandler databaseHandler = new DatabaseHandler(getContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);

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
        RecyclerView mainRecyclerView = (RecyclerView) customView.findViewById(R.id.mainRecyclerView);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), subjectsToDisplay);
        mainRecyclerView.setAdapter(adapter);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final RefreshService refreshService = RefreshService.getInstance();

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) customView.findViewById(R.id.mainSwipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCallback.refresh();
                //swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.Inf); //

        return customView;
    }
}
