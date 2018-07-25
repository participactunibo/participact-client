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

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import it.unibo.participact.domain.persistence.TaskStatus;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

public class CompleteWithFailureTaskRequest extends SpringAndroidSpiceRequest<ResponseMessage> {

    private Context context;
    private TaskStatus taskStatus;


    public CompleteWithFailureTaskRequest(Context context, TaskStatus taskStatus) {
        super(ResponseMessage.class);
        this.context = context;
        this.taskStatus = taskStatus;
    }

    @Override
    public ResponseMessage loadDataFromNetwork() throws Exception {
        ResponseEntity<ResponseMessage> response = getRestTemplate().exchange(Configuration.COMPLETE_WITH_FAILURE_TASK_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), ResponseMessage.class,
                taskStatus.getTask().getId(), taskStatus.getSensingProgress(), taskStatus.getPhotoProgress(), taskStatus.getQuestionnaireProgress());
        return response.getBody();
    }

    /**
     * This method generates a unique cache key for this request. In this case our cache key depends just on the
     * keyword.
     *
     * @return
     */
    public String createCacheKey() {
        return String.format("completeWithFailureTaskRequest.%s", taskStatus.getTask().getId());
    }
}

