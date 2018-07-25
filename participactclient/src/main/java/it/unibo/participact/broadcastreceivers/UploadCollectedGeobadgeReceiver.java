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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unibo.participact.services.NetworkService;

public class UploadCollectedGeobadgeReceiver extends BroadcastReceiver {

    private static final Logger logger = LoggerFactory
            .getLogger(UploadCollectedGeobadgeReceiver.class);

    public static final int DAILY_UPLOAD_COLLECTEDGEOBADGE_REQUEST_CODE = 100;
    public static final String DAILY_UPLOAD_COLLECTEDGEOBADGE_INTENT = "it.unibo.participact.DAILY_UPLOAD_COLLECTEDGEOBADGE_INTENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info("Received daily notification intent");
        Intent i = new Intent(context, NetworkService.class);
        i.setAction(NetworkService.CHECK_COLLECTED_GEOBADGE_TO_SYNC);
        context.startService(i);

    }
}
