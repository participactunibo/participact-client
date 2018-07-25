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

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.preferences.DataUploaderLogPreferences;

public class LogUploadRequestListener implements RequestListener<ResponseMessage> {

    private final static Logger logger = LoggerFactory.getLogger(LogUploadRequestListener.class);

    Context context;

    public LogUploadRequestListener(Context context) {
        this.context = context;
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        logger.warn("Failed to upload log file to server.", spiceException);
    }

    @Override
    public void onRequestSuccess(ResponseMessage result) {
        if (result != null && result.getResultCode() == ResponseMessage.RESULT_OK) {
            DataUploaderLogPreferences.getInstance(context).setLogUpload(false);
            logger.info("Successfully uploaded log file to server.");
        }
    }

}
