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

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import it.unibo.participact.domain.persistence.DataQuestionnaireFlat;
import it.unibo.participact.domain.persistence.support.DomainDBHelper;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.ContentValuesToProto;
import it.unibo.participact.support.preferences.DataUploaderQuestionnairePreferences;

public class DataQuestionnaireFlatUploadRequest extends ApacheHttpSpiceRequest<ResponseMessage> {

    private static final Logger logger = LoggerFactory.getLogger(DataQuestionnaireFlatUploadRequest.class);

    private final static String RELATIVE_URL = "question";

    String key;
    List<DataQuestionnaireFlat> data;
    Context context;

    public DataQuestionnaireFlatUploadRequest(Context context, List<DataQuestionnaireFlat> data) {
        super(context, ResponseMessage.class);
        this.data = data;
        this.context = context;
    }

    @Override
    public ResponseMessage loadDataFromNetwork() throws Exception {

        HttpClient httpClient = getHttpClient();
        HttpPost httppost = getHttpPost(Configuration.RESULT_DATA_URL + RELATIVE_URL);

        ByteArrayEntity entity = new ByteArrayEntity(ContentValuesToProto.convertToDataQuestionnaireFlatProto(data).toByteArray());

        // Log.i("Sending:", new String(dataList.toByteArray()) +" with key: " +
        // getKey());
        httppost.setEntity(entity);

        logger.info("Executing request {}.", httppost.getRequestLine());
        HttpResponse response = httpClient.execute(httppost);
        logger.info("Response: {}", response.getStatusLine());

        ResponseMessage responseMes = new ObjectMapper().readValue(response.getEntity().getContent(), ResponseMessage.class);
        logger.info("Result: {} key {}", responseMes.getResultCode(), responseMes.getKey());

        int deleted = 0;
        DomainDBHelper dbHelper;
        try {

            dbHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<DataQuestionnaireFlat, Long> dao = dbHelper.getRuntimeExceptionDao(DataQuestionnaireFlat.class);

            switch (responseMes.getResultCode()) {
                case ResponseMessage.RESULT_OK:
                    // delete
                    logger.info("Deleting questionnaire.");
                    deleted = dao.delete(data);
                    break;
                case ResponseMessage.DATA_ALREADY_ON_SERVER:
                    logger.info("Deleting questionnaire.");
                    deleted = dao.delete(data);
                    break;
                case ResponseMessage.DATA_NOT_REQUIRED:
                    logger.info("Deleting questionnaire.");
                    deleted = dao.delete(data);
                    break;
                default:
                    break;
            }
        } finally {
            OpenHelperManager.releaseHelper();
        }
        DataUploaderQuestionnairePreferences.getInstance(context).setQuestionnaireUpload(false);
        logger.info("Upload request completed, Successfully deleted {} tuples.", deleted);
        return responseMes;
    }

}
