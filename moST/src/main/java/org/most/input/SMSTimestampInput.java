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
import android.util.Log;

public class SMSTimestampInput extends Input {

    @SuppressWarnings("unused")
    private final static boolean DEBUG = true;

    private final static String TAG = SMSTimestampInput.class.getSimpleName();

    private final static String SMS_RECEIVED_INTENT = "android.provider.Telephony.SMS_RECEIVED";
    private final static String SMS_SENT_INTENT = "android.provider.Telephony.SMS_SENT";

    public final static String KEY_SMS_TYPE = "SMSTimestampInputType";
    public final static int SMS_TYPE_RECEIVED = 0;
    public final static int SMS_TYPE_SENT = 1;

    private IntentFilter _filter;
    private SMSBrodcastReceiver _smsBroadcastReceiver;

    public SMSTimestampInput(MoSTApplication context) {
        super(context);
    }

    @Override
    public void onInit() {
        checkNewState(Input.State.INITED);
        _filter = new IntentFilter();
        _filter.addAction(SMS_RECEIVED_INTENT);
        _filter.addAction(SMS_SENT_INTENT);
        _filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        _smsBroadcastReceiver = new SMSBrodcastReceiver();
        super.onInit();
    }

    @Override
    public boolean onActivate() {
        checkNewState(Input.State.ACTIVATED);
        getContext().registerReceiver(_smsBroadcastReceiver, _filter);
        return super.onActivate();
    }

    @Override
    public void onDeactivate() {
        checkNewState(Input.State.DEACTIVATED);
        getContext().unregisterReceiver(_smsBroadcastReceiver);
        super.onDeactivate();
    }

    @Override
    public void onFinalize() {
        checkNewState(Input.State.FINALIZED);
        _filter = null;
        _smsBroadcastReceiver = null;
        super.onFinalize();
    }

    @Override
    public Type getType() {
        return Input.Type.SMSTIMESTAMP;
    }


    public class SMSBrodcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Sent or Received sms");
            DataBundle b = _bundlePool.borrowBundle();
            if (intent.getAction().equals(SMS_RECEIVED_INTENT))
                b.putInt(KEY_SMS_TYPE, SMS_TYPE_RECEIVED);
            else
                b.putInt(KEY_SMS_TYPE, SMS_TYPE_SENT);
            b.putLong(Input.KEY_TIMESTAMP, System.currentTimeMillis());
            b.putInt(Input.KEY_TYPE, Input.Type.SMSTIMESTAMP.toInt());
            post(b);
        }

    }

    @Override
    public boolean isWakeLockNeeded() {
        return false;
    }
}
