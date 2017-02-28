package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.adapter.RecyclerViewAdapter;
import de.gymnasium_beetzendorf.vertretungsplan.data.Substitution;


public abstract class BaseTabFragment extends Fragment {

    OnSwipeRefreshListener onSwipeRefreshListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            onSwipeRefreshListener = (OnSwipeRefreshListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View customView = inflater.inflate(getLayoutId(), container, false);
        final RecyclerView recyclerView = (RecyclerView) customView.findViewById(getRecyclerViewId());

        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getContext(), getSubstitutionsToDisplay(), getListType());
        recyclerView.setAdapter(recyclerViewAdapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                onSwipeRefreshListener.toggleRefreshing(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        return customView;
    }

    protected abstract int getLayoutId();

    protected abstract int getRecyclerViewId();

    protected abstract List<Substitution> getSubstitutionsToDisplay();

    protected abstract String getListType();

    public interface OnSwipeRefreshListener {
        void toggleRefreshing(boolean enabled);
    }
}
