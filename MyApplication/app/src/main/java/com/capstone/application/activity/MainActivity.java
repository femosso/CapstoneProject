package com.capstone.application.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.adapter.NavigationDrawerCallbacks;
import com.capstone.application.adapter.NavigationDrawerFragment;
import com.capstone.application.alarm.TeenAlarmReceiver;
import com.capstone.application.fragment.HomeFragment;
import com.capstone.application.fragment.PreferencesFragment;
import com.capstone.application.fragment.TeensFragment;
import com.capstone.application.utils.Constants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerCallbacks {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private Context mContext;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        /*if (!LoginActivity.isUserLoggedInViaFacebook()) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_logged_user),
                    Toast.LENGTH_SHORT).show();
            finish();
        }*/

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up the  navigation drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);

        mNavigationDrawerFragment
                .setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), toolbar);

        // set up the fragment with the current position
        displayView(mNavigationDrawerFragment.getCurrentPosition());

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.NEW_FOLLOW_REQUEST_ACTION)) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    int counter = sharedPreferences.getInt(Constants.NOTIFICATION_COUNTER, -1);

                    if (counter != -1) {
                        updateHotCount();
                    }
                }
            }
        };

        // set up alarm
        TeenAlarmReceiver alarm = new TeenAlarmReceiver(mContext);
        alarm.setAlarm(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.NEW_FOLLOW_REQUEST_ACTION));

        updateHotCount();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                title = getString(R.string.nav_item_home);
                break;
            case 1:
                fragment = new TeensFragment();
                title = getString(R.string.nav_item_teens);
                break;
            case 2:
                fragment = new PreferencesFragment();
                title = getString(R.string.nav_item_settings);
                break;
            case 3:
                onClickLogout();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title if it has already been initialized
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.deactivateApp(this);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);

            MenuItem menuItem = menu.findItem(R.id.menu_hotlist);
            MenuItemCompat.setActionView(menuItem, R.layout.action_bar_notifitcation_icon);

            final View menu_hotlist = MenuItemCompat.getActionView(menuItem);
            ui_hot = (TextView) menu_hotlist.findViewById(R.id.hotlist_hot);

            menu_hotlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mContext, PendingFollowRequestActivity.class));
                }
            });

            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_search) {
            Toast.makeText(mContext, "Search action is selected!",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onClickLogout() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        sharedPreferences.edit().putInt(Constants.SIGN_IN_PROVIDER, -1).apply();
        sharedPreferences.edit().putString(Constants.LOGGED_EMAIL, null).apply();

        LoginManager.getInstance().logOut();
        startLoginActivity();
    }

    private void startLoginActivity() {
        // kills this main screen
        finish();

        // initialize login screen of the app
        Intent intent = new Intent(mContext, LoginActivity.class);
        startActivity(intent);
    }

    private TextView ui_hot = null;

    // call the updating code on the main thread,
    // so we can call this asynchronously
    public void updateHotCount() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        final int counter = sharedPreferences.getInt(Constants.NOTIFICATION_COUNTER, 0);

        if (ui_hot == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (counter == 0)
                    ui_hot.setVisibility(View.INVISIBLE);
                else {
                    ui_hot.setVisibility(View.VISIBLE);
                    ui_hot.setText(Integer.toString(counter));
                }
            }
        });
    }
}