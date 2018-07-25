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
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import it.unibo.participact.ParticipActService;
import it.unibo.participact.R;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.persistence.TaskStatus;
import it.unibo.participact.domain.persistence.support.State;
import it.unibo.participact.fragments.ConsoleFragment;
import it.unibo.participact.support.AlarmStateUtility;
import it.unibo.participact.support.DisplayConvertionUtility;
import it.unibo.participact.support.GeolocalizationTaskUtils;

public class ConsoleCard extends Card implements OnCheckedChangeListener {

    View v;
    Typeface typeFace;
    ConsoleFragment consoleFragment;


    public ConsoleCard(ConsoleFragment consoleFragment, String titlePlay, String description, String color,
                       String titleColor, Boolean hasOverflow, Boolean isClickable) {
        super(titlePlay, description, color, titleColor, hasOverflow,
                isClickable);
        typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL);
        this.consoleFragment = consoleFragment;
    }

    @Override
    public View getCardContent(Context context) {
        v = LayoutInflater.from(context).inflate(R.layout.card_console, null);

        TextView title = (TextView) v.findViewById(R.id.title);
        TextView descriptionTextView = (TextView) v.findViewById(R.id.description);

        title.setText(titlePlay);
        title.setTextColor(Color
                .parseColor(titleColor));
        descriptionTextView.setText(description);
        descriptionTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
        ((ImageView) v.findViewById(R.id.stripe)).setBackgroundColor(Color
                .parseColor(color));

//			if (isClickable == true)
//				((LinearLayout) v.findViewById(R.id.contentLayout))
//						.setBackgroundResource(R.drawable.selectable_background_cardbank);

        State state = StateUtility.loadState(context);

        LinearLayout contentLayout = (LinearLayout) v.findViewById(R.id.contentLayout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) DisplayConvertionUtility.convertDpToPixel(8, context);
        params.topMargin = (int) DisplayConvertionUtility.convertDpToPixel(10, context);

        if (state != null && state.getTasks().size() > 0) {
            for (TaskStatus wrapper : state.getTasks().values()) {
                if (wrapper.getState() == TaskState.RUNNING || wrapper.getState() == TaskState.RUNNING_BUT_NOT_EXEC || wrapper.getState() == TaskState.SUSPENDED) {
                    Switch sw = new Switch(context);
                    sw.setLayoutParams(params);
                    sw.setTextOn(context.getString(R.string.enable));
                    sw.setTextOff(context.getString(R.string.disable));
                    sw.setTypeface(typeFace);
                    sw.setTextColor(context.getResources().getColor(R.color.secondary_text));
                    sw.setText(wrapper.getTask().getName());
                    if (wrapper.getState() == TaskState.RUNNING || wrapper.getState() == TaskState.RUNNING_BUT_NOT_EXEC) {
                        sw.setChecked(true);
                    } else {
                        sw.setChecked(false);
                    }
                    sw.setTag(wrapper.getTask());
                    sw.setOnCheckedChangeListener(this);
                    contentLayout.addView(sw);
                }

            }
        } else {

            TextView text = new TextView(context);
            params.bottomMargin = (int) DisplayConvertionUtility.convertDpToPixel(8, context);
            text.setLayoutParams(params);
            text.setTypeface(typeFace);
            text.setTextSize(16);
            text.setText(context.getString(R.string.no_active_tasks));
            contentLayout.addView(text);

        }

        return v;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getTag() instanceof TaskFlat) {
            Context context = buttonView.getContext().getApplicationContext();
            TaskFlat task = (TaskFlat) buttonView.getTag();
            if (isChecked) {
                Location last = ParticipActService.getLastLocation();
                if (!GeolocalizationTaskUtils.isActivatedByArea(task) || last != null && GeolocalizationTaskUtils.isActivatedByArea(task) && GeolocalizationTaskUtils.isInside(context, last.getLongitude(), last.getLatitude(), task.getActivationArea())) {
                    StateUtility.changeTaskState(context, task, TaskState.RUNNING);
                } else {
                    StateUtility.changeTaskState(context, task, TaskState.RUNNING_BUT_NOT_EXEC);
                }
                AlarmStateUtility.removeAlarm(context, task.getId());
            } else {
                StateUtility.changeTaskState(context, task, TaskState.SUSPENDED);
                AlarmStateUtility.addAlarm(context, task.getId());
            }
            consoleFragment.redrawFragment();
        }
    }

}
