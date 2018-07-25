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
 * @author marcomoschettini, giuseppe giammarino
 */

package org.most.pipeline;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.most.DataBundle;
import org.most.MoSTApplication;
import org.most.input.Input;
import org.most.input.PeriodicAccelerometerInput;
import org.most.input.PeriodicFusionLocationInput;
import org.most.utils.Complex;
import org.most.utils.MathUtils;
import org.most.utils.Utils;
import org.tracking.ActivityCircularBuffer;
import org.tracking.ActivityState;
import org.tracking.BusLine;
import org.tracking.BusStop;
import org.tracking.BusStopFinder;
import org.tracking.GPSPosition;
import org.tracking.TrainLine;
import org.tracking.TrainLineFinder;
import org.tracking.TrainStation;
import org.tracking.TrainStationFinder;
import org.tracking.Versor;
import org.tracking.VersorFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PipelineDR extends Pipeline {
    public final static String TAG = PipelineDR.class.getSimpleName();

    public static final String PREF_KEY_DUMP_TO_DB = "PipelineDR.DumpToDB";
    public static final boolean PREF_DEFAULT_DUMP_TO_DB = true;
    public static final String PREF_KEY_SEND_INTENT = "PipelineDR.SendIntent";
    public static final boolean PREF_DEFAULT_SEND_INTENT = false;

    public static final String KEY_ACTION = "PipelineDR";

    public static final String KEY_DR_STATE = "PipelineDR.state";
    public static final String KEY_DR_POLE = "PipelineDR.pole";
    public static final String KEY_DR_LATITUDE = "PipelineDR.latitude";
    public static final String KEY_DR_LONGITUDE = "PipelineDR.longitude";
    public static final String KEY_DR_ACCURACY = "PipelineDR.accuracy";
    public static final String KEY_DR_TIMESTAMP = "PipelineDR.timestamp";
    public static final String KEY_DR_STATUS = "PipelineDR.status";

    public static final String TBL_DR = "DYNAMIC_RECOGNITION";
    public static final String FLD_DR_STATE = "state";
    public static final String FLD_DR_POLE = "pole";
    public static final String FLD_DR_LATITUDE = "latitude";
    public static final String FLD_DR_LONGITUDE = "longitude";
    public static final String FLD_DR_ACCURACY = "accuracy";
    public static final String FLD_DR_TIMESTAMP = "timestamp";
    public static final String FLD_DR_STATUS = "status";

    public static final String CREATE_DR_TABLE = String
            .format("_ID INTEGER PRIMARY KEY, %s TEXT NOT NULL, %s TEXT NOT NULL, %s FLOAT NOT NULL, %s FLOAT NOT NULL, %s FLOAT NOT NULL, %s BIGINT NOT NULL, %s TEXT NOT NULL",
                    FLD_DR_STATE, FLD_DR_POLE, FLD_DR_LATITUDE, FLD_DR_LONGITUDE, FLD_DR_ACCURACY,
                    FLD_DR_TIMESTAMP, FLD_DR_STATUS);

    public final static String PREF_KEY_ACCELEROMETER_RATE = "InputAccelerometer.Rate";
    public final static int BUFFER_LENGTH = 512;

    public final static double FOOTSPEED_LIMIT = 3.055555556; //about 12 km/h
    public final static double RUN_AVG_LIMIT = 0.277777778; //about 1 km/h
    public final static double ACCURACY_THR = 100; //about 200 meters

    private double input;
    private double norm;

    private int n;
    private double[] input_samples;
    private Complex[] fftres;
    private double[] real_input;
    private List<Versor> versors;

    private ActivityCircularBuffer activity_buffer;
    private ActivityState previous_activity;
    private ActivityState current_activity;

    private GPSPosition current_position;
    private GPSPosition previous_position;

    private GPSPosition trip_starting_position;
    private GPSPosition trip_ending_position;

    private BusStopFinder bsfinder;
    private TrainLineFinder tlfinder;
    private TrainStationFinder tsfinder;
    private VersorFactory versor_factory;

    private int bus_score;
    private boolean probably_traveling_by_train;
    private boolean just_ended_train_travel;
    List<TrainStation> stations_on_the_road;

    private double current_speed;

    private Utils utils;

    protected boolean _isDump;
    protected boolean _isSend;
    private boolean DEBUG_MODE = true;
    private int version;

    public PipelineDR(MoSTApplication context) {
        super(context);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onActivate() {
        //declarations
        n = 0;
        utils = new Utils(getContext());
        input_samples = new double[BUFFER_LENGTH];
        real_input = new double[BUFFER_LENGTH];

        for (int i = 0; i < BUFFER_LENGTH; i++)
            input_samples[i] = 0;


        version = utils.getPole("score");

        versor_factory = new VersorFactory(getContext());
        try {

            versors = versor_factory.getVersors();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Versors loaded");

        activity_buffer = new ActivityCircularBuffer(5);
        Log.i(TAG, "Activity buffer initialized");

		/*
		Editor editor = getContext().getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).edit();
		editor.putInt(PREF_KEY_ACCELEROMETER_RATE, SensorManager.SENSOR_DELAY_UI); //Possible values = SENSOR_DELAY_UI, SENSOR_DELAY_NORMAL,SENSOR_DELAY_GAME,SENSOR_DELAY_FASTEST
		editor.apply();
		*/
        bsfinder = new BusStopFinder();
        bus_score = 0;

        tlfinder = new TrainLineFinder();
        tsfinder = new TrainStationFinder();

        probably_traveling_by_train = false;
        just_ended_train_travel = false;
        stations_on_the_road = new ArrayList<TrainStation>();

        Log.i(TAG, "Activity recognition started");

        checkNewState(State.ACTIVATED);
        _isDump = getContext().getSharedPreferences(MoSTApplication.PREF_PIPELINES, Context.MODE_PRIVATE).getBoolean(
                PREF_KEY_DUMP_TO_DB, PREF_DEFAULT_DUMP_TO_DB);
        _isSend = getContext().getSharedPreferences(MoSTApplication.PREF_PIPELINES, Context.MODE_PRIVATE).getBoolean(
                PREF_KEY_SEND_INTENT, PREF_DEFAULT_SEND_INTENT);
        return super.onActivate();
    }

    public void onData(DataBundle b) {
        if (b.getInt(KEY_TYPE) == Input.Type.PERIODIC_ACCELEROMETER.toInt()) //Accelerometer (Fired every 30 seconds...)
        {
            //Accelerometer
            float[] data = b.getFloatArray(PeriodicAccelerometerInput.KEY_PERIODIC_ACCELERATIONS);
            long accelerometer_timestamp = b.getLong(PeriodicAccelerometerInput.KEY_TIMESTAMP); //timestamp in nanoseconds... I have to convert it!
            accelerometer_timestamp = (new Date()).getTime() + (accelerometer_timestamp - System.nanoTime()) / 1000000L; //convert to human readable time
            accelerometer_timestamp = Long.parseLong(("" + accelerometer_timestamp).substring(0, 10));
            input = Math.sqrt(Math.pow(data[0], 2) + Math.pow(data[1], 2) + Math.pow(data[2], 2));
            input_samples[n % BUFFER_LENGTH] = input;
            n++;

            if (n % BUFFER_LENGTH == 0) //every "buffer" samples
            {
                input_samples = MathUtils.verticalShift(input_samples); //Shifting on the x axis
                norm = MathUtils.getNorm(input_samples); //Norm
                fftres = MathUtils.fft(input_samples); //FFT

                for (int i = 0; i < BUFFER_LENGTH; i++) //Real coefficient to normalize
                    real_input[i] = Math.sqrt(Math.pow(fftres[i].re(), 2) + Math.pow(fftres[i].im(), 2));

                real_input = MathUtils.normalize(real_input);
                Versor best_versor = null;
                double best_score = 0;
                for (Versor v : versors) //accelerometer score calc
                {
                    double dot = MathUtils.dot(real_input, v.getFourier_shape());
                    double amp_gauss = MathUtils.gauss_value(v.getAmp_average(), v.getAmp_std(), norm);
                    double shape_gauss = MathUtils.gauss_value(0, v.getShape_std(), Math.acos(dot));
                    double score = amp_gauss * shape_gauss;
                    if (score > best_score) {
                        best_score = score;
                        best_versor = v;
                    }
                }
                current_activity = new ActivityState(best_versor.getActivity(), best_versor.getActivity_pole(), best_score, accelerometer_timestamp); //activity recognition by accelerometer
                activity_buffer.add(current_activity);
            }
        }

        if (b.getInt(KEY_TYPE) == Input.Type.PERIODIC_FUSION_LOCATION.toInt()) //GPS (Fired every 3 minutes...?)
        {
            double gps_accuracy = b.getDouble(PeriodicFusionLocationInput.KEY_ACCURACY);
            long gps_timestamp = b.getLong(PeriodicFusionLocationInput.KEY_TIMESTAMP);
            gps_timestamp = Long.parseLong(("" + gps_timestamp).substring(0, 10)); //rounding timestamp
            double latitude = b.getDouble(PeriodicFusionLocationInput.KEY_LATITUDE);
            double longitude = b.getDouble(PeriodicFusionLocationInput.KEY_LONGITUDE);

            if (current_position != null)
                previous_position = current_position;

            current_position = new GPSPosition(latitude, longitude, gps_accuracy, gps_timestamp);
            Log.d(TAG, "CURRENT_POSITION: " + current_position);

            if (previous_position != null && current_position != null) //calculate speed
            {
                if (current_position.getAccuracy() > ACCURACY_THR || previous_position.getAccuracy() > ACCURACY_THR) //Accuracy Check
                {
                    Log.i(TAG, "ACCURACY LOW!!"); //FIXME Check GPS Accuracy!!!
                }
                current_speed = MathUtils.getSpeed(previous_position, current_position);
                Log.d(TAG, "CURRENT_SPEED: " + current_speed + " meters/seconds");
                utils.appendLog("CURRENT_SPEED: " + current_speed + " meters/seconds", "score", version);
                utils.appendLog("CURRENT_SPEED: " + current_speed + " meters/seconds. position=" + current_position + " activity:" + current_activity, "logs", version);
            }

//            GPSPosition start_test_pos = new GPSPosition(44.4858562, 11.3447669, 37.5, 1393081381);
//            GPSPosition end_test_pos = new GPSPosition(44.4887213, 11.3336603, 87.0, 1392995161);
//            Log.d(TAG,""+isTripOnBus(start_test_pos, end_test_pos));

            //current_activity = getAvgActivity(activity_buffer); //Average on accelerometers activities...
            // TODO Check if walking for a long time...
            if (current_activity != null) //is still, walking, running, on car or on bus??
            {
                if (current_position.getAccuracy() < ACCURACY_THR) //checking the accuracy... Waiting for good accuracy...
                {
                    utils.appendLog("GOOD ACCURACY. Current activity: " + current_activity + " Previous activity: " + previous_activity + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                    if (current_activity.isWalking()) {
                        utils.appendLog("CURRENT ACTIVITY:" + current_activity + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                        if (current_speed > FOOTSPEED_LIMIT) //walking on bus or on a train
                        {
                            utils.appendLog("1 vWalking", "logs", version);
                            if (probably_traveling_by_train)//check if I'm still on train (if I were on train... )
                            {
                                try {//Manage here the exception because if i had a problem connection checking train lines, I still estimate train travel
                                    if (searchTrainLines(current_position, 15).isEmpty())//this check is here and not above to increase the app's performances
                                    {
                                        utils.appendLog("walking. new state ON VEHICLE && previous_activity=vehicle. Just understood I was not on train\n", "logs", version);
                                        //Reset train variables
                                        probably_traveling_by_train = false;
                                        stations_on_the_road.clear();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "train_infos", version);
                                    utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                    utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "errors", version);
                                }
                            }
                            current_activity = new ActivityState("vehicle", "walking", 0.5, current_position.getTimestamp()); //walking on bus or on train
                            utils.appendLog("2 vWalking" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                        } else {
                            if (previous_activity != null && previous_activity.isOnVehicle()) //dismounted
                            {
                                utils.appendLog("CURRENT ACTIVITY:" + current_activity + " e PREVIOUS ACTIVITY:" + previous_activity + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                                trip_ending_position = current_position;
                                Log.d(TAG, "TRIP ENDED at: " + trip_ending_position.toString());

                                if (probably_traveling_by_train) {
                                    utils.appendLog("WALKING, TRIP END. probably_traveling_by_train:" + probably_traveling_by_train + "\n", "logs", version);
                                    if (!searchTrainStations(trip_ending_position, 300).isEmpty())//This check is here to increase performances
                                    {
                                        utils.appendLog("WALKING, TRIP END. there are stations and i was probably on train" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                        //dismounting from train and getting travel infos
                                        Log.d(TAG, "TRIP ON TRAIN ENDED at: " + trip_ending_position.toString());
                                        utils.appendLog("TRIP ON TRAIN ENDED at: " + trip_ending_position.toString(), "trip", version);
                                        utils.appendLog("TRIP ON TRAIN ENDED at: " + trip_ending_position.toString() + " last position on train was:" + previous_position + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                        getTrainTripInfos(trip_starting_position, trip_ending_position, previous_position);
                                        just_ended_train_travel = true;
                                    } else {
                                        utils.appendLog("WALKING, TRIP END. there are NOT stations and i was probably on train" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                        //dismounting far from a train station or train line. that means I was traveling on car or on bus
                                        Log.d(TAG, "TRIP ENDED at: " + trip_ending_position.toString());
                                        utils.appendLog("TRIP ENDED at: " + trip_ending_position.toString(), "trip", version);
                                        utils.appendLog("TRIP ENDED at: " + trip_ending_position.toString() + " last position on vehicle:" + previous_position, "logs", version);
                                        just_ended_train_travel = false;
                                    }
                                } else {
                                    utils.appendLog("WALKING, TRIP END. I wasn NOT probably on train" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                    //dismounting far from a train station or train line. that means I was traveling on car or on bus
                                    Log.d(TAG, "TRIP ENDED at: " + trip_ending_position.toString());
                                    utils.appendLog("TRIP ENDED at: " + trip_ending_position.toString(), "trip", version);
                                    utils.appendLog("TRIP ENDED at: " + trip_ending_position.toString() + " last position on vehicle:" + previous_position, "logs", version);
                                    just_ended_train_travel = false;
                                }
                                //reset counters
                                probably_traveling_by_train = false;
                                stations_on_the_road.clear();
                            }
                        }
                    } else if (current_activity.isRunning()) {
                        utils.appendLog("CURRENT ACTIVITY:" + current_activity + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                        if (current_speed < RUN_AVG_LIMIT) //Only shaking device
                        {
                            current_activity = new ActivityState("still", "shaking", 0.5, current_position.getTimestamp());
                            utils.appendLog("SHAKING DEVICE. probably_traveling_by_train:" + probably_traveling_by_train + "\n", "logs", version);
                        } else if (previous_activity != null && previous_activity.isOnVehicle()) {
                            utils.appendLog("CURRENT ACTIVITY:" + current_activity + " e PREVIOUS ACTIVITY:" + previous_activity + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                            if (current_speed > FOOTSPEED_LIMIT) //you are shaking on a vehicle
                            {
                                utils.appendLog("1SHAKING ON A VEHICLE. probably_traveling_by_train:" + probably_traveling_by_train + "\n", "logs", version);
                                if (probably_traveling_by_train)//check if I'm still on train (if I were on train... )
                                {
                                    try {//Manage here the exception because if i had a problem connection checking train lines, I still estimate train travel
                                        if (searchTrainLines(current_position, 15).isEmpty())//this check is here and not above to increase the app's performances
                                        {
                                            utils.appendLog("running. new state ON VEHICLE && previous_activity=vehicle. Just understood I was not on train\n", "logs", version);
                                            //Reset train variables
                                            probably_traveling_by_train = false;
                                            stations_on_the_road.clear();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "train_infos", version);
                                        utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                        utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "errors", version);
                                    }

                                }
                                current_activity = new ActivityState("vehicle", "shaking", 0.5, current_position.getTimestamp());
                                utils.appendLog("2SHAKING ON A VEHICLE. probably_traveling_by_train:" + probably_traveling_by_train + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                            } else //dismounted
                            {
                                utils.appendLog("CURRENT ACTIVITY:" + current_activity + " DISMOUNTING!" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                                trip_ending_position = current_position;
                                Log.d(TAG, "TRIP ENDED at: " + trip_ending_position.toString());

                                if (probably_traveling_by_train) {
                                    utils.appendLog("RUNNING, TRIP END. probably_traveling_by_train:" + probably_traveling_by_train + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                    if (!searchTrainStations(trip_ending_position, 300).isEmpty())//This check is here to increase performances
                                    {
                                        utils.appendLog("RUNNING, TRIP END. there are stations and i was probably on train" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                        //dismounting from train and getting travel infos
                                        Log.d(TAG, "TRIP ON TRAIN ENDED at: " + trip_ending_position.toString());
                                        utils.appendLog("TRIP ON TRAIN ENDED at: " + trip_ending_position.toString(), "trip", version);
                                        utils.appendLog("TRIP ON TRAIN ENDED at: " + trip_ending_position.toString() + " last position on train was:" + previous_position, "logs", version);
                                        getTrainTripInfos(trip_starting_position, trip_ending_position, previous_position);
                                        just_ended_train_travel = true;
                                    } else {
                                        utils.appendLog("RUNNING, TRIP END. there are NOT stations and i was probably on train" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                        //dismounting far from a train station or train line. that means I was traveling on car or on bus
                                        Log.d(TAG, "TRIP ENDED at: " + trip_ending_position.toString());
                                        utils.appendLog("TRIP ENDED at: " + trip_ending_position.toString(), "trip", version);
                                        utils.appendLog("TRIP ENDED at: " + trip_ending_position.toString() + " last position on vehicle:" + previous_position, "logs", version);
                                        just_ended_train_travel = false;
                                    }
                                } else {
                                    utils.appendLog("RUNNING, TRIP END. I wasn NOT probably on train" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                    //dismounting far from a train station or train line. that means I was traveling on car or on bus
                                    Log.d(TAG, "TRIP ENDED at: " + trip_ending_position.toString());
                                    utils.appendLog("TRIP ENDED at: " + trip_ending_position.toString(), "trip", version);
                                    utils.appendLog("TRIP ENDED at: " + trip_ending_position.toString() + " last position on vehicle:" + previous_position, "logs", version);
                                    just_ended_train_travel = false;
                                }
                                //reset counters
                                probably_traveling_by_train = false;
                                stations_on_the_road.clear();
                            }
                        }
                    } else if (current_activity.isStill()) {
                        if (current_speed < FOOTSPEED_LIMIT)//you are still
                        {
                            utils.appendLog("You are STILL. probably_traveling_by_train:" + probably_traveling_by_train + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()), "logs", version);

                            if (previous_activity != null && previous_activity.isOnVehicle()) //if you dismount you must walk!
                            {
                                utils.appendLog("STILL. if you dismount you must walk!. probably_traveling_by_train:" + probably_traveling_by_train + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                                int near_found_size = 0;
                                if (probably_traveling_by_train)//I search train stops just if I'm on train
                                {
                                    List<TrainStation> near_train_stations = searchTrainStations(current_position, 300);//getting near train stations, maybe i'm at a train stop
                                    near_found_size = near_train_stations.size();
                                    utils.appendLog("if you dismount you must walk! AFTER TRAIN STATIONS RESEARCHES" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                    if (near_train_stations.size() > 0) {
                                        for (TrainStation station : near_train_stations) {
                                            if (!stations_on_the_road.contains(station))//maybe i stop 2 times around a train station, i add that station just 1 time
                                            {
                                                stations_on_the_road.add(station);
                                                utils.appendLog("STILL AROUND THIS TRAIN STATION:" + station.getName() + "\n", "logs", version);
                                            }
                                        }
                                        utils.appendLog("TRAIN STATIONS FOUND UNTIL NOW ON THE ROAD:" + stations_on_the_road.size() + stations_on_the_road + "\n", "logs", version);
                                    }
                                }

                                utils.appendLog("if you dismount you must walk! AFTER TRAIN STOPS RESEARCHES" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                //*****************BUS***************************
//	            				List<BusStop> near_bus_stops = searchBusStops(current_position);
//	            				utils.appendLog("if you dismount you must walk! AFTER BUS stop RESEARCHES"+ new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime())+ "\n", "logs", version);
//	            				if(near_bus_stops.size()>0) //stopped near a bus stop
//	            					bus_score+=1;
//	            				else
//	            					bus_score-=1;	            				
//	            				//*****************BUS***************************
                                utils.appendLog("AROUND THIS POSITION: " + current_position + "there are: " +
//	            						//*****************BUS***************************"\n\tnear_bus_stops=" + near_bus_stops.size() + 
                                        "near_train_stations=" + near_found_size
//	            						//*****************BUS***************************+ "\t bus_score=" + bus_score 
                                        + " probably_traveling_by_train:" + probably_traveling_by_train + "\n", "logs", version);

                                utils.appendLog("1you are vSTILL but moving fast. probably_traveling_by_train:" + probably_traveling_by_train + " just_ended_train_travel:" + just_ended_train_travel + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                                if (probably_traveling_by_train)//check if I'm still on train (if I were on train... )
                                {
                                    try {
                                        if (searchTrainLines(current_position, 15).isEmpty())//this check is here and not above to increase the app's performances
                                        {
                                            utils.appendLog("still. new activity ON VEHICLE still && previous_activity=vehicle. Just understood I was not on train\n", "logs", version);
                                            //Reset train variables
                                            probably_traveling_by_train = false;
                                            stations_on_the_road.clear();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        probably_traveling_by_train = true;
                                        utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "train_infos", version);
                                        utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                        utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "errors", version);
                                    }
                                }
                                current_activity = new ActivityState("vehicle", "still", 0.5, current_position.getTimestamp()); //still on the road?
                                utils.appendLog("2you are vSTILL but moving fast. probably_traveling_by_train:" + probably_traveling_by_train + " just_ended_train_travel:" + just_ended_train_travel + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                            }
                            utils.appendLog("END OF current_speed < FOOTSPEED_LIMIT\n", "logs", version);
                        } else //you are still but moving fast
                        {
                            utils.appendLog("1you are nSTILL but moving fast. probably_traveling_by_train:" + probably_traveling_by_train + " just_ended_train_travel:" + just_ended_train_travel + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                            if (probably_traveling_by_train)//check if I'm still on train (if I were on train... )
                            {
                                try {
                                    if (searchTrainLines(current_position, 15).isEmpty())//this check is here and not above to increase the app's performances
                                    {
                                        utils.appendLog("still. new activity ON VEHICLE normal && previous_activity=vehicle. Just understood I was not on train\n", "logs", version);
                                        //Reset train variables
                                        probably_traveling_by_train = false;
                                        stations_on_the_road.clear();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    probably_traveling_by_train = true;
                                    utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "train_infos", version);
                                    utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                    utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "errors", version);
                                }
                            }
                            current_activity = new ActivityState("vehicle", "normal", 0.5, current_position.getTimestamp());
                            utils.appendLog("2you are nSTILL but moving fast. probably_traveling_by_train:" + probably_traveling_by_train + " just_ended_train_travel:" + just_ended_train_travel + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                        }
                    }

                    if (current_activity.isOnVehicle()) {
                        utils.appendLog("current_activity.isOnVehicle(). PREVIOUS ACTIVITY=" + previous_activity + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                        if ((previous_activity != null && !previous_activity.isOnVehicle()) || (previous_activity == null)) //trip started after another activity or just turning on app
                        {
                            if (previous_activity == null)
                                trip_starting_position = current_position;
                            else
                                trip_starting_position = previous_position; //last 'on foot' position
                            Log.d(TAG, "TRIP STARTED at: " + trip_starting_position.toString());
                            utils.appendLog("TRIP STARTED at: " + trip_starting_position.toString(), "trip", version);

                            //Check if i'm on a train line and if there is at least a train station around me
                            try {
                                if (!searchTrainLines(trip_starting_position, 15).isEmpty() && searchTrainStations(trip_starting_position, 300).size() > 0)
                                    probably_traveling_by_train = true;
                                else {
                                    probably_traveling_by_train = false;
                                    stations_on_the_road.clear();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                stations_on_the_road.clear();
                                utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "train_infos", version);
                                utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                                utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "errors", version);
                            }
                            utils.appendLog("TRIP STARTED at position= " + trip_starting_position.toString() + ". probably_traveling_by_train:" + probably_traveling_by_train + " just_ended_train_travel:" + just_ended_train_travel + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                        }
                    }

                    if (trip_starting_position != null && trip_ending_position != null) //check vehicle trip
                    {
                        utils.appendLog("check vehicle trip if(trip_starting_position!= null && trip_ending_position!=null). probably_traveling_by_train:" + probably_traveling_by_train + " just_ended_train_travel:" + just_ended_train_travel + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
                        boolean was_on_train = just_ended_train_travel;
                        utils.appendLog("check vehicle trip. was_on_train: " + was_on_train, "logs", version);
                        //*****************BUS***************************
                        boolean was_on_bus = isTripOnBus(trip_starting_position, trip_ending_position);
                        utils.appendLog("check vehicle trip. was_on_bus: " + was_on_bus, "logs", version);
                        if (!was_on_bus && !was_on_train)
                        //*****************BUS***************************
                        //if(!was_on_train)
                        {
                            Log.d(TAG, "Your trip was on CAR");
                            utils.appendLog("Your trip was on CAR\n", "trip", version);
                        }
                        utils.appendLog("check vehicle trip RESET COUNTERS " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

                        //reset the trip variables
                        trip_starting_position = null;
                        trip_ending_position = null;
                        bus_score = 0;
                        probably_traveling_by_train = false;
                        just_ended_train_travel = false;
                        stations_on_the_road.clear();
                    }

                    //save results

                    if (_isSend) {
                        //intent
                        Intent i = new Intent(KEY_ACTION);
                        i.putExtra(KEY_TIMESTAMP, current_activity.getTimestamp() * 1000);
                        i.putExtra(KEY_DR_STATE, current_activity.getState());
                        i.putExtra(KEY_DR_POLE, current_activity.getPole());
                        i.putExtra(KEY_DR_LATITUDE, current_position.getLatitude());
                        i.putExtra(KEY_DR_LONGITUDE, current_position.getLongitude());
                        i.putExtra(KEY_DR_ACCURACY, current_position.getAccuracy());
                        i.putExtra(KEY_DR_STATUS, current_activity.getPole());
                        getContext().sendBroadcast(i);

                    }
                    if (_isDump) {
                        //to db
                        ContentValues cv = new ContentValues();
                        cv.put(FLD_DR_TIMESTAMP, current_activity.getTimestamp() * 1000);
                        cv.put(FLD_DR_STATE, current_activity.getState());
                        cv.put(FLD_DR_POLE, current_activity.getPole());
                        cv.put(FLD_DR_LATITUDE, current_position.getLatitude());
                        cv.put(FLD_DR_LONGITUDE, current_position.getLongitude());
                        cv.put(FLD_DR_ACCURACY, current_position.getAccuracy());
                        cv.put(FLD_DR_STATUS, current_activity.getPole());
                        getContext().getDbAdapter().storeData(TBL_DR, cv, true);

                        if (previous_activity != null && DEBUG_MODE) {
                            Log.d(TAG, "CURRENT_ACTIVITY: " + current_activity.toString() + "\nPREVIOUS_ACTIVITY: " + previous_activity.toString() + "\n\n");
                            utils.appendLog("CURRENT_ACTIVITY: " + current_activity.toString() + "\nPREVIOUS_ACTIVITY: " + previous_activity.toString() + "\n\n", "score", version);
                        } else {
                            Log.d(TAG, "CURRENT_ACTIVITY: " + current_activity.toString() + "\n\n");
                            utils.appendLog("CURRENT_ACTIVITY: " + current_activity.toString() + "\n\n", "score", version);
                        }
                    }
                    previous_activity = current_activity;
                } else
                    utils.appendLog("LOW ACCURACY!" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
            }
        }
        b.release();
    }

    private boolean isTripOnBus(GPSPosition start, GPSPosition end) {
        utils.appendLog("IsTripOnBus, start of the function" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
        List<BusStop> start_bus_stops = searchBusStops(start);
        utils.appendLog("IsTripOnBus, end of first research, start of the second " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
        List<BusStop> end_bus_stops = searchBusStops(end);
        utils.appendLog("IsTripOnBus, end of second research " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
        Log.d(TAG, "START_BUS_STOPS: " + start_bus_stops);
        Log.d(TAG, "STOP_BUS_STOPS: " + end_bus_stops);

        boolean was_on_bus = false;
        List<BusLine> bus_lines_in_common = new ArrayList<BusLine>();
        for (BusStop start_stop : start_bus_stops)
            for (BusStop end_stop : end_bus_stops)
                bus_lines_in_common.addAll(start_stop.linesInCommon(end_stop));

        if (bus_lines_in_common.size() > 0) //TRIP WAS ON BUS
        {
            was_on_bus = true;
            Log.d(TAG, "[TRIP_SCORE = " + bus_score + "]Your trip was on BUS on one line of: " + bus_lines_in_common);
            utils.appendLog("[TRIP_SCORE = " + bus_score + "]Your trip was on BUS on one line of: " + bus_lines_in_common, "trip", version);
        }
        utils.appendLog("IsTripOnBus, end of the function" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

        return was_on_bus;
    }

    /**
     * Function started at the end of train trip. Executed to get trip informations
     *
     * @param start             is the GPSPosition found at the start of the trip
     * @param end               is the GPSPosition found at the end of the trip
     * @param previous_position is the last GPSPosition on the train
     * @author Giuseppe Giammarino
     */
    private void getTrainTripInfos(GPSPosition start, GPSPosition end, GPSPosition previous_position) {
        utils.appendLog("getTrainTripInfos, start of the function" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

        double radiusTrainLines = 60; //more than during trip, just to get names
        double departureRadius = start.getAccuracy() + 300; //usually the farthest quay is far maximum 250
        double arrivalRadius = start.getAccuracy() + 300;
        List<TrainLine> start_train_lines = new ArrayList<TrainLine>();
        try {
            start_train_lines = searchTrainLines(start, radiusTrainLines);
        } catch (JSONException e) {
            e.printStackTrace();
            utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "train_infos", version);
            utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
            utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "errors", version);
        }
        String trainStartString = null;
        for (TrainLine startline : start_train_lines) {
            if (trainStartString == null)
                trainStartString = "\t" + startline.getLineName() + "\n";
            else
                trainStartString += "\t" + (startline.getLineName() + "\n");
        }

        List<TrainLine> end_train_lines = new ArrayList<TrainLine>();
        try {
            end_train_lines = searchTrainLines(previous_position, radiusTrainLines);
        } catch (JSONException e) {
            e.printStackTrace();
            utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "train_infos", version);
            utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
            utils.appendLog(e.toString() + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "errors", version);
        }

        //+++++++++remove the following code line
        utils.appendLog("getTrainTripInfos, end_train_lines.size() == " + end_train_lines.size() + ", start_train_lines.size() = " + start_train_lines.size() + "\n", "train_infos", version);
        utils.appendLog("getTrainTripInfos, end_train_lines.size() == " + end_train_lines.size() + ", start_train_lines.size() = " + start_train_lines.size() + "\n", "logs", version);

        //searching for common names of train lines
        utils.appendLog("getTrainTripInfos, searching for common lines", "train_infos", version);
        utils.appendLog("getTrainTripInfos, searching for common lines", "logs", version);

        List<String> common_lines = new ArrayList<String>();

        for (TrainLine start_train_line : start_train_lines) {
            for (TrainLine end_train_line : end_train_lines) {
                if ((start_train_line.getLineName() != null) && (end_train_line.getLineName() != null)) {
                    if (start_train_line.getLineName().equals(end_train_line.getLineName())) {
                        if (!common_lines.contains(start_train_line.getLineName())) {
                            common_lines.add(start_train_line.getLineName());
                            utils.appendLog("added line:" + start_train_line.toString(), "logs", version);
                        }
                    }
                }
            }
        }
        utils.appendLog("getTrainTripInfos, end of searching for common lines" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

        //formatting the string to print
        String to_print = null;
        if (common_lines.size() == 1) {
            to_print = "with this train line: " + common_lines.get(0);
            utils.appendLog("common_lines.size(): " + common_lines.size() + " " + to_print + "\n", "train_infos", version);
            utils.appendLog("common_lines.size(): " + common_lines.size() + " " + to_print + "\n", "logs", version);

        } else if (common_lines.size() > 1) {
            String lines = null;
            for (int i = 0; i < common_lines.size(); i++) {
                lines = lines + common_lines.get(i) + ", ";
            }
            to_print = "with one of these train lines: \n\t" + lines + "\n\t";
            utils.appendLog("common_lines.size(): " + common_lines.size() + "\t " + to_print + "\n", "train_infos", version);
            utils.appendLog("common_lines.size(): " + common_lines.size() + "\t " + to_print + "\n", "logs", version);
        } else {
            to_print = "There are no train lines in common with the same name";
            utils.appendLog("common_lines.size(): " + common_lines.size() + "\t. " + to_print + "\n", "train_infos", version);
            utils.appendLog("common_lines.size(): " + common_lines.size() + "\t. " + to_print + "\n", "logs", version);
        }

        //trying to search the departure station name
        Log.d(TAG, "SEARCHING DEPARTURE TRAIN STATION...");
        utils.appendLog("getTrainTripInfos, SEARCHING DEPARTURE TRAIN STATION...\n", "logs", version);
        List<TrainStation> start_train_stations = searchTrainStations(start, departureRadius);
        utils.appendLog("getTrainTripInfos, END OF SEARCH FOR DEPARTURE TRAIN STATION\n", "logs", version);
        String start_string = null;
        //it's not always possible to retrieve the station name, probably the GPS turned on too far
        if (start_train_stations.size() == 0)
            start_string = "impossible to retrive the departure train station";
        else {
            for (TrainStation startName : start_train_stations) {
                if (start_string == null)
                    start_string = startName.getName();
                else
                    start_string += ", " + startName.getName();
            }
        }

        //trying to search the arrival station name
        Log.d(TAG, "SEARCHING ARRIVAL TRAIN STATION...");
        utils.appendLog("getTrainTripInfos, SEARCHING ARRIVAL TRAIN STATION...\n", "logs", version);
        List<TrainStation> arrival_train_stations = searchTrainStations(end, arrivalRadius);
        utils.appendLog("getTrainTripInfos, END OF SEARCH FOR ARRIVAL TRAIN STATION\n", "logs", version);
        String end_string = null;
        //it's not always possible to retrive the station name, probably the gps turned on too far
        if (arrival_train_stations.size() == 0)
            end_string = "impossible to retrive the arrival train station";
        else {
            for (TrainStation arrivalName : arrival_train_stations) {
                if (end_string == null)
                    end_string = arrivalName.getName();
                else
                    end_string += ", " + arrivalName.getName();
            }
        }

        //Removing start train station and arrival train station from train stations found on the road
        for (Iterator<TrainStation> it = stations_on_the_road.iterator(); it.hasNext(); ) {
            TrainStation foundStation = it.next();
            for (TrainStation start_station : start_train_stations) {
                if (foundStation.equals(start_station))
                    it.remove();
            }
            for (TrainStation arrival_station : arrival_train_stations) {
                if (foundStation.equals(arrival_station))
                    it.remove();
            }
        }

        String found = null;
        if (stations_on_the_road.size() > 0) {
            for (TrainStation found_station : stations_on_the_road) {
                if (found == null)
                    found = found_station.getName();
                else
                    found += ", " + found_station.getName();
            }
        } else
            found = " there are no stations found\n";

        utils.appendLog("\tSTART TRAIN_STATION:  " + start_string, "trip", version);
        utils.appendLog("\tTRAIN STATIONS FOUND ON THE ROAD: " + found, "trip", version);
        utils.appendLog("\tEND TRAIN_STATION:  " + end_string, "trip", version);

        utils.appendLog("getTrainTripInfos START TRAIN_STATION:  " + start_string + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
        utils.appendLog("getTrainTripInfos TRAIN STATIONS FOUND ON THE ROAD: " + found + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "train_infos", version);
        utils.appendLog("getTrainTripInfos END TRAIN_STATION:  " + end_string + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

        Log.d(TAG, "Your trip was on TRAIN " + to_print);
        utils.appendLog("\tTRAIN LINE: " + to_print + "\n", "trip", version);
        utils.appendLog("getTrainTripInfos. TRAIN LINE: " + to_print + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
    }

    private List<TrainLine> searchTrainLines(GPSPosition position, double radius) throws JSONException {
        utils.appendLog("searchTrainLines start of the function" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

        double searchRadius = position.getAccuracy() + radius;

        List<TrainLine> train_lines = new ArrayList<TrainLine>();
        train_lines = tlfinder.searchTrainLines(utils, version, position, searchRadius);
        String to_print = null;
        if (train_lines.size() == 1)
            to_print = "line";
        else
            to_print = "lines";
        utils.appendLog("SEARCHING TRAIN LINES NEAR: " + position + " Search radius= " + searchRadius + "m (original radius= " + radius + ". Found:" + train_lines.size() + " " + to_print, "train_infos", version);
        utils.appendLog("SEARCHING TRAIN LINES NEAR: " + position + " Search radius= " + searchRadius + "m (original radius= " + radius + ". Found:" + train_lines.size() + " " + to_print + ". probably_traveling_by_train:" + probably_traveling_by_train, "logs", version);

        if (train_lines.size() > 0) {
            String formattingString = null;
            for (TrainLine train_line : train_lines) {
                if (formattingString == null)
                    formattingString = train_line.toString() + "\n";
                else
                    formattingString += "\t" + train_line.toString() + "\n";
            }
            utils.appendLog("\t" + formattingString + "\n", "train_infos", version);
            utils.appendLog("\t" + formattingString + "\tprobably_traveling_by_train:" + probably_traveling_by_train + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
        }
        return train_lines;
    }

    private List<TrainStation> searchTrainStations(GPSPosition position, double radius) {
        utils.appendLog("searchTrainStations start of the function. probably_traveling_by_train:" + probably_traveling_by_train + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);

        double searchRadius = position.getAccuracy() + radius;
        Log.d(TAG, "SEARCH TRAIN STATION AROUND: " + searchRadius + "m");
        List<TrainStation> train_stations = new ArrayList<TrainStation>();
        train_stations = tsfinder.searchTrainStation(utils, version, position, searchRadius);
        String to_print = null;
        if (train_stations.size() == 1)
            to_print = "station";
        else
            to_print = "stations";
        utils.appendLog("SEARCHING TRAIN STATIONS AROUND: " + position + " Search radius= " + searchRadius + "m (original radius=" + radius + ".  Found:" + train_stations.size() + " " + to_print, "train_infos", version);
        utils.appendLog("SEARCHING TRAIN STATIONS AROUND: " + position + " Search radius= " + searchRadius + "m (original radius=" + radius + ".  Found:" + train_stations.size() + " " + to_print + ". probably_traveling_by_train:" + probably_traveling_by_train, "logs", version);

        //+++++++++remove the following code line
        if (train_stations.size() > 0) {
            String stationPrint = null;
            for (TrainStation station : train_stations) {
                if (stationPrint == null)
                    stationPrint = station.toString();
                else
                    stationPrint += station.toString();
            }
            utils.appendLog("\t" + stationPrint + "\n", "train_infos", version);
            utils.appendLog("\t" + stationPrint + ". probably_traveling_by_train:" + probably_traveling_by_train + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
        }
        return train_stations;
    }

    private List<BusStop> searchBusStops(GPSPosition position) {
        utils.appendLog("SEARCH BUS STOP STARTS..." + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n", "logs", version);
        double radius = position.getAccuracy() + 10; //10 meters
        Log.d(TAG, "SEARCH_RADIUS: " + radius);
        List<BusStop> bus_stops = new ArrayList<BusStop>();
        bus_stops = bsfinder.searchBusStops(position, radius);
//		bus_stops = searchBusStopss(position, radius);
        utils.appendLog("SEARCHING BUS STOPS NEAR: " + position + "\n", "logs", version);
        utils.appendLog(bus_stops + "\n", "logs", version);
        utils.appendLog("SEARCHING BUS STOPS NEAR: " + position + "\n", "bus_stops", version);
        utils.appendLog(bus_stops + "\n", "bus_stops", version);
        return bus_stops;
    }

    @Override
    public Set<Input.Type> getInputs() {
        Set<Input.Type> result = new HashSet<Input.Type>();
        result.add(Input.Type.PERIODIC_ACCELEROMETER);
        result.add(Input.Type.PERIODIC_FUSION_LOCATION);
        return result;
    }

    @Override
    public Type getType() {
        return Type.DR;
    }
}
