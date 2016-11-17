package de.gymnasium_beetzendorf.vertretungsplan.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Subject;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Substitution;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Teacher;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
        implements Constants, View.OnClickListener {

    private List<Substitution> substitutionList;
    private Context context;

    private int expandedPosition = -1;
    private int prev = -1;

    public RecyclerViewAdapter(Context context, List<Substitution> results) {
        substitutionList = results;
        this.context = context;
    }

    public RecyclerViewAdapter(Context context, List<Substitution> results, String type) {

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
        Substitution currentSubstitution = substitutionList.get(position);

        // reference textviews in layout_item
        TextView courseTextView = holder.courseTextView;
        TextView periodTextView = holder.periodTextView;
        TextView teacherTextView = holder.teacherTextView;
        TextView subjectTextView = holder.subjectTextView;
        TextView infoTextView = holder.infoTextView;
        TextView roomTextView = holder.roomTextView;

        CardView itemCardView = holder.itemCardView;

        int color = context.getResources().getIdentifier(Subject.getSubjectShortById(currentSubstitution.getSubject()), "color", null);
        itemCardView.setCardBackgroundColor(ContextCompat.getColor(context, color));


        // differentiating between free period or not
        if (Subject.getSubjectShortById(currentSubstitution.getSubject()).equals("---")) {
            subjectTextView.setText("frei");
            roomTextView.setText("");
            teacherTextView.setText("");

            // center the subjectTextView vertically
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) subjectTextView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

            roomTextView.setVisibility(View.GONE);

        } else {
            if (Teacher.getTeacher_shortById(currentSubstitution.getTeacher()).length() == 7) {
                subjectTextView.setVisibility(View.INVISIBLE);
            } else {
                subjectTextView.setText(currentSubstitution.getSubject());
            }

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) subjectTextView.getLayoutParams();

            if (Build.VERSION.SDK_INT > 16) {
                layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
            } else {
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
            }

            roomTextView.setText(currentSubstitution.getRoom());
            roomTextView.setVisibility(View.VISIBLE);
            teacherTextView.setText(currentSubstitution.getTeacher());
        }


        // setting the rest of the output
        courseTextView.setText(currentSubstitution.getClassYearLetter());
        periodTextView.setText(String.format((String) context.getResources().getText(R.string.period_description), String.valueOf(currentSubstitution.getPeriod())));
        infoTextView.setText(currentSubstitution.getInfo());

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
        return substitutionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView courseTextView;
        TextView periodTextView;
        TextView teacherTextView;
        TextView subjectTextView;
        TextView infoTextView;
        TextView roomTextView;
        CardView itemCardView;

        ViewHolder(View layoutItemView) {
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