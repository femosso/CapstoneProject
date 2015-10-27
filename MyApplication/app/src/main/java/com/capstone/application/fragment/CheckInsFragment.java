package com.capstone.application.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.capstone.application.R;
import com.capstone.application.activity.CheckInWizardActivity;
import com.capstone.application.adapter.PendingCheckInListAdapter;
import com.capstone.application.alarm.TeenAlarmReceiver;
import com.capstone.application.database.PendingCheckInProvider;
import com.capstone.application.database.PendingCheckInTable;
import com.capstone.application.model.PendingCheckIn;
import com.capstone.application.model.Question;

import java.util.ArrayList;
import java.util.List;

public class CheckInsFragment extends Fragment {
    private static final String TAG = CheckInsFragment.class.getName();

    public static int WIZARD_REQUEST_CODE = 1;

    private Context mContext;

    private ListView mListView;

    private PendingCheckInListAdapter mAdapter;

    private List<PendingCheckIn> mPendingCheckInItems;

    public CheckInsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();

        mPendingCheckInItems = retrievePendingCheckInsFromProvider();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pending_check_ins, container, false);

        mListView = (ListView) rootView.findViewById(R.id.list);
        mListView.setEmptyView(rootView.findViewById(R.id.empty));

        Button checkInNowButton = (Button) rootView.findViewById(R.id.checkInNowButton);
        checkInNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RetrieveNewCheckIn().execute();
            }
        });

        mAdapter = new PendingCheckInListAdapter(this, mPendingCheckInItems);
        mListView.setAdapter(mAdapter);

        // Inflate the layout for this fragment
        return rootView;
    }

    private List<PendingCheckIn> retrievePendingCheckInsFromProvider() {
        String[] projection = {PendingCheckInTable.COLUMN_ID,
                PendingCheckInTable.COLUMN_DATE};

        Cursor cursor = mContext.getContentResolver().query(PendingCheckInProvider.CONTENT_URI,
                projection, null, null, null);

        long id;
        long date;

        List<PendingCheckIn> pendingCheckInItems = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(PendingCheckInTable.COLUMN_ID));
                date = cursor.getLong(cursor.getColumnIndexOrThrow(PendingCheckInTable.COLUMN_DATE));

                pendingCheckInItems.add(new PendingCheckIn(id, date));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return pendingCheckInItems;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WIZARD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                long pendingCheckInId = data.getLongExtra("pendingCheckInId", -1);

                if (pendingCheckInId != -1) {
                    String selectionClause = PendingCheckInTable.COLUMN_ID + " LIKE ?";
                    String[] selectionArgs = {String.valueOf(pendingCheckInId)};

                    Log.d(TAG, "removing pendingCheckInId " + pendingCheckInId);

                    mContext.getContentResolver().delete(PendingCheckInProvider.CONTENT_URI,
                            selectionClause, selectionArgs);

                    mAdapter.setPendingCheckInList(retrievePendingCheckInsFromProvider());
                    mAdapter.notifyDataSetChanged();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private class RetrieveNewCheckIn extends AsyncTask<Void, Void, List<Question>> {

        private ProgressDialog mDialog;

        public RetrieveNewCheckIn() {
            mDialog = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {
            mDialog.setMessage("Doing something, please wait.");
            mDialog.show();
        }

        @Override
        protected List<Question> doInBackground(Void... params) {
            return TeenAlarmReceiver.retrieveNewCheckInFromServer();
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            if (questions != null) {
                Intent intent = new Intent(mContext, CheckInWizardActivity.class);
                intent.putParcelableArrayListExtra("questions", new ArrayList<>(questions));

                startActivityForResult(intent, CheckInsFragment.WIZARD_REQUEST_CODE);
            }
        }
    }
}