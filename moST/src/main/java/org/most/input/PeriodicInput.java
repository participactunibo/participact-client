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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;

import org.most.MoSTApplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class PeriodicInput extends Input {

    public static final String BASE_INTENT_ACTION = "PeriodicInput";

    private PendingIntent _pendingIntent;
    private BroadcastReceiver _receiver;
    private IntentFilter _filter;

    protected Timer _timer;
    protected int _period;
    /**
     * Time when this periodic input was last started.
     */
    protected Date _lastStart;


	/**
	 * Creates a PeriodicInput
	 * @param context {@link MoSTApplication} context
	 * @param period Period of the input in milliseconds.
	 */
    public PeriodicInput(MoSTApplication context, int period) {
        super(context);
        _period = period;
        Calendar lastStart = GregorianCalendar.getInstance();
        lastStart.set(Calendar.YEAR, 1970);
        _lastStart = lastStart.getTime();

        Intent i = new Intent();
        i.setAction(BASE_INTENT_ACTION + getType().toInt());
        _pendingIntent = PendingIntent.getBroadcast(context, 1000 + getType().toInt(), i, 0);
        _receiver = new PeriodicInputBroadcastReceiver();
        _filter = new IntentFilter();
        _filter.addAction(BASE_INTENT_ACTION + getType().toInt());
    }

	/*
     * (non-Javadoc)
	 * 
	 * @see it.unibo.mobilesensingframework.input.Input#resume()
	 */
//	@Override
//	public boolean onActivate() {
//		checkNewState(State.ACTIVATED);
//		super.onActivate();
//		if (_timer == null) {
//			_timer = new Timer(getType() + " timer");
//		}
//
//		Calendar now = GregorianCalendar.getInstance();
//		Calendar nextScheduledTime = GregorianCalendar.getInstance();
//		nextScheduledTime.setTime(_lastStart);
//		nextScheduledTime.add(Calendar.MILLISECOND, (int) _period);
//		Date nextStart = null;
//		if (now.after(nextScheduledTime)) {
//			nextStart = new Date();
//		} else {
//			nextStart = nextScheduledTime.getTime();
//		}
//
//		_timer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				if (getState() == State.ACTIVATED) {
//					workToDo();
//				}
//			}
//		}, nextStart);
//		return true;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibo.mobilesensingframework.input.Input#pause()
	 */
//	@Override
//	public void onDeactivate() {
//		checkNewState(State.DEACTIVATED);
//		if (_timer != null) {
//			_timer.cancel();
//		}
//		_timer = null;
//		super.onDeactivate();
//	}
//
//	protected void scheduleNextStart() {
//		if (getState() == State.ACTIVATED) {
//			Calendar now = GregorianCalendar.getInstance();
//			now.add(Calendar.MILLISECOND, _period);
//			Date nextStart = now.getTime();
//			_timer.schedule(new TimerTask() {
//				@Override
//				public void run() {
//					if (getState() == State.ACTIVATED) {
//						workToDo();
//					}
//				}
//			}, nextStart);
//		}
//	}

    protected void scheduleNextStart() {
        if (getState() == State.ACTIVATED) {
            AlarmManager mgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            Calendar now = GregorianCalendar.getInstance();
            now.add(Calendar.MILLISECOND, _period);
            Date nextStart = now.getTime();
            mgr.set(AlarmManager.RTC_WAKEUP, nextStart.getTime(), _pendingIntent);
        }
    }

    @Override
    public boolean onActivate() {
        checkNewState(State.ACTIVATED);
        getContext().registerReceiver(_receiver, _filter);
        AlarmManager mgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Calendar now = GregorianCalendar.getInstance();
        Calendar nextScheduledTime = GregorianCalendar.getInstance();
        nextScheduledTime.setTime(_lastStart);
        nextScheduledTime.add(Calendar.MILLISECOND, (int) _period);
        Date nextStart = null;
        if (now.after(nextScheduledTime)) {
            nextStart = new Date();
        } else {
            nextStart = nextScheduledTime.getTime();
        }

        mgr.set(AlarmManager.RTC_WAKEUP, nextStart.getTime(), _pendingIntent);

        return super.onActivate();
    }


    @Override
    public void onDeactivate() {
        checkNewState(State.DEACTIVATED);
        AlarmManager mgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        mgr.cancel(_pendingIntent);
        getContext().unregisterReceiver(_receiver);
        super.onDeactivate();
    }

    /**
     * Method that define the actions to do every time that the timer expires.
     */
    public abstract void workToDo();

    @Override
    public boolean isWakeLockNeeded() {
        return false;
    }


    private class PeriodicInputBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            workToDo();
        }

    }


}
