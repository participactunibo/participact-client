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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import it.unibo.participact.R;
import it.unibo.participact.support.ColorUtility;

public class WelcomeCard extends Card {

    private static final String TITLE = "Welcome";
    private static final String COLOR = "#33b6ea";


    public WelcomeCard(Context context) {
        super(context.getString(R.string.welcome), context.getString(R.string.welcome_main_card), ColorUtility.parseColorFromId(context, R.color.primary_dark), ColorUtility.parseColorFromId(context, R.color.primary_dark), true,
                false);
    }

    @Override
    public View getCardContent(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_welcome, null);

        TextView title = (TextView) v.findViewById(R.id.title);
        TextView descriptionTextView = (TextView) v.findViewById(R.id.description);
        title.setText(titlePlay);
        title.setTextColor(Color.parseColor(titleColor));

        descriptionTextView.setText(description);
        descriptionTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
        ((ImageView) v.findViewById(R.id.stripe)).setBackgroundColor(Color
                .parseColor(color));

        if (isClickable == true)
            ((LinearLayout) v.findViewById(R.id.contentLayout))
                    .setBackgroundResource(R.drawable.selectable_background_cardbank);

        return v;
    }

}
