/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author marcomoschettini
 */

package org.most.pipeline;

import android.annotation.SuppressLint;
import android.util.Log;

import org.most.DataBundle;
import org.most.MoSTApplication;
import org.most.input.Input;
import org.most.input.PeriodicAccelerometerInput;
import org.most.input.PeriodicFusionLocationInput;
import org.most.utils.Complex;
import org.most.utils.MathUtils;
import org.most.utils.Utils;

import java.util.HashSet;
import java.util.Set;

public class PipelineTraining extends Pipeline {
    public final static String TAG = PipelineTraining.class.getSimpleName();
    public final static int BUFFER_LENGTH = 512;
    public final static String PREF_KEY_PERIODIC_ACCELEROMETER_RATE = "InputPeriodicAccelerometer.Rate";
    private final String activity_type = "TRAINING";


    private double input;
    private double norm;
    private int n;
    private double[] input_samples;
    private Complex[] fftres;
    private double[] real_input;
    private int activity_pole;
    private Utils utils;

    public PipelineTraining(MoSTApplication context) {
        super(context);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onActivate() {

        //declarations
        n = 0;
        input_samples = new double[BUFFER_LENGTH];
        real_input = new double[BUFFER_LENGTH];

        for (int i = 0; i < BUFFER_LENGTH; i++)
            input_samples[i] = 0;
        utils = new Utils(getContext());
        activity_pole = utils.getPole(activity_type);

        Log.d(TAG, "Training started");

		/*
		Editor editor = getContext().getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).edit();
		editor.putInt(PREF_KEY_PERIODIC_ACCELEROMETER_RATE, SensorManager.SENSOR_DELAY_UI); //Possible values = SENSOR_DELAY_UI, SENSOR_DELAY_NORMAL,SENSOR_DELAY_GAME,SENSOR_DELAY_FASTEST
		editor.apply();
		*/
        return super.onActivate();
    }

    @SuppressWarnings("unused")
    public void onData(DataBundle b) {
        if (b.getInt(KEY_TYPE) == Input.Type.PERIODIC_ACCELEROMETER.toInt()) {
            //Accelerometer
            float[] data = b.getFloatArray(PeriodicAccelerometerInput.KEY_PERIODIC_ACCELERATIONS);
            long accelerometer_timestamp = b.getLong(PeriodicAccelerometerInput.KEY_TIMESTAMP);

            //norm of accelerometer coefficients
            input = Math.sqrt(Math.pow(data[0], 2) + Math.pow(data[1], 2) + Math.pow(data[2], 2));
            input_samples[n % BUFFER_LENGTH] = input;
            n++;

            if (n % BUFFER_LENGTH == 0) //Calculate time --> Every time sample buffer is full
            {
                input_samples = MathUtils.verticalShift(input_samples); //Shifting on the x axis
                norm = MathUtils.getNorm(input_samples); //Norm
                fftres = MathUtils.fft(input_samples); //FFT
                Log.d(TAG, "norma: " + norm);

                String samples_to_db = "" + norm;
                for (int i = 0; i < fftres.length; i++) //Real coefficient to normalize
                {
                    real_input[i] = Math.sqrt(Math.pow(fftres[i].re(), 2) + Math.pow(fftres[i].im(), 2));
                    samples_to_db += "," + real_input[i];
                }

                utils.appendLog(samples_to_db, activity_type, activity_pole);
            }
        }
        if (b.getInt(KEY_TYPE) == Input.Type.PERIODIC_FUSION_LOCATION.toInt()) {
            //GPS
            double gps_accuracy = b.getDouble(PeriodicFusionLocationInput.KEY_ACCURACY);
            long gps_timestamp = b.getLong(PeriodicFusionLocationInput.KEY_TIMESTAMP);
            double latitude = b.getDouble(PeriodicFusionLocationInput.KEY_LATITUDE);
            double longitude = b.getDouble(PeriodicFusionLocationInput.KEY_LONGITUDE);


            utils.appendLog("[" + gps_timestamp + "] - Lat: " + latitude + " Long: " + longitude + " Accuracy: " + gps_accuracy, "gps_walk", activity_pole);
        }

    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
    }


    @Override
    public Type getType() {
        return Type.TRAINING;
    }

    @Override
    public Set<org.most.input.Input.Type> getInputs() {
        Set<Input.Type> result = new HashSet<Input.Type>();
        result.add(Input.Type.PERIODIC_ACCELEROMETER);
        result.add(Input.Type.PERIODIC_FUSION_LOCATION);
        return result;
    }

}
