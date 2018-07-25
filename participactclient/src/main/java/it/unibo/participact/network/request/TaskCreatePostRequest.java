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

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.domain.rest.TaskFlatRequest;
import it.unibo.participact.support.Configuration;

/**
 * Created by alessandro on 24/11/14.
 */
public class TaskCreatePostRequest extends SpringAndroidSpiceRequest<TaskFlat> {

    private TaskFlatRequest newTaskFlatRequest;

    public TaskCreatePostRequest(TaskFlatRequest newTaskFlatRequest) {
        super(TaskFlat.class);
        this.newTaskFlatRequest = newTaskFlatRequest;
    }

    @Override
    public TaskFlat loadDataFromNetwork() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(new MediaType("application", "json"));

        //mock taskflatrequest

        //this.newTaskFlatRequest = mockTaskflatRequest();
        // TaskFlatRequest prova = mockProva();
        HttpEntity<TaskFlatRequest> requestEntity = new HttpEntity<TaskFlatRequest>(newTaskFlatRequest, requestHeaders);

        RestTemplate restTemplate = getRestTemplate();

        // Add the Jackson and String message converters
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        // Make the HTTP POST request, marshaling the request to JSON, and the response to a TaskFlat
        ResponseEntity<TaskFlat> responseEntity = restTemplate.exchange(Configuration.CREATE_TASK_URL, HttpMethod.POST, requestEntity, TaskFlat.class);
        return responseEntity.getBody();
    }


}
