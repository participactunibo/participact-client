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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;

import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskStatus;
import it.unibo.participact.domain.persistence.support.State;

public class StateLogBroadcastReceiver extends BroadcastReceiver {

    private final static Logger logger = LoggerFactory.getLogger(StateLogBroadcastReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        State state = StateUtility.loadState(context);
        if (state != null) {

            logger.info("STATUS");
            for (Entry<Long, TaskStatus> task : state.getTasks().entrySet()) {

                logger.info(String.format("Task id: %d", task.getKey()));
                logger.info(String.format("Task state: %s", task.getValue().getState().toString()));
                if (task.getValue().getAcceptedTime() == null) {
                    logger.info(String.format("Accepted Time: not accepted"));
                } else {
                    logger.info(String.format("Accepted Time: %s", task.getValue().getAcceptedTime().toString()));
                }
                logger.info(String.format("Last checked timestamp: %s", new DateTime(task.getValue().getLastCheckedTimestamp())));
                logger.info(String.format("Sensing progress: %d", task.getValue().getSensingProgress()));
                logger.info(String.format("Photo progress: %d", task.getValue().getPhotoProgress()));
                logger.info(String.format("Questionnaire progress: %d", task.getValue().getQuestionnaireProgress()));
                logger.info("--------------------------");

            }
            logger.info("END STATUS");

        } else {
            logger.info("NO STATUS");
        }

    }

}
