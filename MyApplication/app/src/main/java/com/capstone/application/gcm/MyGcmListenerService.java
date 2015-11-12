package com.capstone.application.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.capstone.application.R;
import com.capstone.application.activity.CheckInDetailsActivity;
import com.capstone.application.activity.FollowRequestActivity;
import com.capstone.application.activity.MainActivity;
import com.capstone.application.fragment.HomeFragment;
import com.capstone.application.model.CheckIn;
import com.capstone.application.utils.Constants;
import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = MyGcmListenerService.class.getName();

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "onMessageReceived " + data);

        String message = data.getString("message");
        String type = data.getString("type");
        String checkInString = data.getString("checkInId");

        // checkInId will come only in some GCM messages, if it does not come its value will be -1
        long checkInId = checkInString != null ? Long.valueOf(checkInString) : -1;

        sendNotification(message, type, checkInId);
    }

    /**
     * Create and show a notification containing the data received in the GCM message.
     */
    private void sendNotification(String message, String type, long checkInId) {
        PendingIntent pendingIntent = null;
        String title = "";

        if (Constants.GCM_FOLLOW_REQUEST_TYPE.equals(type)) {
            title = "Follow Request";

            Intent intent = new Intent(this, FollowRequestActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            // update notification counter as new follow request message has arrived
            updateFollowRequestCounter();
        } else if (Constants.GCM_NEW_CHECK_IN_TYPE.equals(type)) {
            title = "New Check-In from a Teen";

            CheckIn checkIn = HomeFragment.retrieveCheckInFromServer(getApplicationContext(), checkInId);

            Intent intent = new Intent(this, CheckInDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("checkIn", checkIn);

            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        } else if (Constants.GCM_ADMIN_TYPE.equals(type)) {
            title = "Got It";

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        // if title is empty, it means we received a message that we don't know how to handle, so ignore it
        if (!title.isEmpty()) {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    public void updateFollowRequestCounter() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int counter = sharedPreferences.getInt(Constants.PENDING_FOLLOW_REQUEST_COUNTER, -1);
        sharedPreferences.edit().putInt(Constants.PENDING_FOLLOW_REQUEST_COUNTER, ++counter).apply();

        // notify ui to update follow request counter view
        Intent intent = new Intent(Constants.NEW_FOLLOW_REQUEST_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}