/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most;

import org.most.input.Input;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * This class implements a simple duty cycle policy that periodically switched
 * on and off a sensor.
 */
public class DutyCyclePolicy implements IPowerPolicy {

	private final static String TAG = DutyCyclePolicy.class.getSimpleName();

	private Context _context;
	private PendingIntent _pendingIntent;
	private DutyCyclePolicyBroadcastReceiver _receiver;
	private IntentFilter _filter;

	public static final String KEY_INPUT_TYPE = "Input.Type";
	public final static String PREF_KEY_DUTYCYCLEPOLICY_PERIOD_MS = "DutyCyclePolicyPeriodMs";
	public final static long PREF_DEFAULT_DUTYCYCLEPOLICY_PERIOD_MS = 20 * 1000; // 20
																					// seconds

	public static final String BASE_INTENT_ACTION = "DutyCyclePolicyIntent";

	private boolean isStarted = false;

	public DutyCyclePolicy(Context context, Input.Type input) {
		_context = context;
		Intent i = new Intent();
		i.setAction(BASE_INTENT_ACTION + input.toInt());
		i.putExtra(KEY_INPUT_TYPE, input.toInt());
		_pendingIntent = PendingIntent.getBroadcast(_context, input.toInt(), i, 0);
		_receiver = new DutyCyclePolicyBroadcastReceiver();
		_filter = new IntentFilter();
		_filter.addAction(BASE_INTENT_ACTION + input.toInt());
	}

	public synchronized void start() {
		_context.registerReceiver(_receiver, _filter);
		AlarmManager mgr = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
		long period = _context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getLong(
				PREF_KEY_DUTYCYCLEPOLICY_PERIOD_MS, PREF_DEFAULT_DUTYCYCLEPOLICY_PERIOD_MS);
		mgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), period, _pendingIntent);
		Log.i(TAG, "Power policy started");
		isStarted = true;
	}

	public synchronized void stop() {
		if (isStarted) {
			AlarmManager mgr = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
			mgr.cancel(_pendingIntent);
			_context.unregisterReceiver(_receiver);
			Log.i(TAG, "Power policy stopped");
			isStarted = false;
		}
	}

	public class DutyCyclePolicyBroadcastReceiver extends BroadcastReceiver {

		private boolean _state;

		public DutyCyclePolicyBroadcastReceiver() {
			_state = true;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Input.Type inputType = Input.Type.fromInt(intent.getExtras().getInt(KEY_INPUT_TYPE));
			Log.i(TAG, "Timer expired for " + inputType.toString());
			_state = !_state;
			((MoSTApplication) context).getInputsArbiter().setPowerVote(inputType, _state);
		}
	}
}
