package com.capstone.application.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import com.capstone.application.R;
import com.capstone.application.adapter.FollowRequestListAdapter;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FollowRequestActivity extends AppCompatActivity {

    private static final String TAG = FollowRequestActivity.class.getName();

    private Context mContext;

    private ListView mListView;

    private FollowRequestListAdapter mAdapter;

    private List<User> mUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_request);

        mContext = getApplicationContext();

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = (ListView) findViewById(R.id.followRequestList);

        mAdapter = new FollowRequestListAdapter(this, mUserList);
        mListView.setAdapter(mAdapter);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPreferences.edit().putInt(Constants.PENDING_FOLLOW_REQUEST_COUNTER, 0).apply();
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

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.updatePendingFollowRequestList(FollowRequestActivity.this);
    }
}
