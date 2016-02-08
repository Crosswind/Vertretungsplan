package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG = "RecyclerViewAdapter";

    private List<Subject> subjectList;
    private Context context;

    private int expandedPosition = -1;

    public RecyclerViewAdapter(Context context, List<Subject> results) {
        subjectList = results;
        this.context = context;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // get context and inflater
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the layout
        View customView = inflater.inflate(R.layout.layout_item, parent, false);

        // create Viewholder and return it
        ViewHolder viewHolder = new ViewHolder(customView);

        // set onclick for the items
        viewHolder.itemView.setOnClickListener(RecyclerViewAdapter.this);
        viewHolder.itemView.setTag(viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        Subject currentSubject = subjectList.get(position);

        // reference textviews in my layout_item
        TextView courseTextView = holder.courseTextView;
        TextView periodTextView = holder.periodTextView;
        TextView teacherTextView = holder.teacherTextView;
        TextView subjectTextView = holder.subjectTextView;
        TextView infoTextView = holder.infoTextView;
        TextView roomTextView = holder.roomTextView;

        CardView itemCardView = holder.itemCardView;

        int color = R.color.defaultColor;
        switch (currentSubject.getSubject()) {
            case "BSB":
                color = R.color.defaultColor;
                break;
            case "DLI":
                color = R.color.defaultColor;
                break;
            case "---":
                if (currentSubject.getRoom().equalsIgnoreCase("---")) color = R.color.freePeriod;
                break;
            case "Mat":
                color = R.color.Mat;
                break;
            case "Eng":
                color = R.color.Eng;
                break;
            case "Che":
                color = R.color.Che;
                break;
            case "Deu":
                color = R.color.Deu;
                break;
            case "Bio":
                color = R.color.Bio;
                break;
            case "Phy":
                color = R.color.Phy;
                break;
            case "Phi":
                color = R.color.Phi;
                break;
            case "Spo":
                color = R.color.Spo;
                break;
            case "Kun":
                color = R.color.Kun;
                break;
            case "Mus":
                color = R.color.Mus;
                break;
            case "Soz":
                color = R.color.Soz;
                break;
            case "Inf":
                color = R.color.Inf;
                break;
            case "Ges":
                color = R.color.Ges;
                break;
            case "Ast":
                color = R.color.Ast;
                break;
            case "Frz":
                color = R.color.foreignLanguage;
                break;
            case "Rus":
                color = R.color.foreignLanguage;
                break;
            case "Lat":
                color = R.color.foreignLanguage;
                break;
            default:
                color = R.color.defaultColor;
                break;
        }
        itemCardView.setBackgroundColor(ContextCompat.getColor(context, color));

        // differentiating between free period or not
        if (currentSubject.getSubject().equals("---")
                && currentSubject.getRoom().equals("---")) {
            subjectTextView.setText("frei");
            roomTextView.setText("");
        } else {
            subjectTextView.setText(currentSubject.getSubject());
            roomTextView.setText(currentSubject.getRoom());
        }

        // setting the rest of the output
        courseTextView.setText(currentSubject.getCourse());
        periodTextView.setText(String.valueOf(currentSubject.getPeriod()) + ". Stunde");
        teacherTextView.setText(currentSubject.getTeacher());
        infoTextView.setText(currentSubject.getInfo());

        // change visibility on onclick event
        if (position == expandedPosition) {
            infoTextView.setVisibility(View.VISIBLE);
        } else {
            infoTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();

        // notify previous item if there's one
        if (expandedPosition >= 0) {
            int prev = expandedPosition;
            notifyItemChanged(prev);
        }

        // notifiy clicked icon to expand
        expandedPosition = holder.getLayoutPosition();
        notifyItemChanged(expandedPosition);
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView courseTextView;
        public TextView periodTextView;
        public TextView teacherTextView;
        public TextView subjectTextView;
        public TextView infoTextView;
        public TextView roomTextView;
        public CardView itemCardView;

        public ViewHolder(View layoutItemView) {
            super(layoutItemView);
            courseTextView = (TextView) layoutItemView.findViewById(R.id.courseTextView);
            periodTextView = (TextView) layoutItemView.findViewById(R.id.periodTextView);
            teacherTextView = (TextView) layoutItemView.findViewById(R.id.teacherTextView);
            subjectTextView = (TextView) layoutItemView.findViewById(R.id.subjectTextView);
            infoTextView = (TextView) layoutItemView.findViewById(R.id.infoTextView);
            roomTextView = (TextView) layoutItemView.findViewById(R.id.roomTextView);

            itemCardView = (CardView) layoutItemView.findViewById(R.id.itemCardView);
        }
    }
}