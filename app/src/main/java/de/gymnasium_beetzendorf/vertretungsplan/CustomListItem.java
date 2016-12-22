package de.gymnasium_beetzendorf.vertretungsplan;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by davidfrenzel on 07/12/2016.
 */

public class CustomListItem extends LinearLayout implements Checkable {
    public CustomListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListItem(Context context) {
        super(context);
    }

    private boolean checked = false;
    private TextView textView;
    private ObjectAnimator objectAnimator;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        textView = (TextView) findViewById(R.id.listViewText);
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        int color = checked ? R.color.colorAccent : R.color.appWhite;

        textView.setBackgroundColor(ContextCompat.getColor(getContext(), color));
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }
}
