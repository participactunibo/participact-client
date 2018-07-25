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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import it.unibo.participact.R;

public class NoTaskAvailableCard extends Card {

    private static String TITLE;


    public NoTaskAvailableCard() {
        super();
    }

    @Override
    public View getCardContent(Context context) {
        TITLE = context.getString(R.string.no_available_tasks);
        View view = LayoutInflater.from(context).inflate(R.layout.card_no_task_available, null);
        ((TextView) view.findViewById(R.id.title)).setText(TITLE);
        return view;
    }

}
