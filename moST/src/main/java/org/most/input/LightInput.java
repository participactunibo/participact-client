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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class LightInput extends Input implements SensorEventListener {

	/** The Constant DEBUG. */
    private final static boolean DEBUG = false;

	/** The Constant TAG. */
    private final static String TAG = LightInput.class.getSimpleName();

	/** The _sensor manager. */
    private SensorManager _sensorManager = null;

	/** The _sensor. */
    private Sensor _sensor = null;

	/** The _sensor rate. */
    private int _sensorRate = 0;

    public final static String KEY_VALUE = "LightInput.lightValue";

    public final static String PREF_KEY_LIGHTINPUT_SENSOR_RATE = "LightInputSensorRate";
    public final static int PREF_DEFAULT_LIGHTINPUT_SENSOR_RATE = SensorManager.SENSOR_DELAY_NORMAL;

	/**
	 * Return a new instance of LightInput.
	 * 
	 * @param context
	 *            Context Android
	 */
    public LightInput(MoSTApplication context) {
        super(context);
        _sensorRate = context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getInt(
                PREF_KEY_LIGHTINPUT_SENSOR_RATE, PREF_DEFAULT_LIGHTINPUT_SENSOR_RATE);
    }

    @Override
    public void onInit() {
        checkNewState(State.INITED);

        if (_sensorManager == null) {
            _sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        }
        if (_sensor == null) {
            _sensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        if (DEBUG)
            Log.d(TAG, "onInit()");

        super.onInit();
    }

    @Override
    public boolean onActivate() {
        checkNewState(State.ACTIVATED);
        if (DEBUG)
            Log.d(TAG, "onActivate()");

        boolean registrationSuccessful = _sensorManager.registerListener(this, _sensor, _sensorRate);
        if (registrationSuccessful) {
            return super.onActivate();
        } else {
            return false;
        }

    }

    public void onDeactivate() {
        checkNewState(State.DEACTIVATED);
        _sensorManager.unregisterListener(this, _sensor);
        if (DEBUG)
            Log.d(TAG, "onDeactivate()");

        super.onDeactivate();
    }

    @Override
    public void onFinalize() {
        checkNewState(State.FINALIZED);
        if (_sensorManager != null) {
            _sensorManager = null;
        }
        if (_sensor != null) {
            _sensor = null;
        }

        if (DEBUG)
            Log.d(TAG, "onFinalize()");

        super.onFinalize();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (DEBUG) {
            Log.v(TAG, "Received light sensor data");
        }

        if (!getState().equals(Input.State.ACTIVATED)) {
            return;
        }

        DataBundle b = _bundlePool.borrowBundle();
        b.putFloat(KEY_VALUE, event.values[0]);
        b.putLong(Input.KEY_TIMESTAMP, event.timestamp);
        b.putInt(Input.KEY_TYPE, Input.Type.LIGHT.toInt());

        post(b);
    }

    /**
     * Gets the sensor rate.
     *
     * @return the _sensor rate
     */
    public int getSensorRate() {
        return _sensorRate;
    }

	/**
	 * Sets the sensor rate.
	 * 
	 * @param sensorRate
	 *            the new _sensor rate
	 */
    public void setSensorRate(int sensorRate) {
        this._sensorRate = sensorRate;
        _sensorManager.unregisterListener(this, _sensor);
        _sensorManager.registerListener(this, _sensor, _sensorRate);
    }

    @Override
    public Type getType() {
        return Input.Type.LIGHT;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LightInput)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

    @Override
    public boolean isWakeLockNeeded() {
        return true;
    }
}
