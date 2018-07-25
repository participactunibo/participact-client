/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.TaskStackBuilder;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.participact.ParticipActService;
import it.unibo.participact.R;
import it.unibo.participact.activities.DashboardActivity;
import it.unibo.participact.broadcastreceivers.GcmBroadcastReceiver;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.GeolocalizationTaskUtils;
import it.unibo.participact.support.LoginUtility;
import it.unibo.participact.support.NotificationUtility;

public class NotificationAcceptMandatoryTaskListener implements RequestListener<ResponseMessage> {

    private static final Logger logger = LoggerFactory.getLogger(NotificationAcceptMandatoryTaskListener.class);

    Context context;
    NotificationManager mNotificationManager;
    TaskFlat taskDB;

    public NotificationAcceptMandatoryTaskListener(Context context, TaskFlat task) {
        this.context = context;
        this.taskDB = task;
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        LoginUtility.checkIfLoginException(context, spiceException);
    }

    @Override
    public void onRequestSuccess(ResponseMessage result) {
        if (result != null && result.getResultCode() == 200) {
            if (taskDB != null) {
                //activate task
                Location last = ParticipActService.getLastLocation();
                if (GeolocalizationTaskUtils.isActivatedByArea(taskDB) && !GeolocalizationTaskUtils.isInside(context, last.getLongitude(), last.getLatitude(), taskDB.getActivationArea())) {
                    StateUtility.changeTaskState(context, taskDB, TaskState.RUNNING_BUT_NOT_EXEC);
                    logger.info("Activated mandatory task with id {} in RUNNING_BUT_NOT_EXEC because not in activation area.", taskDB.getId());
                } else {
                    StateUtility.changeTaskState(context, taskDB, TaskState.RUNNING);
                    logger.info("Activated mandatory task with id {} in RUNNING.", taskDB.getId());
                }

                Intent resultIntent = new Intent(context, DashboardActivity.class);

                resultIntent.setAction(DashboardActivity.GO_TO_TASK_ACTIVE_FRAGMENT);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(DashboardActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );


                NotificationUtility.addNotification(context, R.drawable.ic_new_task, context.getString(R.string.participact_notification), context.getString(R.string.new_task_accepted), GcmBroadcastReceiver.NOTIFICATION_NEW_TASK_ACCEPTED, resultPendingIntent);
            }
        }
    }

}