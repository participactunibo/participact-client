/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.octo.android.robospice.SpiceManager;
import com.splunk.mint.Mint;

import org.most.MoSTApplication;
import org.most.MoSTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unibo.participact.activities.DashboardActivity;
import it.unibo.participact.broadcastreceivers.GcmBroadcastReceiver;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.ActionFlat;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.persistence.GeoBadgeCollected;
import it.unibo.participact.domain.persistence.InterestPoint;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.persistence.TaskStatus;
import it.unibo.participact.domain.rest.Task;
import it.unibo.participact.fragments.UserFragment;
import it.unibo.participact.network.NotificationAcceptMandatoryTaskListener;
import it.unibo.participact.network.request.AcceptTaskRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.support.CheckClientAppVersionAlarm;
import it.unibo.participact.support.GeolocalizationTaskUtils;
import it.unibo.participact.support.NotificationUtility;
import it.unibo.participact.support.ProgressAlarm;
import it.unibo.participact.support.StateLogAlarm;
import it.unibo.participact.support.UploadAlarm;

public class ParticipActService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private SpiceManager contentManager = new SpiceManager(ParticipactSpringAndroidService.class);

    private static final Logger logger = LoggerFactory.getLogger(ParticipActService.class);
    public static final String START = "ParticipActService.START";
    public static final String STOP = "ParticipActService.STOP";

    public static final String GEO_TASK_UPDATE_INTENT = "it.unibo.participact.GEO_TASK_UPDATE";

    private static Location last;

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 90;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    LocationClient locationClient;
    LocationRequest mLocationRequest;

    private AtomicBoolean isStarted = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        Mint.initAndStartSession(ParticipActService.this, "6e05d719");
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void init() {

        Editor editor = getSharedPreferences(MoSTApplication.PREF_MOST_SERVICE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(MoSTService.PREF_KEY_STORE_STATE, false);
        editor.apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        if (intent != null) {
            if (START.equals(intent.getAction()) && !isStarted.get()) {
                //il telefono � stato spento correttamente tutti i task sono in SUSPENDED
                //se invece il telefono si � spento all'improvviso alcuni RUNNING e altri SUSPENDED
                StateUtility.suspendAllTask(this, TaskState.RUNNING);
                StateUtility.activateAllTask(this, TaskState.SUSPENDED);
                ProgressAlarm.getInstance(this).start();
                UploadAlarm.getInstance(this).start();
                CheckClientAppVersionAlarm.getInstance(this).start();
                StateLogAlarm.getInstance(this).start();

                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
                locationClient = new LocationClient(this, this, this);

                locationClient.connect();

                isStarted.set(true);
                logger.info("Starting ParticipActService with START intent");
            } else if (STOP.equals(intent.getAction()) && isStarted.get()) {
                StateUtility.suspendAllTask(this, TaskState.RUNNING);
                ProgressAlarm.getInstance(this).stop();
                UploadAlarm.getInstance(this).stop();
                CheckClientAppVersionAlarm.getInstance(this).stop();
                StateLogAlarm.getInstance(this).stop();

                locationClient.disconnect();
                isStarted.set(false);
                stopSelf();
                logger.info("Stopping ParticipActService with STOP intent");
            }
        } else {
            //restore state
            ProgressAlarm.getInstance(this).start();
            UploadAlarm.getInstance(this).start();
            CheckClientAppVersionAlarm.getInstance(this).start();
            StateLogAlarm.getInstance(this).start();

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            locationClient = new LocationClient(this, this, this);

            locationClient.connect();

            logger.info("Starting ParticipActService with STICKY intent");
        }
        return START_STICKY;
    }


    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        logger.info("Location Client connection failed.");
    }

    @Override
    public void onConnected(Bundle arg0) {
        logger.info("Location Client connected.");
        locationClient.requestLocationUpdates(mLocationRequest, this);

    }

    @Override
    public void onDisconnected() {
        logger.info("Location Client disconnected.");
    }

    @Override
    public void onLocationChanged(Location location) {

        List<TaskFlat> hiddens = StateUtility.getTaskByState(this, TaskState.HIDDEN);
        for (TaskFlat taskFlat : hiddens) {
            if (GeolocalizationTaskUtils.isNotifiedByArea(taskFlat) && GeolocalizationTaskUtils.isInside(this, location.getLongitude(), location.getLatitude(), taskFlat.getNotificationArea())) {
                if (!taskFlat.getCanBeRefused()) {
                    AcceptTaskRequest request = new AcceptTaskRequest(this, taskFlat.getId());
                    if (!contentManager.isStarted()) {
                        contentManager.start(this);
                    }
                    contentManager.execute(request, new NotificationAcceptMandatoryTaskListener(this, taskFlat));
                } else {
                    StateUtility.changeTaskState(this, taskFlat, TaskState.GEO_NOTIFIED_AVAILABLE);
                }
            }
        }


        List<TaskFlat> running = StateUtility.getTaskByState(this, TaskState.RUNNING);
        for (TaskFlat taskFlat : running) {
            if (GeolocalizationTaskUtils.isActivatedByArea(taskFlat)
                    && !GeolocalizationTaskUtils.isInside(this, location.getLongitude(), location.getLatitude(),
                    taskFlat.getActivationArea())) {
                StateUtility.changeTaskState(this, taskFlat, TaskState.RUNNING_BUT_NOT_EXEC);
            }

            for (ActionFlat actionFlat: taskFlat.getActions()){

                if(actionFlat.getType().equals(ActionType.GEOFENCE)){

                    List<InterestPoint> interestList = StateUtility.getInterestPointByActionFlat(this, actionFlat.getId());

                    for(InterestPoint i : interestList){
                        if(GeolocalizationTaskUtils.isInsideCircle(this,location.getLongitude(),location.getLatitude(), i.getInterestPointString()) && ! i.isCollected()) {
                            Intent resultIntent = new Intent(this, DashboardActivity.class);

                            resultIntent.setAction(DashboardActivity.GO_TO_PROFILE_FRAGMENT);
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                            stackBuilder.addParentStack(DashboardActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );
                            NotificationUtility.addNotification(this, R.drawable.ic_collected_badge, this.getString(R.string.new_badge_collected), i.getDesctioprionGeofence(), GcmBroadcastReceiver.NOTIFICATION_NEW_BADGE, resultPendingIntent);

                            GeoBadgeCollected geoBadgeCollected = new GeoBadgeCollected();
                            geoBadgeCollected.setId(i.getId());
                            geoBadgeCollected.setDesctioprionGeofence(i.getDesctioprionGeofence());
                            geoBadgeCollected.setActionFlatId(i.getActionFlatId());
                            geoBadgeCollected.setTaskId(taskFlat.getId());
                            StateUtility.addOnGeoBadgeCollected(this, geoBadgeCollected, i);
                            StateUtility.setInterestPointCollected(this,i);

                            geoBadgeCollected.getTaskId();

                        }
                    }

                }

            }

        }

        List<TaskFlat> runningNotExec = StateUtility.getTaskByState(this, TaskState.RUNNING_BUT_NOT_EXEC);

        for (TaskFlat taskFlat : runningNotExec) {
            if (GeolocalizationTaskUtils.isActivatedByArea(taskFlat)
                    && GeolocalizationTaskUtils.isInside(this, location.getLongitude(), location.getLatitude(),
                    taskFlat.getActivationArea())) {
                StateUtility.changeTaskState(this, taskFlat, TaskState.RUNNING);
            }
        }




        last = location;

        Intent i = new Intent();
        i.setAction(GEO_TASK_UPDATE_INTENT);
        sendBroadcast(i);
    }


    public static Location getLastLocation() {
        return last;
    }


}
