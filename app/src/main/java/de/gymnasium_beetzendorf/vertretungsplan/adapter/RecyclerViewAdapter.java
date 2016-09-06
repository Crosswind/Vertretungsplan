package de.gymnasium_beetzendorf.vertretungsplan.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.Subject;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
        implements View.OnClickListener {

    private List<Subject> subjectList;
    private Context context;

    private int expandedPosition = -1;
    private int prev = -1;

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

        // reference textviews in layout_item
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
        itemCardView.setCardBackgroundColor(ContextCompat.getColor(context, color));


        Log.i(Constants.TAG, currentSubject.getSubject().length() + " " + currentSubject.getTeacher());

        // differentiating between free period or not
        if (currentSubject.getSubject().equals("---")) {
            subjectTextView.setText("frei");
            roomTextView.setText("");
            teacherTextView.setText("");

            // center the subjectTextView vertically
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) subjectTextView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

            roomTextView.setVisibility(View.GONE);

        } else {
            if (currentSubject.getTeacher().length() == 7) {
                subjectTextView.setVisibility(View.INVISIBLE);
            } else {
                subjectTextView.setText(currentSubject.getSubject());
            }

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) subjectTextView.getLayoutParams();

            if (Build.VERSION.SDK_INT > 16) {
                layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
            } else {
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
            }

            roomTextView.setText(currentSubject.getRoom());
            roomTextView.setVisibility(View.VISIBLE);
            teacherTextView.setText(currentSubject.getTeacher());
        }

        // setting the rest of the output
        courseTextView.setText(currentSubject.getCourse());
        periodTextView.setText(String.valueOf(currentSubject.getPeriod()) + ". Stunde");
        infoTextView.setText(currentSubject.getInfo());

        // change visibility on onclick event
        if (position == expandedPosition)

        {
            infoTextView.setVisibility(View.VISIBLE);
        } else if (position == prev)

        {
            infoTextView.setVisibility(View.GONE);
        } else

        {
            infoTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();

        // check if the same item has been clicked - then collapse it
        if (expandedPosition == holder.getLayoutPosition()) {
            prev = expandedPosition;
            expandedPosition = -1;
            notifyItemChanged(prev);
        } else {
            // notifiy previous item to collapse
            if (expandedPosition >= 0) {
                prev = expandedPosition;
                notifyItemChanged(prev);
            }

            // assign newly clicked item
            expandedPosition = holder.getLayoutPosition();
            notifyItemChanged(expandedPosition);
        }
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