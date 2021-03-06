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
import org.most.utils.DelayedWakeLockRelease;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class PeriodicFusionLocationInput extends PeriodicInput implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

	/** The Constant DEBUG. */
    private final static boolean DEBUG = true;

	/** The Constant TAG. */
    private final static String TAG = PeriodicFusionLocationInput.class.getSimpleName();

    /**
     * {@link SharedPreferences} key to set the location check period.
     */
    public final static String PREF_KEY_PERIODIC_LOCATION_PERIOD = "PeriodicFusionLocationInput.PeriodicLocationInputPeriodMs";

    /**
     * Default location monitoring interval in milliseconds. Currently set to
     * {@value #PREF_DEFAULT_PERIODIC_LOCATION_PERIOD}.
     */
	public final static int PREF_DEFAULT_PERIODIC_LOCATION_PERIOD = 150 * 1000;

    public static final String PREF_KEY_LOCATION_FASTEST_INTERVAL_MS = "PeriodicFusionLocationInput.FastestInterval";
    public static final long PREF_DEFAULT_LOCATION_FASTEST_INTERVAL = 1000;

    public static final String PREF_KEY_LOCATION_PRIORITY = "PeriodicFusionLocationInput.Priority";
	public static final int PREF_DEFAULT_LOCATION_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    public final static String KEY_LONGITUDE = "LocationInput.Longitude";
    public final static String KEY_LATITUDE = "LocationInput.Latitude";
    public final static String KEY_ACCURACY = "LocationInput.Accuracy";
    public final static String KEY_PROVIDER = "LocationInput.Provider";

    public static final long SIGNIFICANT_TIME_DIFFERENCE = 15000; // 15 seconds

    Location _lastLocation;
    LocationClient _locationClient;
    LocationRequest _locationRequest;
    boolean _isLibraryAvailable;

	/**
	 * Creates a new FusionLocationInput.
	 * 
	 * @param context
	 *            The reference {@link MoSTApplication} context.
	 */
    public PeriodicFusionLocationInput(MoSTApplication context) {
        super(context, context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getInt(
                PREF_KEY_PERIODIC_LOCATION_PERIOD, PREF_DEFAULT_PERIODIC_LOCATION_PERIOD));
    }

    @Override
    public void onInit() {
        checkNewState(Input.State.INITED);

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());

        if (ConnectionResult.SUCCESS == resultCode) {
            _isLibraryAvailable = true;

            _locationRequest = LocationRequest.create();

            _locationRequest.setInterval(getContext().getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getLong(
                    PREF_KEY_PERIODIC_LOCATION_PERIOD, PREF_DEFAULT_PERIODIC_LOCATION_PERIOD));

            _locationRequest.setPriority(getContext().getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getInt(
                    PREF_KEY_LOCATION_PRIORITY, PREF_DEFAULT_LOCATION_PRIORITY));

            _locationRequest.setFastestInterval(getContext().getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getLong(
                    PREF_KEY_LOCATION_FASTEST_INTERVAL_MS, PREF_DEFAULT_LOCATION_FASTEST_INTERVAL));

        } else {
            Log.e(TAG, "Google Play Service Library not available.");
        }

        super.onInit();
    }

    @Override
    public boolean onActivate() {
        return super.onActivate();
    }

    @Override
    public void onDeactivate() {
        if (_isLibraryAvailable) {
            if (_locationClient != null && _locationClient.isConnected()) {
                _locationClient.removeLocationUpdates(this);
                _locationClient.disconnect();
                _locationClient = null;
            }
        }
        super.onDeactivate();
    }

    @Override
    public void onFinalize() {
        super.onFinalize();
    }

    @Override
    public Type getType() {
        return Input.Type.PERIODIC_FUSION_LOCATION;
    }

    @Override
    public boolean isWakeLockNeeded() {
        return false;
    }

    public void onProviderEnabled(String provider) {
        Log.i(TAG, String.format("Provider %s enabled", provider));
    }

    public void onProviderDisabled(String provider) {
        Log.e(TAG, String.format("Provider %s disabled", provider));
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status == LocationProvider.OUT_OF_SERVICE) {
            Log.e(TAG, "Location provider out of service: " + provider);
        } else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
            Log.w(TAG, "Location provider temp unavailable: " + provider);
        }
    }

    public void onConnected(Bundle arg0) {
        Log.w(TAG, "Connected");
        _locationClient.requestLocationUpdates(_locationRequest, this);
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Can't connect to Google Play Library. Error code: " + connectionResult.getErrorCode());
    }

    public void onDisconnected() {
        Log.w(TAG, "Disconnected");
    }

    public void onLocationChanged(Location newLocation) {
        if (newLocation == null || (newLocation.getLatitude() == 0.0 && newLocation
                .getLongitude() == 0.0)) {
            // filter out 0.0, 0.0 locations
            return;
        }
        if (DEBUG) {
            Log.d(TAG, String.format(
                    "New location:  lat %f - long %f (accuracy: %f)",
                    newLocation.getLatitude(), newLocation.getLongitude(),
                    newLocation.getAccuracy(), newLocation.getProvider()));
        }
        if (isBetterThanCurrent(newLocation)) {
            _lastLocation = newLocation;
            DataBundle b = _bundlePool.borrowBundle();
            b.putDouble(KEY_LATITUDE, _lastLocation.getLatitude());
            b.putDouble(KEY_LONGITUDE, _lastLocation.getLongitude());
            b.putDouble(KEY_ACCURACY, _lastLocation.getAccuracy());
            b.putString(KEY_PROVIDER, _lastLocation.getProvider());
            b.putLong(Input.KEY_TIMESTAMP, System.currentTimeMillis());
            b.putInt(Input.KEY_TYPE, getType().toInt());
            post(b);

            if (_locationClient != null && _locationClient.isConnected()) {
                _locationClient.removeLocationUpdates(this);
                _locationClient.disconnect();
                _locationClient = null;
            }
        }
    }

    private boolean isBetterThanCurrent(Location newLocation) {
        if (_lastLocation == null) {
            return true;
        }
        long timeDiff = newLocation.getTime() - _lastLocation.getTime();
        return timeDiff > SIGNIFICANT_TIME_DIFFERENCE
                || (newLocation.getAccuracy() <= _lastLocation.getAccuracy());
    }


    @Override
    public void workToDo() {
        if (_isLibraryAvailable) {
            if (_locationClient == null) {

                _locationClient = new LocationClient(getContext(), this, this);
                _locationClient.connect();

                getContext().getWakeLockHolder().acquireWL();
                new DelayedWakeLockRelease(getContext(), 30000).start();
            }
        }

        scheduleNextStart();
    }

}
