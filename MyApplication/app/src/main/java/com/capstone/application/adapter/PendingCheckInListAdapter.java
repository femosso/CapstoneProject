package com.capstone.application.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.capstone.application.R;
import com.capstone.application.activity.CheckInWizardActivity;
import com.capstone.application.alarm.TeenAlarmReceiver;
import com.capstone.application.fragment.PendingCheckInsFragment;
import com.capstone.application.model.PendingCheckIn;
import com.capstone.application.model.Question;
import com.capstone.application.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PendingCheckInListAdapter extends BaseAdapter {
    private static final String TAG = PendingCheckInListAdapter.class.getName();

    private Activity mActivity;
    private PendingCheckInsFragment mFragment;
    private LayoutInflater inflater;
    private List<PendingCheckIn> mPendingCheckInItems;

    public PendingCheckInListAdapter(PendingCheckInsFragment fragment, List<PendingCheckIn> pendingCheckIns) {
        mFragment = fragment;
        mActivity = mFragment.getActivity();
        setPendingCheckInList(pendingCheckIns);
    }

    public void setPendingCheckInList(List<PendingCheckIn> pendingCheckIns) {
        mPendingCheckInItems = (pendingCheckIns == null ? new ArrayList<PendingCheckIn>() : pendingCheckIns);
    }

    @Override
    public int getCount() {
        return mPendingCheckInItems.size();
    }

    @Override
    public Object getItem(int location) {
        return mPendingCheckInItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        PendingCheckIn pendingCheckIn = mPendingCheckInItems.get(position);
        final long pendingCheckInId = pendingCheckIn.getId();

        if (convertView == null) {
            if (inflater == null) {
                inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.row_pending_check_in, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.date = (TextView) convertView.findViewById(R.id.txtDate);
            viewHolder.delayTime = (TextView) convertView.findViewById(R.id.txtDelayTime);

            viewHolder.answerNow = (Button) convertView.findViewById(R.id.btnAnswerNow);
            viewHolder.answerNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new RetrieveNewCheckIn(pendingCheckInId).execute();
                }
            });

            // store the holder with the view.
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        viewHolder.date.setText(new SimpleDateFormat(Constants.DATE_TIME_FORMAT).format(pendingCheckIn.getDate()));

        long delay = System.currentTimeMillis() - pendingCheckIn.getDate();

        String value = mActivity.getString(R.string.last_pending_check_in_time,
                TimeUnit.MILLISECONDS.toMinutes(delay),
                TimeUnit.MILLISECONDS.toSeconds(delay) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(delay)));

        viewHolder.delayTime.setText(value);

        return convertView;
    }

    public static class ViewHolderItem {
        TextView date;
        TextView delayTime;

        Button answerNow;
    }

    private class RetrieveNewCheckIn extends AsyncTask<Void, Void, List<Question>> {

        private long mPendingCheckInId;
        private ProgressDialog mDialog;

        public RetrieveNewCheckIn(long pendingCheckInId) {
            mDialog = new ProgressDialog(mActivity);
            mPendingCheckInId = pendingCheckInId;
        }

        @Override
        protected void onPreExecute() {
            mDialog.setMessage(mActivity.getString(R.string.progress_dialog_loading));
            mDialog.show();
        }

        @Override
        protected List<Question> doInBackground(Void... params) {
            return TeenAlarmReceiver.retrieveNewCheckInFromServer(mActivity);
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            if (questions != null) {
                Intent intent = new Intent(mActivity, CheckInWizardActivity.class);
                intent.putParcelableArrayListExtra("questions", new ArrayList<>(questions));
                intent.putExtra("pendingCheckInId", mPendingCheckInId);

                mFragment.startActivityForResult(intent, PendingCheckInsFragment.WIZARD_REQUEST_CODE);
            }
        }
    }

}
