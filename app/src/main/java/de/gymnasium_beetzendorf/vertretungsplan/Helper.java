package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import java.util.List;

// class to provide certain methods that will be needed across the whole app
// this shrinks down the MainActivity whereas lots of things will be moved here
public class Helper {
    public static final String TAG = ".vertretungsplan";

    Context context;
    View view;
    List<Schoolday> results;

    public Helper(Context context, View view) {
        this.context = context;
        this.view = view;
    }

    public void refresh () {
        SwipeRefreshLayout srl = (SwipeRefreshLayout) view.findViewById(R.id.mainSwipeContainer);
        srl.setRefreshing(true);
    }
}
