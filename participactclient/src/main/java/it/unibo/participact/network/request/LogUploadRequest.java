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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.Configuration;

public class LogUploadRequest extends ApacheHttpSpiceRequest<ResponseMessage> {

    private static final Logger logger = LoggerFactory.getLogger(LogUploadRequest.class);

    private final static String RELATIVE_URL = "log";
    private final static String KEY = "logRequestKey";

    private String logFilename;

    public LogUploadRequest(Context context, String filename) {
        super(context, ResponseMessage.class);
        this.logFilename = filename;
    }

    @Override
    public ResponseMessage loadDataFromNetwork() throws Exception {

        File file = new File(context.getExternalFilesDir(null) + "/" + logFilename + ".log");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ITALIAN);
        String fileName = formatter.format(new Date(file.lastModified()));

        HttpClient httpClient = getHttpClient();
        HttpPost httppost = getHttpPost(Configuration.RESULT_DATA_URL + RELATIVE_URL + "/" + fileName + "_" + logFilename);

        byte[] logBytes = compress(context.getExternalFilesDir(null) + "/" + logFilename + ".log");
        ByteArrayEntity entity = new ByteArrayEntity(logBytes);
        httppost.setEntity(entity);

        logger.info("Executing request {}.", httppost.getRequestLine());
        HttpResponse response = httpClient.execute(httppost);
        logger.info("Response: {}", response.getStatusLine());

        ResponseMessage responseMes = new ObjectMapper().readValue(response.getEntity().getContent(), ResponseMessage.class);
        logger.info("Result: {} key {}", responseMes.getResultCode(), responseMes.getKey());

        switch (responseMes.getResultCode()) {
            case ResponseMessage.RESULT_OK:
                logger.info("Log {} successfully uploaded", logFilename);
                break;
            default:
                break;
        }

        return responseMes;
    }

    @Override
    public String getKey() {
        return KEY + logFilename;
    }

    public static byte[] compress(String path) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(os);
        FileInputStream fi = new FileInputStream(path);
        int buffer = 2048;
        byte data[] = new byte[buffer];
        BufferedInputStream origin = new BufferedInputStream(fi, buffer);
        int count;
        while ((count = origin.read(data, 0, buffer)) != -1) {
            gos.write(data, 0, count);
        }
        origin.close();
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return compressed;
    }

}
