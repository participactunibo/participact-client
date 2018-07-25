/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;

import it.unibo.participact.ParticipActService;
import it.unibo.participact.R;
import it.unibo.participact.activities.DashboardActivity;
import it.unibo.participact.broadcastreceivers.GcmBroadcastReceiver;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.domain.rest.TaskFlatList;
import it.unibo.participact.network.request.AcceptTaskRequest;
import it.unibo.participact.network.request.AvailableTaskRequest;
import it.unibo.participact.network.request.CheckClientAppVersionRequest;
import it.unibo.participact.network.request.NotificationAcceptMandatoryTaskListener;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.GeolocalizationTaskUtils;
import it.unibo.participact.support.NotificationUtility;
import it.unibo.participact.support.SntpClient;
import it.unibo.participact.support.preferences.ChangeTimePreferences;

public class NetworkService extends IntentService {

    private final static Logger logger = LoggerFactory.getLogger(NetworkService.class);
    private SpiceManager contentManager = new SpiceManager(ParticipactSpringAndroidService.class);

    public final static String CHECK_TIME_ACTION = "it.unibo.participact.CHECK_TIME";
    public final static String CHECK_TASK_FROM_GCM_ACTION = "it.unibo.participact.CHECK_TASK_FROM_GCM";
    public final static String CHECK_CLIENT_APP_VERSION = "it.unibo.participact.CHECK_CLIENT_APP_VERSION";
    public final static String CHECK_COLLECTED_GEOBADGE_TO_SYNC = "it.unibo.participact.CHECK_COLLECTED_GEOBADGE_TO_SYNC";
    public final static long CHANGE_TIME_THRESHOLD = 1000 * 60 * 15;

    NotificationManager mNotificationManager;

    public NetworkService() {
        super(NetworkService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction().equals(CHECK_TIME_ACTION)) {
            SntpClient client = new SntpClient();
            if (client.requestTime("time.windows.com", 2000)) {
                long ntp = client.getNtpTime() + SystemClock.elapsedRealtime()
                        - client.getNtpTimeReference();
                long diff = Math.abs(System.currentTimeMillis() - ntp);

                logger.info(
                        "Checked time with ntp server. Ntp time = {}, System time = {}, diff = {}.",
                        ntp, System.currentTimeMillis(), diff);

                if (diff < CHANGE_TIME_THRESHOLD) {
                    ChangeTimePreferences.getInstance(this).setLastCurrentMillisChecked(
                            System.currentTimeMillis());
                    ChangeTimePreferences.getInstance(this).setLastElapsedChecked(
                            SystemClock.elapsedRealtime());

                    StateUtility.defreezeAllTask(this);
                    ChangeTimePreferences.getInstance(this).setChangeTimeRequest(false);
                    mNotificationManager = (NotificationManager) this
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(GcmBroadcastReceiver.NOTIFICATION_TIME_ERR);

                    NotificationUtility.addNotification(NetworkService.this, R.drawable.ic_stat_ok, getString(R.string.participact_notification), getString(R.string.time_restore_success), GcmBroadcastReceiver.NOTIFICATION_TIME_OK);

                }
            }
        }

        if (intent.getAction().equals(CHECK_TASK_FROM_GCM_ACTION)) {

            logger.info("Check task by gcm.");

            if (!contentManager.isStarted()) {
                contentManager.start(this);
            }
            AvailableTaskRequest request = new AvailableTaskRequest(this, TaskState.AVAILABLE, AvailableTaskRequest.ALL);
            contentManager.execute(request, new NotificationTaskRequestListener(this));
        }
        if (intent.getAction().equals(CHECK_COLLECTED_GEOBADGE_TO_SYNC)) {

            logger.info("Check sync collected badge with server");
            StateUtility.updateCollectedgeobadgeNotSync(this);

        }

        if (intent.getAction().equals(CHECK_CLIENT_APP_VERSION)) {

            logger.info("Check app version.");

            if (!contentManager.isStarted()) {
                contentManager.start(this);
            }
            CheckClientAppVersionRequest request = new CheckClientAppVersionRequest(this);
            contentManager.execute(request, new CheckClientAppVersionRequestListener());
        }

    }

    private void sendNewClientAppVersionNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=it.unibo.participact"));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

        NotificationUtility.addNotification(NetworkService.this, R.drawable.ic_login_err, getString(R.string.new_app_update), getString(R.string.tap_to_update), GcmBroadcastReceiver.NOTIFICATION_NEW_VERSION, contentIntent);
    }

    private class NotificationTaskRequestListener implements RequestListener<TaskFlatList> {

        Context context;

        public NotificationTaskRequestListener(Context context) {
            this.context = context;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            if (spiceException.getCause() instanceof HttpMessageNotReadableException) {
                sendNewClientAppVersionNotification();
            }
        }

        @Override
        public void onRequestSuccess(TaskFlatList result) {

            for (TaskFlat task : result.getList()) {
                if (!task.getCanBeRefused() && !GeolocalizationTaskUtils.isNotifiedByArea(task)) {

                    AcceptTaskRequest request = new AcceptTaskRequest(context, task.getId());

                    if (!contentManager.isStarted()) {
                        contentManager.start(context);
                    }

                    contentManager.execute(request, new NotificationAcceptMandatoryTaskListener(context, task));

                } else if (GeolocalizationTaskUtils.isNotifiedByArea(task)) {
                    it.unibo.participact.domain.persistence.TaskFlat taskDB = StateUtility.getTaskById(context, task.getId());
                    if (taskDB == null) {
                        taskDB = StateUtility.addTask(context, task);
                        if (taskDB != null) {
                            // state in hidden
                            StateUtility.changeTaskState(context, taskDB, TaskState.HIDDEN);
                        }
                    }

                    if (StateUtility.getTaskByState(context, TaskState.HIDDEN).size() > 0) {
                        Location last = ParticipActService.getLastLocation();
                        if (last != null) {
                            for (it.unibo.participact.domain.persistence.TaskFlat hiddenTask : StateUtility.getTaskByState(context, TaskState.HIDDEN)) {
                                if (!hiddenTask.getCanBeRefused() && GeolocalizationTaskUtils.isInside(context, last.getLongitude(), last.getLatitude(), hiddenTask.getNotificationArea())) {
                                    AcceptTaskRequest request = new AcceptTaskRequest(context, task.getId());

                                    if (!contentManager.isStarted()) {
                                        contentManager.start(context);
                                    }

                                    contentManager.execute(request, new it.unibo.participact.network.NotificationAcceptMandatoryTaskListener(context, hiddenTask));

                                }
                            }
                        }
                    }

                } else {

                    Intent resultIntent = new Intent(NetworkService.this, DashboardActivity.class);

                    resultIntent.setAction(DashboardActivity.GO_TO_TASK_AVAILABLE_FRAGMENT);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(NetworkService.this);
                    stackBuilder.addParentStack(DashboardActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );


                    NotificationUtility.addNotification(NetworkService.this, R.drawable.ic_new_task, getString(R.string.participact_notification), getString(R.string.new_tasks_notification), GcmBroadcastReceiver.NOTIFICATION_NEW_TASK, resultPendingIntent);

                }
            }

        }
    }

    private class CheckClientAppVersionRequestListener implements RequestListener<Integer> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
        }

        @Override
        public void onRequestSuccess(Integer result) {
            if (result != null) {
                if (result > Configuration.VERSION) {
                    sendNewClientAppVersionNotification();
                }
            }
        }
    }

}
