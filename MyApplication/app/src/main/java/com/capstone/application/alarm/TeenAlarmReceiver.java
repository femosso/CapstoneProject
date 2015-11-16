package com.capstone.application.alarm;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.capstone.application.R;
import com.capstone.application.activity.CheckInWizardActivity;
import com.capstone.application.database.PendingCheckInProvider;
import com.capstone.application.database.PendingCheckInTable;
import com.capstone.application.model.Question;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * When the check in alarm fires, this BroadcastReceiver receives the REQUEST_NEW_CHECK_IN_ACTION
 * broadcast Intent and then ask the app's server for a new check in
 */
public class TeenAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = TeenAlarmReceiver.class.getName();

    private AlarmManager mAlarmMgr;

    private PendingIntent mAlarmPendingIntent;

    private Intent mAlarmIntent;

    private Context mContext;

    public TeenAlarmReceiver() {
        // Required empty public constructor
    }

    public TeenAlarmReceiver(Context context) {
        mContext = context;

        mAlarmIntent = new Intent(mContext, TeenAlarmReceiver.class);
        mAlarmIntent.setAction(Constants.REQUEST_NEW_CHECK_IN_ACTION);

        mAlarmPendingIntent = PendingIntent.getBroadcast(mContext, 0, mAlarmIntent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Action received " + intent.getAction());

        if (intent.getAction().equals(Constants.REQUEST_NEW_CHECK_IN_ACTION)) {
            new RetrieveNewCheckIn(context).execute();
        } else if (intent.getAction().equals(Constants.ADD_PENDING_CHECK_IN_ACTION)) {
            // dismiss the question notification
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(Constants.QUESTION_NOTIFICATION_ID);

            // close notification bar
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);

            // write on content provider the time the pending question has come
            ContentValues values = new ContentValues();
            values.put(PendingCheckInTable.COLUMN_DATE, System.currentTimeMillis());

            context.getContentResolver().insert(PendingCheckInProvider.CONTENT_URI, values);

            // notify the ui to update pending check in counter in navigation drawer
            Intent notifyUi = new Intent(Constants.NOTIFY_PENDING_CHECK_IN_ACTION);
            LocalBroadcastManager.getInstance(context).sendBroadcast(notifyUi);
        }
    }

    private void sendCheckInNotification(Context context, String message, List<Question> questions) {
        Intent intentQuestionWizard = new Intent(context, CheckInWizardActivity.class);
        intentQuestionWizard.putParcelableArrayListExtra("questions", new ArrayList<>(questions));
        intentQuestionWizard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent answerNowPendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                intentQuestionWizard, PendingIntent.FLAG_ONE_SHOT);

        Intent intent = new Intent(context, TeenAlarmReceiver.class);
        intent.setAction(Constants.ADD_PENDING_CHECK_IN_ACTION);

        PendingIntent laterPendingIntent = PendingIntent.getBroadcast(context, 0 /* Request code */, intent, 0);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notify)
                .addAction(R.drawable.ic_check_in, context.getString(R.string.answer_now), answerNowPendingIntent)
                .addAction(R.drawable.ic_later, context.getString(R.string.answer_later), laterPendingIntent)
                .setContentTitle(context.getString(R.string.notification_new_check_in_title))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(answerNowPendingIntent)
                .setDeleteIntent(laterPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(Constants.QUESTION_NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Sets a repeating alarm that runs according to the teen configuration. When the
     * alarm fires, the app broadcasts a REQUEST_NEW_CHECK_IN_ACTION Intent to query
     * server for a new check in.
     */
    public void setAlarm() {
        if (isAlarmUp()) {
            Log.d(TAG, "Alarm is already active, no need to create it again");
            return;
        }

        mAlarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        // reads the alarm frequency from shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String reminderFrequency = sharedPreferences.getString(Constants.REMINDER_FREQUENCY_KEY, null);

        if (reminderFrequency != null) {
            Log.d(TAG, "Setting alarm for " + reminderFrequency + " times/day");

            mAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    AlarmManager.INTERVAL_DAY / Integer.valueOf(reminderFrequency), mAlarmPendingIntent);
        }

        // Enable {@code BootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(mContext, BootReceiver.class);
        PackageManager pm = mContext.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private boolean isAlarmUp() {
        // FIXME - For presentation purposes, I have commented this line to trigger alarm every time MainActivity is created
        /*return PendingIntent.getBroadcast(mContext, 0,
                mAlarmIntent, PendingIntent.FLAG_NO_CREATE) != null;*/
        return false;
    }

    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (mAlarmMgr != null) {
            mAlarmMgr.cancel(mAlarmPendingIntent);
        }

        // Disable {@code BootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public class RetrieveNewCheckIn extends AsyncTask<Void, Void, List<Question>> {
        private Context mContext;

        public RetrieveNewCheckIn(Context context) {
            mContext = context;
        }

        @Override
        protected List<Question> doInBackground(Void... params) {
            return retrieveNewCheckInFromServer(mContext);
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            if (questions != null) {
                sendCheckInNotification(mContext,
                        mContext.getString(R.string.notification_new_check_in_content), questions);
            }
        }
    }

    public static List<Question> retrieveNewCheckInFromServer(Context context) {
        Log.d(TAG, "Contacting server to retrieve new check in for this user");

        List<Question> questions = null;
        try {
            // The URL for making the GET request
            final String url = Constants.getServerUrl(context) +
                    RestUriConstants.QUESTION_CONTROLLER + File.separator + RestUriConstants.LIST;

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Add the String message converter
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            // Make the HTTP GET request, marshaling the response to Question[]
            Question[] result = restTemplate.getForObject(url, Question[].class);

            if (result != null) {
                questions = Arrays.asList(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return questions;
    }
}
