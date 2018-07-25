/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.event;

import org.most.MoSTApplication;
import org.most.input.Input;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScreenOnReceiver extends EventReceiver {

	private final static boolean DEBUG = false;
	private final String TAG = ScreenOnReceiver.class.getSimpleName();

	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		return filter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (DEBUG) {
			Log.d(TAG, "Screen unlocking detected");
		}
		// Screen On -> vote to turn on apponscreen input
		((MoSTApplication) context.getApplicationContext()).getInputsArbiter().setEventVote(Input.Type.APPONSCREEN,
				true);
	}
}