package de.gymnasium_beetzendorf.vertretungsplan;

import android.os.Parcel;
import android.text.style.URLSpan;
import android.view.View;

/**
 * Created by davidfrenzel on 26.04.17.
 */

public class ChromeTabsUrlSpan extends URLSpan {
    public ChromeTabsUrlSpan(String url) {
        super(url);
    }

    public ChromeTabsUrlSpan(Parcel src) {
        super(src);
    }

    @Override
    public void onClick(View widget) {

    }
}
