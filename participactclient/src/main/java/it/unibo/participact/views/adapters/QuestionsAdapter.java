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
import it.unibo.participact.domain.rest.QuestionRequest;

/**
 * Created by alessandro on 27/11/14.
 */
public class QuestionsAdapter extends ArrayAdapter<QuestionRequest> {

    private final int resource;
    private final QuestionRemovedListener questionRemovedListener;


    public QuestionsAdapter(Context context, int resource, List<QuestionRequest> objects, QuestionRemovedListener questionRemovedListener) {
        super(context, resource, objects);
        this.resource = resource;
        this.questionRemovedListener = questionRemovedListener;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        QuestionRequest questionRequest = getItem(position);
        QuestionViewHolder holder = null;
        if (convertView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                    inflater);
            convertView = li.inflate(resource, parent, false);
            holder = new QuestionViewHolder();
            holder.question = (TextView) convertView.findViewById(R.id.action_question_title);
            holder.type = (TextView) convertView.findViewById(R.id.action_type);
            holder.letterImageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.removeButton = (ImageButton) convertView.findViewById(R.id.action_remove_button);
            convertView.setTag(holder);
        } else
            holder = (QuestionViewHolder) convertView.getTag();

        if (holder != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(questionRequest.getQuestion());
            holder.question.setText(stringBuilder.toString());

            stringBuilder = new StringBuilder();
            if (questionRequest.getIsClosedAnswers()) {
                stringBuilder.append("Closed Question");
            } else {
                stringBuilder.append("Open Question");
            }

            holder.type.append(stringBuilder.toString());
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    questionRemovedListener.onQuestionRemoved(position);
                }
            });
        }


        return convertView;
    }


    static class QuestionViewHolder {
        TextView type;
        TextView question;
        ImageView letterImageView;
        ImageButton removeButton;
    }

    public interface QuestionRemovedListener {
        void onQuestionRemoved(int position);
    }
}
