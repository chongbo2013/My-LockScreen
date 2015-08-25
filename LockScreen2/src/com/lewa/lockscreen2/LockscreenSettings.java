
package com.lewa.lockscreen2;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import lewa.support.v7.app.ActionBarActivity;
import lewa.support.v7.app.ActionBar;


/**
 * Created by lewa on 3/6/15.
 */
public class LockscreenSettings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = this.getSupportActionBar();
        int flag = actionBar.getDisplayOptions();
        actionBar.setDisplayOptions(flag ^ ActionBar.DISPLAY_HOME_AS_UP ^ ActionBar.DISPLAY_SHOW_HOME);
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        LockscreenSettingsFragment settingsFragment = new LockscreenSettingsFragment();
        fragmentTransaction.replace(android.R.id.content, settingsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}
