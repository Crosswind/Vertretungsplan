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


        getFragmentManager().beginTransaction()
                .replace(R.id.settingsContainer, new PreferenceFragment())
                .commit();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_preference;
    }

    @Override
    protected Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.mainToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
