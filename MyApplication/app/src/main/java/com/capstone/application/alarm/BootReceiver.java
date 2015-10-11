package com.capstone.application.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted.
 */
public class BootReceiver extends BroadcastReceiver {
    TeenAlarmReceiver mAlarm = new TeenAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            mAlarm.setAlarm(context);
        }
    }
}
