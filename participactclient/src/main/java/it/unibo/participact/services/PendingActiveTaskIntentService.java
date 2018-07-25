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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import java.util.Collection;

import it.unibo.participact.R;
import it.unibo.participact.activities.DashboardActivity;
import it.unibo.participact.broadcastreceivers.CheckPendingActionsBroadcastReceiver;
import it.unibo.participact.domain.persistence.ActionFlat;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.persistence.TaskStatus;
import it.unibo.participact.domain.persistence.support.State;
import it.unibo.participact.support.NotificationUtility;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PendingActiveTaskIntentService extends IntentService {

    private static final String ScheduleReceiver = "it.unibo.participact.support.action.ScheduleReceiver";
    private static final String AddPendingNotifications = "it.unibo.participact.support.action.AddPendingNotifications";
    public static final int NOTIFICATION_ID = 97;

    private static final String TASK_ID = "TASK_ID";

    /**
     * Starts this service to perform action ScheduleReceiver with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionScheduleReceiver(Context context, long taskId) {
        Intent intent = new Intent(context, PendingActiveTaskIntentService.class);
        intent.putExtra(TASK_ID, taskId);
        intent.setAction(ScheduleReceiver);
        context.startService(intent);
    }

    public static void startAddPendingNotifications(Context context, long taskId) {
        Intent intent = new Intent(context, PendingActiveTaskIntentService.class);
        intent.putExtra(TASK_ID, taskId);
        intent.setAction(AddPendingNotifications);
        context.startService(intent);
    }


    public PendingActiveTaskIntentService() {
        super("PendingActiveTaskIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ScheduleReceiver.equals(action)) {
                long taskId = intent.getLongExtra(TASK_ID, -1);
                if (taskId >= 0)
                    handleActionScheduleReceiver(taskId);
            } else if (AddPendingNotifications.equals(intent.getAction())) {
                long taskId = intent.getLongExtra(TASK_ID, -1);
                if (taskId >= 0)
                    handleActionAddPendingNotification(taskId);
            }
        }
    }

    /**
     * Handle action ActionAddPendingActiveTasNotification in the provided background thread with the provided
     * parameters.
     */
    private void handleActionScheduleReceiver(long taskId) {

        State state = StateUtility.loadState(this);

        if (state == null)
            return;

        TaskStatus status = state.getTaskById(taskId);

        if (status == null)
            return;

        TaskFlat task = status.getTask();

        if (task == null)
            return;

        //86 400 000 = 1 day taskDuration is in minutes
        long now = System.currentTimeMillis();
        long notificationTime = now + (task.getDuration() * 60000) - 86400000;

        if (notificationTime > now + 300000) {//300000 = 5 minutes, don't sent notification if the range between accept and deadline is less than 5 minutes
            Intent intent = new Intent();
            intent.setAction(CheckPendingActionsBroadcastReceiver.CHECK_PENDING_ACTIONS);
            intent.putExtra(TASK_ID, taskId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, CheckPendingActionsBroadcastReceiver.CHECK_PENDING_ACTIONS_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }

    }

    private void handleActionAddPendingNotification(long taskId) {

        if (taskId < 0)
            return;

        State state = StateUtility.loadState(this);

        if (state == null)
            return;

        TaskStatus status = state.getTaskById(taskId);

        if (status == null)
            return;

        Collection<ActionFlat> actions = status.getTask().getActions();

        if (actions == null)
            return;

        boolean toSendPhoto = false;
        boolean toSendQuest = false;

        for (ActionFlat currentFlat : actions) {

            if (currentFlat.getType() == ActionType.PHOTO) {

                if (status.getRemainingPhotoPerAction(currentFlat.getId()) > 0) {
                    toSendPhoto = true;
                }

            } else if (currentFlat.getType() == ActionType.QUESTIONNAIRE) {

                if (!status.isQuestionnaireCompleted(currentFlat.getId())) {
                    toSendQuest = true;
                }
            }
        }

        String notificationText = "";

        if (toSendPhoto && toSendQuest)
            notificationText = getString(R.string.pending_task_photo_and_questionnaire);

        else if (toSendPhoto)
            notificationText = getString(R.string.pending_task_photo);

        else if (toSendQuest)
            notificationText = getString(R.string.pending_task_questionnaire);


        if (toSendPhoto || toSendQuest) {

            Intent resultIntent = new Intent(this, DashboardActivity.class);

            resultIntent.setAction(DashboardActivity.GO_TO_TASK_ACTIVE_FRAGMENT);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(DashboardActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            NotificationUtility.addNotification(this, R.drawable.ic_new_task, status.getTask().getName(), notificationText, NOTIFICATION_ID, resultPendingIntent);

        }
    }
}
