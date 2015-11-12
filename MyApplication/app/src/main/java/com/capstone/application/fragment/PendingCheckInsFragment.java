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

public class PendingCheckInsFragment extends Fragment {
    private static final String TAG = PendingCheckInsFragment.class.getName();

    public static int WIZARD_REQUEST_CODE = 1;

    private Context mContext;

    private PendingCheckInListAdapter mAdapter;

    private List<PendingCheckIn> mPendingCheckInItems;

    public PendingCheckInsFragment() {
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

        ListView listView = (ListView) rootView.findViewById(R.id.listPendingCheckIns);
        listView.setEmptyView(rootView.findViewById(R.id.emptyLayout));

        // teen has the option to do a check in now, so retrieve it from server when button is clicked
        Button checkInNowButton = (Button) rootView.findViewById(R.id.btnCheckInNow);
        checkInNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RetrieveNewCheckIn().execute();
            }
        });

        mAdapter = new PendingCheckInListAdapter(this, mPendingCheckInItems);
        listView.setAdapter(mAdapter);

        // Inflate the layout for this fragment
        return rootView;
    }

    private List<PendingCheckIn> retrievePendingCheckInsFromProvider() {
        String[] projection = {PendingCheckInTable.COLUMN_ID, PendingCheckInTable.COLUMN_DATE};

        Cursor cursor = mContext.getContentResolver().query(PendingCheckInProvider.CONTENT_URI,
                projection, null, null, null);

        long id, date;

        List<PendingCheckIn> pendingCheckInItems = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(PendingCheckInTable.COLUMN_ID));
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(PendingCheckInTable.COLUMN_DATE));

                    pendingCheckInItems.add(new PendingCheckIn(id, date));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

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

                    // remove the pending check in from the list as the teen has just done it
                    mContext.getContentResolver().delete(PendingCheckInProvider.CONTENT_URI,
                            selectionClause, selectionArgs);

                    // refresh list with new values
                    mAdapter.setPendingCheckInList(retrievePendingCheckInsFromProvider());
                    mAdapter.notifyDataSetChanged();
                }
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
            mDialog.setMessage(getString(R.string.progress_dialog_loading));
            mDialog.show();
        }

        @Override
        protected List<Question> doInBackground(Void... params) {
            return TeenAlarmReceiver.retrieveNewCheckInFromServer(mContext);
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            // shows the wizard containing all the questions for this check in
            if (questions != null) {
                Intent intent = new Intent(mContext, CheckInWizardActivity.class);
                intent.putParcelableArrayListExtra("questions", new ArrayList<>(questions));

                startActivityForResult(intent, PendingCheckInsFragment.WIZARD_REQUEST_CODE);
            }
        }
    }
}