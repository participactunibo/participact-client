/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.input;

import java.util.List;

import org.most.DataBundle;
import org.most.MoSTApplication;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * This Input periodically reports the full name of the application and activity
 * currently on the foreground. When posting on a bus, it sends
 * {@link DataBundle} containing:
 * <ul>
 * <li>{@link Input#KEY_TYPE} (int): type of the sensor, convert it to a
 * {@link Input#Type} using {@link Input#Type.fromInt()}. Set to
 * {@link Input.Type#APPONSCREEN}.</li>
 * <li> {@link Input#KEY_TIMESTAMP} (long): timestamp in milliseconds.</li>
 * <li> {@link #KEY_APPONSCREEN} (String): package name of the application
 * currently on the foreground on the screen.</li>
 * <li> {@link #KEY_APPONSCREENACTIVITY} (String) full name (including package)
 * of the activity currently on the foreground.</li>
 * </ul>
 * 
 * This input supports the app monitoring rate to be configured. The
 * {@link SharedPreferences} name to use is {@link MoSTApplication#PREF_INPUT},
 * the key value to use is {@link #PREF_KEY_APPMONITOR_PERIOD} . Its default
 * value is {@link #PREF_DEFAULT_APPMONITOR_PERIOD} (
 * {@value #PREF_DEFAULT_APPMONITOR_PERIOD} milliseconds). For example:
 * 
 * <pre>
 * {
 * 	Editor editor = context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).edit();
 * 	editor.putInt(PREF_KEY_APPMONITOR_PERIOD, 5000);
 * 	editor.apply();
 * }
 * </pre>
 */
public class AppOnScreenInput extends PeriodicInput {

	/** The Constant TAG. */
    @SuppressWarnings("unused")
    private final static String TAG = AppOnScreenInput.class.getSimpleName();

    /**
     * Key to access the package name of the app currently on the foreground.
     */
    public final static String KEY_APPONSCREEN = "AppOnScreenInput.AppOnScreenName";
    /**
     * Key to access the full name (including package) of the activity currently
     * on the foreground.
     */
    public final static String KEY_APPONSCREENACTIVITY = "AppOnScreenInput.AppOnScreenActivity";

    /**
     * {@link SharedPreferences} key to set the foreground application
     * monitoring period.
     */
    public final static String PREF_KEY_APPMONITOR_PERIOD = "AppOnScreenInputMonitorPeriod";

    /**
     * Default foreground application monitoring interval in milliseconds.
     * Currently set to {@value #PREF_DEFAULT_APPMONITOR_PERIOD}.
     */
    public final static int PREF_DEFAULT_APPMONITOR_PERIOD = 2000;

    /**
     * @param context
     * @param period
     */
    public AppOnScreenInput(MoSTApplication context) {
        super(context, getMonitorPeriod(context));
    }

    @Override
    public void workToDo() {
        RunningTaskInfo fgApp = getForegroundTask();
        ComponentName fgAct = null;
        if (null != fgApp) {
            fgAct = fgApp.topActivity;
        }

		/*
         * Log the foreground application and activity if: a) we successfully
		 * got the foreground application b) we successfully got the foreground
		 * activity
		 */
        if (fgApp != null && fgAct != null) {
            DataBundle b = _bundlePool.borrowBundle();
            b.putString(KEY_APPONSCREEN, fgApp.baseActivity.getPackageName());
            b.putString(KEY_APPONSCREENACTIVITY, fgAct.getClassName());
            b.putLong(Input.KEY_TIMESTAMP, System.currentTimeMillis());
            b.putInt(Input.KEY_TYPE, Input.Type.APPONSCREEN.toInt());
            post(b);
        }
        scheduleNextStart();
    }

    /**
     * Gets the current foreground task or <code>null</code> on exception.
     *
     * @return The current foreground task.
     */
    private RunningTaskInfo getForegroundTask() {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo result = null;
        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
        if (runningTasks.size() > 0) {
            result = runningTasks.get(0);
        }
        return result;
    }

    @Override
    public Type getType() {
        return Input.Type.APPONSCREEN;
    }

    public static int getMonitorPeriod(Context context) {
        SharedPreferences sp = context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE);
        return sp.getInt(PREF_KEY_APPMONITOR_PERIOD, PREF_DEFAULT_APPMONITOR_PERIOD);
    }
}
