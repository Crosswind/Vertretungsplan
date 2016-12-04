package de.gymnasium_beetzendorf.vertretungsplan.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by davidfrenzel on 04/12/2016.
 */

public class CustomArrayAdapter extends ArrayAdapter {

    private int hidingItemIndex;

    public CustomArrayAdapter(Context context, int resource, Object[] objects, int hidingItemIndex) {
        super(context, resource, objects);
        this.hidingItemIndex = hidingItemIndex;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return super.getDropDownView(position, convertView, parent);
    }
}
