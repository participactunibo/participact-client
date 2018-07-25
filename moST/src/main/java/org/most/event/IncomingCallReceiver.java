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
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncomingCallReceiver extends EventReceiver {

	private final String TAG = IncomingCallReceiver.class.getSimpleName();
	
	
	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PHONE_STATE");
		return filter;
	}

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (null == bundle)
            return;
        String state = bundle.getString(TelephonyManager.EXTRA_STATE);
        Log.i(TAG, "State: " + state);
        MoSTApplication application = (MoSTApplication) context.getApplicationContext();
        if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
            //Telephone Ringing -> vote to turn off microphone
            application.getInputsArbiter().setEventVote(Input.Type.AUDIO, false);
        }
        if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
            //IncomingCall finished -> vote to turn on microphone
            application.getInputsArbiter().setEventVote(Input.Type.AUDIO, true);
        }
    }

}
