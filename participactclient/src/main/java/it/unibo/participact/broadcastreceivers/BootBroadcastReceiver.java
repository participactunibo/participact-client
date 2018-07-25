/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.broadcastreceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import org.most.StateUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;

import it.unibo.participact.ParticipActService;
import it.unibo.participact.support.preferences.ChangeTimePreferences;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private final static Logger logger = LoggerFactory.getLogger(BootBroadcastReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {


        logger.info("Boot.");

        ChangeTimePreferences.getInstance(context).setLastCurrentMillisChecked(System.currentTimeMillis());
        ChangeTimePreferences.getInstance(context).setLastElapsedChecked(SystemClock.elapsedRealtime());

        //delete MoST state
        StateUtility.deleteState(context);
        //start ParticipActService
        Intent i = new Intent(context, ParticipActService.class);
        i.setAction(ParticipActService.START);
        context.startService(i);

        //Schedule alarm at 12 or 18
        Calendar current = new GregorianCalendar();
        current.setTimeInMillis(System.currentTimeMillis());

        if (current.get(Calendar.HOUR_OF_DAY) <= 18) {

            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.MINUTE, 10);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));

            if (current.get(Calendar.HOUR_OF_DAY) <= 12) {
                cal.set(Calendar.HOUR_OF_DAY, 12);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 18);
            }

            Intent alarmIntent = new Intent();
            alarmIntent.setAction(DailyNotificationBroadcastReceiver.DAILY_NOTIFICATION_INTENT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), DailyNotificationBroadcastReceiver.DAILY_NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            logger.info("Daily notification alarm setted at {}", cal.get(Calendar.HOUR_OF_DAY));

        }
    }

}
