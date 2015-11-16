package com.capstone.application.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted.
 */
public class BootReceiver extends BroadcastReceiver {
    public static final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BOOT_COMPLETED_ACTION)) {
            TeenAlarmReceiver alarm = new TeenAlarmReceiver(context);
            alarm.setAlarm();
        }
    }
}
