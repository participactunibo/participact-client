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

public class AsymmetricDutyCyclePolicy implements IPowerPolicy {

	private final static String TAG = AsymmetricDutyCyclePolicy.class.getSimpleName();

	private Context _context;
	private PendingIntent _pendingIntent;
	private AsymetricDutyCyclePolicyBroadcastReceiver _receiver;
	private IntentFilter _filter;

	public static final String KEY_INPUT_TYPE = "Input.Type";
	public final static String PREF_KEY_DUTYCYCLEPOLICY_HIGH_PERIOD_MS = "DutyCyclePolicyHighPeriodMs";
	public final static String PREF_KEY_DUTYCYCLEPOLICY_LOW_PERIOD_MS = "DutyCyclePolicyLowPeriodMs";
	public final static long PREF_DEFAULT_ASYMETRIC_DUTYCYCLEPOLICY_HIGH_PERIOD_MS = 10 * 1000; // 10 seconds
	public final static long PREF_DEFAULT_ASYMETRIC_DUTYCYCLEPOLICY_LOW_PERIOD_MS = 2 * 60 * 1000; // 2 minutes

	public static final String BASE_INTENT_ACTION = "AsymetricDutyCyclePolicyIntent";

	private boolean isStarted = false;
	private long _highPeriod = 0L;
	private long _lowPeriod = 0L;

	public AsymmetricDutyCyclePolicy(Context context, Input.Type input) {
		_context = context;
		Intent i = new Intent();
		i.setAction(BASE_INTENT_ACTION + input.toInt());
		i.putExtra(KEY_INPUT_TYPE, input.toInt());
		_pendingIntent = PendingIntent.getBroadcast(_context, input.toInt(), i, 0);
		_receiver = new AsymetricDutyCyclePolicyBroadcastReceiver();
		_filter = new IntentFilter();
		_filter.addAction(BASE_INTENT_ACTION + input.toInt());
	}

	public synchronized void start() {
		_context.registerReceiver(_receiver, _filter);
		AlarmManager mgr = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
		_highPeriod = _context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getLong(
				PREF_KEY_DUTYCYCLEPOLICY_HIGH_PERIOD_MS, PREF_DEFAULT_ASYMETRIC_DUTYCYCLEPOLICY_HIGH_PERIOD_MS);
		_lowPeriod = _context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getLong(
				PREF_KEY_DUTYCYCLEPOLICY_LOW_PERIOD_MS, PREF_DEFAULT_ASYMETRIC_DUTYCYCLEPOLICY_LOW_PERIOD_MS);
		if(_receiver.getState()){
			mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + _highPeriod, _pendingIntent);
		}else{
			mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + _lowPeriod, _pendingIntent);
		}
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

	public class AsymetricDutyCyclePolicyBroadcastReceiver extends BroadcastReceiver {

		private boolean _state;
		private AlarmManager mgr;
		
		public AsymetricDutyCyclePolicyBroadcastReceiver() {
			_state = true;
		}
		
		public boolean getState(){
			return _state;
		}
		
		public void setState(boolean state){
			_state = state;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(mgr == null){
				mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			}
			
			Input.Type inputType = Input.Type.fromInt(intent.getExtras().getInt(KEY_INPUT_TYPE));
			Log.i(TAG, "Timer expired for " + inputType.toString());
			_state = !_state;
			if(_state){
				mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + _highPeriod, _pendingIntent);
			}else{
				mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + _lowPeriod, _pendingIntent);
			}
			((MoSTApplication) context).getInputsArbiter().setPowerVote(inputType, _state);
		}
	}
}
