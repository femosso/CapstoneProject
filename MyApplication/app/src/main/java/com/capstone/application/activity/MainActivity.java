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
import android.support.v7.app.ActionBar;
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
import com.capstone.application.fragment.PendingCheckInsFragment;
import com.capstone.application.fragment.PreferencesFragment;
import com.capstone.application.fragment.TeensFragment;
import com.capstone.application.utils.Constants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;

public class MainActivity extends AppCompatActivity implements NavigationDrawerCallbacks {
    private Context mContext;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private BroadcastReceiver mUpdateFollowRequestReceiver;

    private TeenAlarmReceiver mAlarm;

    private TextView mTextPendingRequest;

    private int mUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);

        }

        // saves the type of user logged in
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mUserType = sharedPreferences.getInt(Constants.USER_TYPE, -1);

        if (mUserType == -1) {
            Toast.makeText(mContext, getString(R.string.no_user_logged_in), Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up the navigation drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragmentDrawer);

        mNavigationDrawerFragment
                .setup(R.id.fragmentDrawer, (DrawerLayout) findViewById(R.id.drawer), toolbar);

        // set up the fragment with the current position
        displayView(mNavigationDrawerFragment.getCurrentPosition());

        // only set up alarm and make follow request activity available if logged user is a teen
        if (mUserType == Constants.UserType.TEEN.ordinal()) {
            // set up alarm for the logged teen
            mAlarm = new TeenAlarmReceiver(getApplicationContext());
            mAlarm.setAlarm();

            // set up receiver to update follow request counter in action bar
            mUpdateFollowRequestReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Constants.NEW_FOLLOW_REQUEST_ACTION)) {
                        updateFollowRequestCounter();
                    }
                }
            };
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);

        // only register the receiver to update follow request counter if logged user is a teen
        if (mUserType == Constants.UserType.TEEN.ordinal()) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateFollowRequestReceiver,
                    new IntentFilter(Constants.NEW_FOLLOW_REQUEST_ACTION));

            updateFollowRequestCounter();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if (mUserType != Constants.UserType.TEEN.ordinal() && position > 1) {
            position++;
        }

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
                fragment = new PendingCheckInsFragment();
                title = getString(R.string.nav_item_check_ins);
                break;
            case 3:
                fragment = new PreferencesFragment();
                title = getString(R.string.nav_item_settings);
                break;
            case 4:
                onClickLogout();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerBody, fragment);
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

        if (mUserType == Constants.UserType.TEEN.ordinal()) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateFollowRequestReceiver);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);

            MenuItem pendingRequest = menu.findItem(R.id.menu_pending_request);

            // it will only have pending request option menu if user is a teen
            if (mUserType == Constants.UserType.TEEN.ordinal()) {
                MenuItemCompat.setActionView(pendingRequest, R.layout.action_bar_notification_icon);

                View pendingRequestView = MenuItemCompat.getActionView(pendingRequest);
                mTextPendingRequest = (TextView) pendingRequestView.findViewById(R.id.txtCounter);

                pendingRequestView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(mContext, FollowRequestActivity.class));
                    }
                });

                updateFollowRequestCounter();
            } else {
                pendingRequest.setVisible(false);
            }

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_action_about) {
            startActivity(new Intent(mContext, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickLogout() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        sharedPreferences.edit().putInt(Constants.SIGN_IN_PROVIDER, -1).apply();
        sharedPreferences.edit().putInt(Constants.USER_TYPE, -1).apply();
        sharedPreferences.edit().putString(Constants.LOGGED_EMAIL, null).apply();

        LoginManager.getInstance().logOut();

        if (mAlarm != null) {
            mAlarm.cancelAlarm(mContext);
        }

        // return to login activity
        startLoginActivity();
    }

    private void startLoginActivity() {
        // kills this main screen
        finish();

        // initialize login screen of the app
        startActivity(new Intent(mContext, LoginActivity.class));
    }

    public void updateFollowRequestCounter() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        final int followRequestCounter = sharedPreferences.getInt(Constants.PENDING_FOLLOW_REQUEST_COUNTER, 0);

        // call the updating code on the main thread, so we can call this asynchronously
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextPendingRequest != null) {
                    if (followRequestCounter == 0)
                        mTextPendingRequest.setVisibility(View.INVISIBLE);
                    else {
                        mTextPendingRequest.setVisibility(View.VISIBLE);
                        mTextPendingRequest.setText(Integer.toString(followRequestCounter));
                    }
                }
            }
        });
    }
}