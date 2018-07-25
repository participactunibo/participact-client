/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.participact.network.request.GCMRegisterRequest;
import it.unibo.participact.network.request.GCMRegisterRequestListener;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;

public class UpdateReceiver extends BroadcastReceiver {

    private static final Logger logger = LoggerFactory.getLogger(UpdateReceiver.class);
    private SpiceManager _contentManager = new SpiceManager(ParticipactSpringAndroidService.class);

    @Override
    public void onReceive(Context context, Intent intent) {

        GCMRegisterRequest request = new GCMRegisterRequest(context);
        String lastRequestCacheKey = request.createCacheKey();
        if (!_contentManager.isStarted()) {
            _contentManager.start(context);
        }
        _contentManager.execute(request, lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED, new GCMRegisterRequestListener(context));
        logger.info("Sending GCM id to server.");
    }

}
