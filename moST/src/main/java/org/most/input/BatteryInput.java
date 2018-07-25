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

import org.most.DataBundle;
import org.most.MoSTApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

public class BatteryInput extends PeriodicInput {

	/** The Constant DEBUG. */
    @SuppressWarnings("unused")
    private final static boolean DEBUG = true;

	/** The Constant TAG. */
    @SuppressWarnings("unused")
    private final static String TAG = BatteryInput.class.getSimpleName();

    /**
     * {@link SharedPreferences} key to set the battery check period.
     */
    public final static String PREF_KEY_BATTERY_PERIOD = "BatteryInputPediodMs";

    /**
     * Default battery monitoring interval in milliseconds.
     * Currently set to {@value #PREF_DEFAULT_BATTERY_PERIOD}.
     */
    public final static int PREF_DEFAULT_BATTERY_PERIOD = 15 * 60 * 1000;

    private final static String ACTION_BATTERY_CHANGED_INTENT = Intent.ACTION_BATTERY_CHANGED;

    public static final String KEY_BATTERY_LEVEL = "BatteryInput.level";
    public static final String KEY_BATTERY_SCALE = "BatteryInput.scale";
    public static final String KEY_BATTERY_TEMPERATURE = "BatteryInput.temperature";
    public static final String KEY_BATTERY_VOLTAGE = "BatteryInput.voltage";
    public static final String KEY_BATTERY_PLUGGED = "BatteryInput.plugged";
    public static final String KEY_BATTERY_STATUS = "BatteryInput.status";
    public static final String KEY_BATTERY_HEALTH = "BatteryInput.health";

    private IntentFilter _filter;
    private BatteryBrodcastReceiver _batteryBroadcastReceiver;

    public BatteryInput(MoSTApplication context) {
        super(context, context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getInt(
                PREF_KEY_BATTERY_PERIOD, PREF_DEFAULT_BATTERY_PERIOD));
    }

    @Override
    public void onInit() {
        checkNewState(Input.State.INITED);
        _filter = new IntentFilter();
        _filter.addAction(ACTION_BATTERY_CHANGED_INTENT);
        _batteryBroadcastReceiver = new BatteryBrodcastReceiver();
        super.onInit();
    }

    @Override
    public boolean onActivate() {
        checkNewState(Input.State.ACTIVATED);
        return super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
    }

    @Override
    public void onFinalize() {
        checkNewState(Input.State.FINALIZED);
        _filter = null;
        _batteryBroadcastReceiver = null;
        super.onFinalize();
    }

    @Override
    public Type getType() {
        return Input.Type.BATTERY;
    }

    class BatteryBrodcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);

            DataBundle b = _bundlePool.borrowBundle();
            b.putInt(Input.KEY_TYPE, Input.Type.BATTERY.toInt());
            b.putLong(Input.KEY_TIMESTAMP, System.currentTimeMillis());
            b.putInt(KEY_BATTERY_LEVEL, level);
            b.putInt(KEY_BATTERY_SCALE, scale);
            b.putInt(KEY_BATTERY_TEMPERATURE, temperature);
            b.putInt(KEY_BATTERY_VOLTAGE, voltage);
            b.putInt(KEY_BATTERY_PLUGGED, plugged);
            b.putInt(KEY_BATTERY_STATUS, status);
            b.putInt(KEY_BATTERY_HEALTH, health);
            post(b);

            getContext().unregisterReceiver(_batteryBroadcastReceiver);
        }
    }

    @Override
    public boolean isWakeLockNeeded() {
        return false;
    }

    @Override
    public void workToDo() {
        getContext().registerReceiver(_batteryBroadcastReceiver, _filter);
        scheduleNextStart();
    }
}
