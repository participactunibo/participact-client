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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import org.most.MoSTService;
import org.most.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.activities.TimeWarningActivity;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.ActionFlat;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.persistence.support.State;
import it.unibo.participact.services.NetworkService;
import it.unibo.participact.support.NotificationUtility;
import it.unibo.participact.support.preferences.ChangeTimePreferences;

public class ProgressBroadcastReceiver extends BroadcastReceiver {

    private static final Logger logger = LoggerFactory.getLogger(ProgressBroadcastReceiver.class);
    private static final String TAG = ProgressBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // aggiorno progresso dei task di sensing
        StateUtility.incrementSensingProgress(context);

        // ping a most
        Log.i(TAG, "Ping to MoST, last timestamp: "
                + MoSTPingBroadcastReceiver.moSTlastResponsePing);

        State state = StateUtility.loadState(context);
        List<TaskFlat> tasks = state.getTaskByState(TaskState.RUNNING);

        HashMap<Pipeline.Type, Integer> runningPipeline = new HashMap<Pipeline.Type, Integer>();

        for (TaskFlat taskFlat : tasks) {
            for (ActionFlat actionFlat : taskFlat.getActions()) {
                if (actionFlat.getType() == ActionType.SENSING_MOST) {
                    Pipeline.Type pipeline = Pipeline.Type.fromInt(actionFlat.getInput_type());
                    if (runningPipeline.containsKey(pipeline)) {
                        int count = runningPipeline.get(pipeline);
                        runningPipeline.put(pipeline, count + 1);
                    } else {
                        runningPipeline.put(pipeline, 1);
                    }
                }

            }
        }


        Intent i = new Intent(context, MoSTService.class);
        i.setAction(MoSTService.PING);
        i.putExtra(MoSTService.KEY_PARTICIPACT_STATE, runningPipeline);
        context.startService(i);

        long lastCurrent = ChangeTimePreferences.getInstance(context).getLastCurrentMillisChecked();
        long lastElapsed = ChangeTimePreferences.getInstance(context).getLastElapsedChecked();

        if (lastCurrent != 0 && lastElapsed != 0
                && !ChangeTimePreferences.getInstance(context).getChangeTimeRequest()) {

            long diffCurrent = Math.abs(System.currentTimeMillis() - lastCurrent);
            long diffElapsed = Math.abs(SystemClock.elapsedRealtime() - lastElapsed);

            if (Math.abs(diffCurrent - diffElapsed) > NetworkService.CHANGE_TIME_THRESHOLD) {
                logger.warn("Freeze task. DiffCurrent {}, diffElapsed {}", diffCurrent, diffElapsed);

                ChangeTimePreferences.getInstance(context).setChangeTimeRequest(true);
                StateUtility.freezeAllTask(context);

                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notificationIntent = new Intent(context, TimeWarningActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationUtility.addNotification(context, R.drawable.ic_time_error, context.getString(R.string.participact_compromised), context.getString(R.string.date_time_changed), GcmBroadcastReceiver.NOTIFICATION_TIME_ERR, contentIntent);

            }

        }

        // update current millis and elapsed in pref
        ChangeTimePreferences.getInstance(context).setLastCurrentMillisChecked(
                System.currentTimeMillis());
        ChangeTimePreferences.getInstance(context).setLastElapsedChecked(
                SystemClock.elapsedRealtime());

        // if set check time with ntp server
        if (ChangeTimePreferences.getInstance(context).getChangeTimeRequest()) {
            Intent netIntent = new Intent(context, NetworkService.class);
            netIntent.setAction(NetworkService.CHECK_TIME_ACTION);
            context.startService(netIntent);
        }

    }

}
