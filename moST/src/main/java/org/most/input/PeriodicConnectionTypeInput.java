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

import java.util.Locale;

import org.most.DataBundle;
import org.most.MoSTApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class PeriodicConnectionTypeInput extends PeriodicInput {

    @SuppressWarnings("unused")
    private final static String TAG = PeriodicConnectionTypeInput.class.getSimpleName();

    /**
     * {@link SharedPreferences} key to set the statistics monitoring period.
     */
    public final static String PREF_KEY_CONNECTION_TYPE_PERIOD = "PeriodicConnectionTypeInputMs";

    /**
     * Default net traffic monitoring interval in milliseconds. Currently set to
     * {@value #PREF_DEFAULT_CONNECTION_TYPE_PERIOD}.
     */
    public final static int PREF_DEFAULT_CONNECTION_TYPE_PERIOD = 1000 * 60 * 5;

    public final static String KEY_CONNECTION_TYPE = "PeriodicConnectionTypeInput.connectionType";
    public final static String KEY_MOBILE_NETWORK_TYPE = "PeriodicConnectionTypeInput.mobileNetworkType";

    private final static String NO_CONNECTION = "NONE";
    private final static String NO_DATA = "";


    /**
     * @param context
     */
    public PeriodicConnectionTypeInput(MoSTApplication context) {
        super(context, context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getInt(
                PREF_KEY_CONNECTION_TYPE_PERIOD, PREF_DEFAULT_CONNECTION_TYPE_PERIOD));
    }

    @Override
    public void workToDo() {
        DataBundle b = _bundlePool.borrowBundle();
        b.putLong(Input.KEY_TIMESTAMP, System.currentTimeMillis());
        b.putInt(Input.KEY_TYPE, Input.Type.PERIODIC_CONNECTION_TYPE.toInt());
        if (isConnected(getContext())) {
            if (isConnectedWifi(getContext())) {
                b.putString(KEY_CONNECTION_TYPE, getNetworkInfo(getContext()).getTypeName());
                b.putString(KEY_MOBILE_NETWORK_TYPE, NO_DATA);

            } else if (isConnectedMobile(getContext())) {
                b.putString(KEY_CONNECTION_TYPE, getNetworkInfo(getContext()).getTypeName().toUpperCase(Locale.ITALY));
                b.putString(KEY_MOBILE_NETWORK_TYPE, getNetworkInfo(getContext()).getSubtypeName());
            }

        } else {
            b.putString(KEY_CONNECTION_TYPE, NO_CONNECTION);
            b.putString(KEY_MOBILE_NETWORK_TYPE, NO_DATA);
        }

        post(b);
        scheduleNextStart();
    }

    @Override
    public Type getType() {
        return Input.Type.PERIODIC_CONNECTION_TYPE;
    }


    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

}
