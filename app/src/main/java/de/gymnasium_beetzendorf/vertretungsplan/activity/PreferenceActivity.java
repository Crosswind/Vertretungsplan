package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.PreferenceFragment;

public class PreferenceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this workaround is needed because else the back button won't work
        // related to the PreferenceActivity thing
        getToolbar().setNavigationOnClickListener(v -> NavUtils.navigateUpFromSameTask(PreferenceActivity.this));

        getSupportFragmentManager().beginTransaction().replace(R.id.settingsContainer, new PreferenceFragment()).commit();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_preference;
    }

    @Override
    protected Toolbar getToolbar() {
        return findViewById(R.id.mainToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            /*
            I have removed this next function call because it was deprecated and doesn't seem
            to serve any kind of purpose anymore
            Someone has mentioned this here:
            https://stackoverflow.com/questions/72634225/onbackpressed-is-deprecated-what-is-the-alternative
             */
            //onBackPressed();
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
