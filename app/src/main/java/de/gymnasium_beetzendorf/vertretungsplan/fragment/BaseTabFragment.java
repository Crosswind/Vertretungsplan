package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.adapter.RecyclerViewAdapter;
import de.gymnasium_beetzendorf.vertretungsplan.data.Substitution;


public abstract class BaseTabFragment extends Fragment {

    private OnSwipeRefreshListener onSwipeRefreshListener;

    @Override
    public void onAttach(@NonNull Context context) {
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
        final RecyclerView recyclerView = customView.findViewById(getRecyclerViewId());

        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getContext(), getSubstitutionsToDisplay(), getListType());
        recyclerView.setAdapter(recyclerViewAdapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
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
