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

import it.unibo.participact.ParticipActService;
import it.unibo.participact.support.preferences.ChangeTimePreferences;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {

    private final static Logger logger = LoggerFactory.getLogger(ShutdownBroadcastReceiver.class);


    @Override
    public void onReceive(Context context, Intent intent) {

        logger.info("Shutdown.");

        ChangeTimePreferences.getInstance(context).setLastCurrentMillisChecked(0);
        ChangeTimePreferences.getInstance(context).setLastElapsedChecked(0);

        Intent i = new Intent(context, ParticipActService.class);
        i.setAction(ParticipActService.STOP);
        context.startService(i);
    }

}
