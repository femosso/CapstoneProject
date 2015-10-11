package com.capstone.application.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.capstone.application.R;
import com.capstone.application.adapter.PendingFollowRequestListAdapter;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PendingFollowRequestActivity extends AppCompatActivity {

    private static final String TAG = PendingFollowRequestActivity.class.getName();

    private Context mContext;

    private ListView mListView;

    private PendingFollowRequestListAdapter mAdapter;

    private List<User> mUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_follow_request);

        mContext = getApplicationContext();

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mListView = (ListView) findViewById(R.id.pendingFollowRequestList);

        mAdapter = new PendingFollowRequestListAdapter(this, mUserList);
        mListView.setAdapter(mAdapter);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPreferences.edit().putInt(Constants.NOTIFICATION_COUNTER, 0).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.updatePendingFollowRequestList(PendingFollowRequestActivity.this);
    }
}
