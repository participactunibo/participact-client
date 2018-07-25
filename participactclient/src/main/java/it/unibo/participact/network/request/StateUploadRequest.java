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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskStatus;
import it.unibo.participact.domain.persistence.support.State;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.preferences.DataUploaderStatePreferences;

public class StateUploadRequest extends ApacheHttpSpiceRequest<ResponseMessage> {

    private static final Logger logger = LoggerFactory.getLogger(StateUploadRequest.class);

    private final static String RELATIVE_URL = "log";
    private final static String KEY = "stateRequestKey";

    public StateUploadRequest(Context context) {
        super(context, ResponseMessage.class);
    }

    @Override
    public ResponseMessage loadDataFromNetwork() throws Exception {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ITALIAN);
        String fileName = formatter.format(new Date());

        HttpClient httpClient = getHttpClient();
        HttpPost httppost = getHttpPost(Configuration.RESULT_DATA_URL + RELATIVE_URL + "/" + fileName + "_state");

        State state = StateUtility.loadState(context);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
        PrintWriter print = new PrintWriter(gzipOut);
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
        print.close();

        byte[] logBytes = baos.toByteArray();
        ByteArrayEntity entity = new ByteArrayEntity(logBytes);
        httppost.setEntity(entity);

        logger.info("Executing request {}.", httppost.getRequestLine());
        HttpResponse response = httpClient.execute(httppost);
        logger.info("Response: {}", response.getStatusLine());

        ResponseMessage responseMes = new ObjectMapper().readValue(response.getEntity().getContent(), ResponseMessage.class);
        logger.info("Result: {} key {}", responseMes.getResultCode(), responseMes.getKey());

        switch (responseMes.getResultCode()) {
            case ResponseMessage.RESULT_OK:
                logger.info("State successfully uploaded");
                DataUploaderStatePreferences.getInstance(context).setStateUpload(false);
                break;
            default:
                break;
        }

        return responseMes;
    }

    @Override
    public String getKey() {
        return KEY;
    }

}
