package com.capstone.application.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceiverManager extends BroadcastReceiver {
    private static final String TAG = ReceiverManager.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Action received " + intent.getAction());

        // Automatically (re)starts the alarm when the device is rebooted.
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            TeenAlarmReceiver alarm = new TeenAlarmReceiver();
            alarm.setAlarm(context);
        } else if(intent.getAction().equals("capstone.intent.action.ADD_PENDING_QUESTION")) {

        }
    }
}
