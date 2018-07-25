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
import it.unibo.participact.domain.rest.TaskFlatList;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

public class AvailableTaskRequest extends SpringAndroidSpiceRequest<TaskFlatList> {

    private final static Logger logger = LoggerFactory.getLogger(AvailableTaskRequest.class);

    public static final String ADMIN = "admin";
    public static final String USER = "user";
    public static final String ALL = "all";


    private TaskState state;
    private Context context;
    private String type;

    public AvailableTaskRequest(Context context, TaskState state, String type) {
        super(TaskFlatList.class);
        this.context = context;
        this.state = state;
        this.type = type;
    }

    @Override
    public TaskFlatList loadDataFromNetwork() throws Exception {
        logger.info("Executing GET task request {} state {}.", Configuration.TASK_URL, state);

        ResponseEntity<TaskFlatList> response = getRestTemplate().exchange(Configuration.TASK_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), TaskFlatList.class, type, state.toString());
        return response.getBody();
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public String createCacheKey() {
        return String.format("getTask%s%s", type, state.toString());
    }
}

