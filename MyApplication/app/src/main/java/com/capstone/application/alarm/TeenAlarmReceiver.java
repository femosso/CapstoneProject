package com.capstone.application.alarm;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.activity.DialogActivity;
import com.capstone.application.model.Question;
import com.capstone.application.utils.Constants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;

/**
 * When the mAlarm fires, this BroadcastReceiver receives the broadcast Intent
 * and then starts the IntentService {@code SampleSchedulingService} to do some work.
 */
public class TeenAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = TeenAlarmReceiver.class.getName();

    private Context mContext;

    private AlarmManager mAlarmMgr;
    private PendingIntent mAlarmIntent;

    public TeenAlarmReceiver() {
    }

    public TeenAlarmReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Felipe", "onReceive");

        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        new RetrieveNewQuestion().execute();
    }

    private void sendNotification(String message, Question question) {
        Intent intent = new Intent(mContext, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("question", question);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_home)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Sets a repeating mAlarm that runs according to the teen's configuration. When the
     * mAlarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     */
    public void setAlarm(Context context) {
        mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TeenAlarmReceiver.class);
        mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set the alarm's trigger time to 8:30 a.m.
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 3);

        Log.d("Felipe", "setting alarm");

        // Set the mAlarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        /*mAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mAlarmIntent);*/

        mAlarmMgr.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 5, mAlarmIntent);

        // Enable {@code BootReceiver} to automatically restart the mAlarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {
        // If the mAlarm has been set, cancel it.
        if (mAlarmMgr != null) {
            mAlarmMgr.cancel(mAlarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // mAlarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private class RetrieveNewQuestion extends AsyncTask<Void, Void, Question> {

        @Override
        protected Question doInBackground(Void... params) {
            Log.d(TAG, "Contacting server to retrieve new question for this user");

            Question result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "question/";

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to Teen object
                result = restTemplate.getForObject(url, Question.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Question question) {
            if (question != null) {
                sendNotification("new question", question);
            }
        }
    }
}
