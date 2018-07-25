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

import it.unibo.participact.support.preferences.UserAccountPreferences;

public class GCMRegisterRequestListener implements RequestListener<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(GCMRegisterRequestListener.class);

    Context context;

    public GCMRegisterRequestListener(Context context) {
        this.context = context;
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        logger.warn("GCM registration on server failed.");
    }

    @Override
    public void onRequestSuccess(Boolean result) {
        if (result == null) {
            return;
        }
        if (result) {
            UserAccountPreferences.getInstance(context).setGCMSetOnServer(true);
            logger.info("GCM registration on server ok.");
        }
    }
}