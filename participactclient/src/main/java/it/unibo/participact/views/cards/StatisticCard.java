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

import java.util.Properties;

import it.unibo.participact.R;
import it.unibo.participact.support.StringUtils;

public class StatisticCard extends Card {

    private static final String YEAR_MONTH = "yearmonth";
    private static final String COMPLETED_TASK = "completedTasks";
    private static final String FAILED_TASK = "failedTasks";
    private static final String REJECTED_TASK = "rejectedTasks";
    private static final String POINTS = "points";
    private static final String THRESHOLD = "threshold";
    private static final String CREATED_TASK = "createdTasks";

    Properties prop;

    public StatisticCard(Properties prop) {
        super();
        this.prop = prop;
    }

    @Override
    public View getCardContent(Context context) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.card_statistics, null);

        ((TextView) view.findViewById(R.id.title)).setText(getMonthAndYear(prop.getProperty(YEAR_MONTH), context));

        ((TextView) view.findViewById(R.id.completedWithSuccess)).setText(StringUtils.formatForTextView(context.getString(R.string.stats_completed_with_success), prop.getProperty(COMPLETED_TASK)));
        ((TextView) view.findViewById(R.id.completedWithFailure)).setText(StringUtils.formatForTextView(context.getString(R.string.stats_completed_with_failure), prop.getProperty(FAILED_TASK)));
        ((TextView) view.findViewById(R.id.createdTasks)).setText(StringUtils.formatForTextView(context.getString(R.string.stats_created_tasks), prop.getProperty(CREATED_TASK)));

        ((TextView) view.findViewById(R.id.rejected)).setText(StringUtils.formatForTextView(context.getString(R.string.stats_rejected), prop.getProperty(REJECTED_TASK)));
        ((TextView) view.findViewById(R.id.points)).setText(StringUtils.formatForTextView(context.getString(R.string.card_label_punti), prop.getProperty(POINTS)));

        if (!org.apache.commons.lang3.StringUtils.isEmpty(prop.getProperty(THRESHOLD))) {
            ((TextView) view.findViewById(R.id.threshold)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.threshold)).setText(StringUtils.formatForTextView("/", prop.getProperty(THRESHOLD)));
        }
        return view;
    }

    private String getMonthAndYear(String date, Context context) {
        String[] splitted = date.split(" ");
        int i = Integer.parseInt(splitted[1]);
        String result = "";
        switch (i) {
            case 1:
                result = context.getString(R.string.january);
                break;
            case 2:
                result = context.getString(R.string.february);
                break;
            case 3:
                result = context.getString(R.string.march);
                break;
            case 4:
                result = context.getString(R.string.april);
                break;
            case 5:
                result = context.getString(R.string.may);
                break;
            case 6:
                result = context.getString(R.string.june);
                break;
            case 7:
                result = context.getString(R.string.july);
                break;
            case 8:
                result = context.getString(R.string.august);
                break;
            case 9:
                result = context.getString(R.string.september);
                break;
            case 10:
                result = context.getString(R.string.october);
                break;
            case 11:
                result = context.getString(R.string.november);
                break;
            case 12:
                result = context.getString(R.string.december);
                break;
            default:
                break;
        }
        return String.format("%s %s", result, splitted[0]);
    }

}
