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

import it.unibo.participact.services.PendingActiveTaskIntentService;

public class CheckPendingActionsBroadcastReceiver extends BroadcastReceiver {

    public static final String CHECK_PENDING_ACTIONS = "it.unibo.participact.CHECK_PENDING_ACTIONS";
    private static final String TASK_ID = "TASK_ID";
    public static final int CHECK_PENDING_ACTIONS_REQUEST_CODE = 98;

    public CheckPendingActionsBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        long taskId = intent.getLongExtra(TASK_ID, -1);

        if (taskId > 0)
            PendingActiveTaskIntentService.startAddPendingNotifications(context, taskId);

    }
}
