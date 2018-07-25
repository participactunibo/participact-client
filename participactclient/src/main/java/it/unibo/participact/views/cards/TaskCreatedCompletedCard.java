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

import java.util.Collection;

import it.unibo.participact.R;
import it.unibo.participact.domain.enums.SensingActionEnum;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.rest.ActionFlat;
import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.support.StringUtils;

/**
 * Created by alessandro on 10/11/14.
 */
public class TaskCreatedCompletedCard extends Card {

    private TaskFlat task;
    private String size;

    public TaskCreatedCompletedCard(TaskFlat task, String size) {
        super(task.getName(), task.getDescription());
        this.task = task;
        this.size = size;
    }

    @Override
    public View getCardContent(Context context) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.card_task_created_completed, null);

        TextView titleTextView = (TextView) view.findViewById(R.id.title);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.description);

        titleTextView.setText(title);
        descriptionTextView.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_descrizione), desc));
        descriptionTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));

        Collection<ActionFlat> actions = task.getActions();
        TextView sensors = ((TextView) view.findViewById(R.id.sensors));
        String sensorsList = "";
        boolean camera = false;
        boolean questionario = false;

        for (ActionFlat actionFlat : actions) {
            if (actionFlat.getType() == ActionType.SENSING_MOST) {
                sensorsList += SensingActionEnum.Type.fromIntToHumanReadable(actionFlat.getInput_type().intValue()).toString() + " ";
            }

            if (actionFlat.getType() == ActionType.PHOTO && !camera) {
                sensorsList += "Fotocamera ";
                camera = true;
            }

            if (actionFlat.getType() == ActionType.QUESTIONNAIRE && !questionario) {
                sensorsList += "Questionario ";
                questionario = true;
            }
        }

        sensors.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_sensori), sensorsList));
        sensors.setTextColor(context.getResources().getColor(R.color.secondary_text));

        TextView durationTextView = (TextView) view.findViewById(R.id.duration);
        durationTextView.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_durata), task.getDuration().toString()));
        durationTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));

        TextView completedUserNumTextView = (TextView) view.findViewById(R.id.completed_user_num);
        completedUserNumTextView.setText(StringUtils.formatForTextView("Completato da: ", size + " utenti"));
        durationTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));


        return view;
    }
}
