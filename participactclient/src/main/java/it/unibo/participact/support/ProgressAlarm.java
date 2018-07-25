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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class ProgressAlarm {

    private static final Logger logger = LoggerFactory.getLogger(ProgressAlarm.class);
    public static final String TAG = ProgressAlarm.class.getSimpleName();
    public static final String PROGRESS_INTENT = "it.unibo.participact.PROGRESS_INTENT";
    public static final long PERIOD = 1 * 60 * 1000; //1 MIN
    private static final int REQUEST_CODE = 568;

    private static ProgressAlarm instance;

    private Context context;
    private AtomicBoolean isStarted;
    private Intent intent;
    private PendingIntent pendingIntent;

    private ProgressAlarm(Context context) {
        this.context = context;
        isStarted = new AtomicBoolean(false);
        intent = new Intent();
        intent.setAction(PROGRESS_INTENT);
        pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static synchronized ProgressAlarm getInstance(Context context) {
        if (instance == null) {
            instance = new ProgressAlarm(context);
            logger.info("Created new ProgressAlarm instance");
        }
        return instance;
    }


    public synchronized void start() {
        if (!isStarted.get()) {
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), PERIOD, pendingIntent);
            logger.info("Progress Alarm started");
            isStarted.set(true);
        }
    }

    public synchronized void stop() {
        if (isStarted.get()) {
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.cancel(pendingIntent);
            logger.info("Progress Alarm stopped");
            isStarted.set(false);
        }
    }

    public synchronized boolean isStarted() {
        return isStarted.get();
    }

}
