/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.domain.rest.ClosedAnswerRequest;

/**
 * Created by alessandro on 28/11/14.
 */
public class ClosedAnswerAdapter extends ArrayAdapter<ClosedAnswerRequest> {

    private final int resource;
    private final ClosedAnswerRemovedListener answerRemovedListener;


    public ClosedAnswerAdapter(Context context, int resource, List<ClosedAnswerRequest> objects, ClosedAnswerRemovedListener answerRemovedListener) {
        super(context, resource, objects);
        this.resource = resource;
        this.answerRemovedListener = answerRemovedListener;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ClosedAnswerRequest item = getItem(position);
        ClosedAnswerHolder holder = null;

        if (convertView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                    inflater);
            convertView = li.inflate(resource, parent, false);
            holder = new ClosedAnswerHolder();
            holder.answer = (TextView) convertView.findViewById(R.id.answer_item);
            holder.removeButton = (ImageButton) convertView.findViewById(R.id.action_remove_button);
            holder.icon = (ImageView) convertView.findViewById(R.id.imageView);

            convertView.setTag(holder);

        } else
            holder = (ClosedAnswerHolder) convertView.getTag();

        if (holder != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(item.getAnswerDescription());
            holder.answer.setText(stringBuilder.toString());
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    answerRemovedListener.onClosedAnswerRemoved(position);
                }
            });
        }


        return convertView;
    }

    public interface ClosedAnswerRemovedListener {
        void onClosedAnswerRemoved(int position);
    }

    static class ClosedAnswerHolder {
        TextView answer;
        ImageView icon;
        ImageButton removeButton;
    }
}
