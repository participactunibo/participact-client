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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.octo.android.robospice.networkstate.DefaultNetworkStateChecker;
import com.octo.android.robospice.networkstate.NetworkStateChecker;

import it.unibo.participact.support.DataUploader;

public class WifiChangeStateBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WifiChangeStateBroadcastReceiver.class.getSimpleName();
    NetworkStateChecker networkChecker;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (networkChecker == null) {
            networkChecker = new DefaultNetworkStateChecker();
        }
        SupplicantState supState;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        supState = wifiInfo.getSupplicantState();
        Log.i(TAG, "supplicant state: " + supState);
        if (supState.equals(SupplicantState.COMPLETED)) {
            //wifi enabled and connected
            Log.i(TAG, "wifi enabled and connected");
            if (networkChecker.isNetworkAvailable(context)) {
                DataUploader.getInstance(context).uploadOverWifi();
            }
        } else {

        }
    }

//	
//	@Override
//	public void onReceive(Context context, Intent intent) {
//	 final String action = intent.getAction();
//	    if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
//	        if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
//	            //do stuff
//	        	Log.i(TAG, "wifi enabled and connected");
//	        	DataUploader.getInstance(context).uploadOverWifi();
//	        } else {
//	            // wifi connection was lost
//	        }
//	    }
//	}

}
