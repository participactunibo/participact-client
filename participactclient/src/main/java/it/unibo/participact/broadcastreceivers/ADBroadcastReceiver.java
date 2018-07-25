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
import android.content.SharedPreferences.Editor;

import org.most.MoSTService;
import org.most.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.participact.views.cards.TaskActiveCard;

public class ADBroadcastReceiver extends BroadcastReceiver {

    private static Logger logger = LoggerFactory.getLogger(ADBroadcastReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, MoSTService.class);
        i.setAction(MoSTService.STOP);
        i.putExtra(MoSTService.KEY_PIPELINE_TYPE, Pipeline.Type.ACTIVITY_RECOGNITION_COMPARE.toInt());//TODO change
        context.startService(i);

        // reset btn state
        Editor editor = context.getSharedPreferences(TaskActiveCard.ACTIVITY_DETECTION_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(TaskActiveCard.KEY_AD_SELECTED, -1);
        editor.apply();
        logger.info("Activity Detection stopped by alarm.");

    }

}
