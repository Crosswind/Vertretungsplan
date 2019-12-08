package de.gymnasium_beetzendorf.vertretungsplan.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;

/**
 * Created by davidfrenzel on 07/12/2016.
 */

public class CustomListAdapter extends ArrayAdapter<String> {

    private List<String> list;

    public CustomListAdapter(Context context, int resource, List<String> list) {
        super(context, resource, list);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        ViewHolder holder;


        convertView = inflater.inflate(R.layout.welcome_list_item, parent, false);
        holder = new ViewHolder(convertView);
        convertView.setTag(holder);


        holder.getRowText().setText(list.get(position));

        return convertView;
    }

    private static class ViewHolder {
        private View row;
        private TextView rowTextView;

        ViewHolder(View row) {
            this.row = row;
        }

        TextView getRowText() {
            if (this.rowTextView == null) {
                this.rowTextView = row.findViewById(R.id.listViewText);
            }
            return this.rowTextView;
        }
    }
}
