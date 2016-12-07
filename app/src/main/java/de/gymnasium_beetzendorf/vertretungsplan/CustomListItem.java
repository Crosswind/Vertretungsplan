package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Checkable;

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

    private ImageView checkmark;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        checkmark = (ImageView) findViewById(R.id.checkmark);
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        if (checkmark.getVisibility() == View.GONE) {
            checkmark.setImageResource((checked) ? R.drawable.ic_checkmark : 0);
        }
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
