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

import android.content.Context;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.LoginUtility;

public class CompleteTaskListener implements RequestListener<ResponseMessage> {

    private final static Logger logger = LoggerFactory.getLogger(CompleteTaskListener.class);


    private Context context;
    private TaskFlat task;

    public CompleteTaskListener(Context context, TaskFlat task) {
        this.task = task;
        this.context = context;
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        LoginUtility.checkIfLoginException(context, spiceException);
        logger.warn("Exception uploading the final state of the task with id {}.", task.getId(), spiceException);
    }

    @Override
    public void onRequestSuccess(ResponseMessage result) {
        if (result.getResultCode() == ResponseMessage.RESULT_OK) {
            logger.info("Successfully uploaded the final state of the task with id {}.", task.getId());
            StateUtility.removeTask(context, task);
        }

    }

}
