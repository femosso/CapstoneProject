package com.capstone.application.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.capstone.application.R;
import com.capstone.application.adapter.FollowRequestListAdapter;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FollowRequestActivity extends AppCompatActivity {
    private static final String TAG = FollowRequestActivity.class.getName();

    private Context mContext;

    private RecyclerView mRecyclerView;

    private TextView mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_request);

        mContext = getApplicationContext();

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.activity_name_follow_request));
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);

        mEmptyView = (TextView) findViewById(R.id.txtEmptyView);

        new RetrievePendingFollowRequestTask(FollowRequestActivity.this).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class RetrievePendingFollowRequestTask extends AsyncTask<Void, Void, List<User>> {
        private ProgressDialog dialog;
        private Activity mActivity;

        public RetrievePendingFollowRequestTask(FollowRequestActivity activity) {
            mActivity = activity;
            dialog = new ProgressDialog(mActivity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_loading));
            dialog.show();
        }

        @Override
        protected List<User> doInBackground(Void... params) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            Log.d(TAG, loggedEmail + " is contacting server to retrieve list of pending follow requests");

            List<User> userList = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.TEEN_CONTROLLER + File.separator +
                        RestUriConstants.PENDING + File.separator + RestUriConstants.LIST + "?" +
                        RestUriConstants.PARAM_EMAIL + "=" + loggedEmail;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to Teen object
                User[] result = restTemplate.getForObject(url, User[].class);

                if (result != null) {
                    userList = Arrays.asList(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return userList;
        }

        @Override
        protected void onPostExecute(List<User> result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            result = result == null ? new ArrayList<User>() : result;

            RecyclerView.Adapter adapter = new FollowRequestListAdapter(FollowRequestActivity.this, result);
            mRecyclerView.setAdapter(adapter);

            refreshUi(result);
        }
    }

    public void refreshUi(List<User> userList) {
        if (userList.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        // update MainActivity option menu with new count of pending follow requests
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putInt(Constants.PENDING_FOLLOW_REQUEST_COUNTER, userList.size()).apply();
    }
}
