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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.persistence.support.State;
import it.unibo.participact.support.AlarmStateUtility;

public class AlarmBroadcastReceiver extends BroadcastReceiver implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(AlarmBroadcastReceiver.class);

    private static final long serialVersionUID = -2289366968646749313L;

    private long taskId;

    public AlarmBroadcastReceiver(long taskId) {
        this.taskId = taskId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        long taskId = intent.getExtras().getLong(AlarmStateUtility.KEY_TASK);
        logger.info("Received resume alarm for task {}.", taskId);
        State state = StateUtility.loadState(context);

        if (state != null) {
            TaskFlat task = state.getTaskById(taskId).getTask();
            StateUtility.changeTaskState(context, task, TaskState.RUNNING);
            AlarmStateUtility.removeAlarm(context.getApplicationContext(), taskId);
        }

    }

    @Override
    public int hashCode() {
        return (int) taskId;
    }

}
