/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.support;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import it.unibo.participact.ParticipActApplication;
import it.unibo.participact.broadcastreceivers.AlarmBroadcastReceiver;

public class AlarmStateUtility {

    public final static String CONSOLE_SUSPEND_INTENT = "it.unibo.participact.CONSOLE_SUSPEND_INTENT";
    public final static String KEY_TASK = "CONSOLE_SUSPEND_INTENT.KEY_TASK";

    private final static long SUSPEND_DURATION = 1000 * 60 * 60 * 8;

    public static synchronized void addAlarm(Context context, long taskId) {
        ParticipActApplication application = (ParticipActApplication) context.getApplicationContext();

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent();
        intent.setAction(CONSOLE_SUSPEND_INTENT + taskId);
        intent.putExtra(KEY_TASK, taskId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SUSPEND_DURATION, pendingIntent);

        if (!application.getAlarmBR().containsKey(taskId)) {
            AlarmBroadcastReceiver alarm = new AlarmBroadcastReceiver(taskId);
            application.getAlarmBR().put(taskId, alarm);
            IntentFilter filter = new IntentFilter(CONSOLE_SUSPEND_INTENT + taskId);
            context.registerReceiver(alarm, filter);
        }

    }

    public static synchronized void removeAlarm(Context context, long taskId) {
        ParticipActApplication application = (ParticipActApplication) context.getApplicationContext();

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(CONSOLE_SUSPEND_INTENT + taskId);
        intent.putExtra(KEY_TASK, taskId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.cancel(pendingIntent);

        if (application.getAlarmBR().containsKey(taskId)) {
            AlarmBroadcastReceiver alarm = application.getAlarmBR().get(taskId);
            context.unregisterReceiver(alarm);
            application.getAlarmBR().remove(taskId);
        }

    }

}
