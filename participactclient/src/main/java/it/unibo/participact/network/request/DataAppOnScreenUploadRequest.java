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

import android.content.ContentValues;
import android.content.Context;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.most.persistence.DBAdapter;
import org.most.pipeline.PipelineAppOnScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.ContentValuesToProto;

public class DataAppOnScreenUploadRequest extends ApacheHttpSpiceRequest<ResponseMessage> {

    private static final Logger logger = LoggerFactory.getLogger(DataAppOnScreenUploadRequest.class);

    private final static String RELATIVE_URL = "apponscreen";
    private final static String TABLE = PipelineAppOnScreen.TBL_APP_ON_SCREEN;

    String key;
    List<ContentValues> data;

    public DataAppOnScreenUploadRequest(Context context, List<ContentValues> data) {
        super(context, ResponseMessage.class);
        this.data = data;
    }

    @Override
    public ResponseMessage loadDataFromNetwork() throws Exception {

        HttpClient httpClient = getHttpClient();
        HttpPost httppost = getHttpPost(Configuration.RESULT_DATA_URL + RELATIVE_URL);
        ByteArrayEntity entity = new ByteArrayEntity(ContentValuesToProto.convertToDataAppOnScreenProto(data).toByteArray());

        // Log.i("Sending:", new String(dataList.toByteArray()) +" with key: " +
        // getKey());
        httppost.setEntity(entity);

        logger.info("Executing request {}.", httppost.getRequestLine());
        HttpResponse response = httpClient.execute(httppost);
        logger.info("Response: {}", response.getStatusLine());

        ResponseMessage responseMes = new ObjectMapper().readValue(response.getEntity().getContent(), ResponseMessage.class);
        logger.info("Result: {} key {}", responseMes.getResultCode(), responseMes.getKey());

        DBAdapter adapter = null;
        int deleted = 0;
        switch (responseMes.getResultCode()) {
            case ResponseMessage.RESULT_OK:
                // delete
                adapter = DBAdapter.getInstance(getContext());
                logger.info("Deleting tuples from {} table", TABLE);
                deleted = adapter.deleteTuples(TABLE, ContentValuesToProto.getIds(data));
                break;
            case ResponseMessage.DATA_ALREADY_ON_SERVER:
                adapter = DBAdapter.getInstance(getContext());
                logger.info("Deleting tuples from {} table", TABLE);
                deleted = adapter.deleteTuples(TABLE, ContentValuesToProto.getIds(data));
                break;
            case ResponseMessage.DATA_NOT_REQUIRED:
                adapter = DBAdapter.getInstance(getContext());
                logger.info("Deleting tuples from {} table", TABLE);
                deleted = adapter.deleteTuples(TABLE, ContentValuesToProto.getIds(data));
                break;
            default:
                break;
        }
        logger.info("Upload request completed, Successfully deleted {} tuples.", deleted);
        return responseMes;
    }

}
