package de.gymnasium_beetzendorf.vertretungsplan;

import android.text.style.URLSpan;
import android.view.View;

/**
 * Created by davidfrenzel on 26.04.17.
 */

public class ChromeTabsUrlSpan extends URLSpan {
    ChromeTabsUrlSpan(String url) {
        super(url);
    }

    @Override
    public void onClick(View widget) {

    }
}
