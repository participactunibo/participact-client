/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.network.request;


import android.content.Context;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.rest.TaskFlatMap;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

/**
 * Created by alessandro on 10/11/14.
 */
public class CreatedTaskByStateRequest extends SpringAndroidSpiceRequest<TaskFlatMap> {

    private final static Logger logger = LoggerFactory.getLogger(AvailableTaskRequest.class);

    private TaskState state;
    private Context context;


    public CreatedTaskByStateRequest(Context context, TaskState state) {
        super(TaskFlatMap.class);
        this.state = state;
        this.context = context;
    }

    @Override
    public TaskFlatMap loadDataFromNetwork() throws Exception {
        logger.info("Executing GET created task request {} state {}.", Configuration.CREATED_TASK_STATE_URL, state);
        ResponseEntity<TaskFlatMap> response = getRestTemplate().exchange(Configuration.CREATED_TASK_STATE_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), TaskFlatMap.class, state.toString());
        return response.getBody();
    }


    public TaskState getState() {
        return state;
    }


    public Context getContext() {
        return context;
    }

    public String createCacheKey() {
        return String.format("getCreatedTask%s", state.toString());
    }

}
