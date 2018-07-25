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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;
import com.octo.android.robospice.networkstate.DefaultNetworkStateChecker;
import com.octo.android.robospice.networkstate.NetworkStateChecker;

import it.unibo.participact.R;

public class TaskNotSupportedCard extends Card implements OnClickListener {

    NetworkStateChecker networkChecker;

    Button updateButton;

    View view;

    public TaskNotSupportedCard() {
        super("Task non supportato");
        networkChecker = new DefaultNetworkStateChecker();
    }

    @Override
    public View getCardContent(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_task_not_compatible, null);
        ((TextView) view.findViewById(R.id.title)).setText(title);
        updateButton = ((Button) view.findViewById(R.id.update_button));
        updateButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        if (!networkChecker.isNetworkAvailable(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Attenzione").setMessage("Nessuna rete. Riprovare più tardi").create().show();
            return;
        } else {
            if (v.getId() == R.id.update_button) {
                String appName = "it.unibo.participact";
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                }
            }
        }
    }

}
