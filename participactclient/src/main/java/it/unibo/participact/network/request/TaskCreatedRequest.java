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

import it.unibo.participact.domain.enums.TaskValutation;
import it.unibo.participact.domain.rest.TaskFlatList;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

/**
 * Created by alessandro on 11/11/14.
 */
public class TaskCreatedRequest extends SpringAndroidSpiceRequest<TaskFlatList> {


    private Context context;
    private TaskValutation valutation;

    private final static Logger logger = LoggerFactory.getLogger(AvailableTaskRequest.class);


    public TaskCreatedRequest(Context context, TaskValutation valutation) {
        super(TaskFlatList.class);
        this.context = context;
        this.valutation = valutation;
    }

    @Override
    public TaskFlatList loadDataFromNetwork() throws Exception {
        logger.info("Executing GET task request {} state {}.", Configuration.CREATED_TASK_URL, valutation);
        ResponseEntity<TaskFlatList> response = getRestTemplate().exchange(Configuration.CREATED_TASK_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), TaskFlatList.class, valutation.toString());
        return response.getBody();


    }

    public String createCacheKey() {
        return String.format("getCreatedTask%s", valutation.toString());
    }

}
