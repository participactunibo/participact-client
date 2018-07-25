/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.views.cards;

import android.content.Context;

import it.unibo.participact.R;
import it.unibo.participact.support.ColorUtility;

public class TaskCompletedFirstCard extends MyPlayCard {


    private static final String TITLE = "Task Completati";
    private static final String DESCRIPTION = "Questa scheda riassume le informazioni sui task completati con successo dall'utente.";
    private static final String COLOR = "#f2a400";
    private static final String TITLE_COLOR = "#f2a400";
    private static final boolean HAS_OVERFLOW = false;
    private static final boolean IS_CLICKABLE = false;


    public TaskCompletedFirstCard(Context context) {
        super(TITLE, DESCRIPTION, ColorUtility.parseColorFromId(context, R.color.primary_dark), ColorUtility.parseColorFromId(context, R.color.primary_dark), HAS_OVERFLOW, IS_CLICKABLE);
    }


}
