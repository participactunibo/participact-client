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
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.most.pipeline.Pipeline;

import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.domain.rest.ActionFlatRequest;


/**
 * Created by alessandro on 22/11/14.
 */
public class ActionsAdapter extends BaseAdapter {

    private final ActionRemoveListener actionRemoveListener;
    private Context context;
    private List<ActionFlatRequest> actionFlatRequests;

    public ActionsAdapter(Context context, List<ActionFlatRequest> actionFlatRequest, ActionRemoveListener actionRemoveListener) {
        this.actionRemoveListener = actionRemoveListener;
        this.context = context;
        this.actionFlatRequests = actionFlatRequest;

    }


    @Override
    public int getCount() {
        return actionFlatRequests.size();
    }

    @Override
    public Object getItem(int position) {
        return actionFlatRequests.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        switch (actionFlatRequests.get(position).getType()) {
            case ACTIVITY_DETECTION:
                return 0;
            case QUESTIONNAIRE:
                return 1;
            case PHOTO:
                return 2;
            case SENSING_MOST:
                return 3;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ActionFlatRequest item = actionFlatRequests.get(position);
        ViewPhotoHolder photoHolder = null;
        ViewActivityDetectionHolder activityDetectionHolder = null;
        ViewSensingHolder sensingHolder = null;
        ViewQuestionnaireHolder questionnaireHolder = null;

        if (convertView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater) context.getSystemService(
                    inflater);
            if (getItemViewType(position) == 1) {
                convertView = li.inflate(R.layout.fragment_questionnaire_action_item, parent, false);
                questionnaireHolder = new ViewQuestionnaireHolder();
                questionnaireHolder.type = (TextView) convertView.findViewById(R.id.action_type);
                questionnaireHolder.title = (TextView) convertView.findViewById(R.id.action_questionnaire_title);
                questionnaireHolder.description = (TextView) convertView.findViewById(R.id.action_questionnaire_details);
                questionnaireHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                questionnaireHolder.removeButton = (ImageButton) convertView.findViewById(R.id.action_remove_button);
                convertView.setTag(photoHolder);
            }

            if (getItemViewType(position) == 2) {
                convertView = li.inflate(R.layout.fragment_photo_action_item, parent, false);
                photoHolder = new ViewPhotoHolder();
                photoHolder.type = (TextView) convertView.findViewById(R.id.action_type);
                photoHolder.name = (TextView) convertView.findViewById(R.id.action_photo_name);
                photoHolder.details = (TextView) convertView.findViewById(R.id.action_photo_details);
                photoHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                photoHolder.removeButton = (ImageButton) convertView.findViewById(R.id.action_remove_button);
                convertView.setTag(photoHolder);
            } else if (getItemViewType(position) == 3) {
                convertView = li.inflate(R.layout.fragment_sensing_action_item, parent, false);
                sensingHolder = new ViewSensingHolder();
                sensingHolder.type = (TextView) convertView.findViewById(R.id.action_type);
                sensingHolder.description = (TextView) convertView.findViewById(R.id.action_sensing_description);
                sensingHolder.inputType = (TextView) convertView.findViewById(R.id.action_sensing_input);
                sensingHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                sensingHolder.removeButton = (ImageButton) convertView.findViewById(R.id.action_remove_button);
                convertView.setTag(sensingHolder);


            } else if (getItemViewType(position) == 0) {
                convertView = li.inflate(R.layout.fragment_activity_detection_action_item, parent, false);
                activityDetectionHolder = new ViewActivityDetectionHolder();
                activityDetectionHolder.type = (TextView) convertView.findViewById(R.id.action_type);
                activityDetectionHolder.details = (TextView) convertView.findViewById(R.id.action_activity_detection_details);
                activityDetectionHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                activityDetectionHolder.removeButton = (ImageButton) convertView.findViewById(R.id.action_remove_button);
                convertView.setTag(activityDetectionHolder);
            }


        } else {
            if (getItemViewType(position) == 0) {
                activityDetectionHolder = (ViewActivityDetectionHolder) convertView.getTag();
            } else if (getItemViewType(position) == 2) {
                photoHolder = (ViewPhotoHolder) convertView.getTag();

            } else if (getItemViewType(position) == 1)
                questionnaireHolder = (ViewQuestionnaireHolder) convertView.getTag();
            else if (getItemViewType(position) == 3) {
                sensingHolder = (ViewSensingHolder) convertView.getTag();
            }
        }

        if (photoHolder != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(item.getType());
            photoHolder.type.setText(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("Nome: ");
            stringBuilder.append(item.getName());
            photoHolder.name.setText(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("Numero di fotografie: ");
            stringBuilder.append(item.getNumeric_threshold());
            photoHolder.details.setText(stringBuilder.toString());
            photoHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionRemoveListener.onActionRemoved(position);
                }
            });

        }
        if (questionnaireHolder != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(item.getTitle());
            questionnaireHolder.title.setText(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(item.getDescription());
            questionnaireHolder.description.setText(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(item.getType());
            questionnaireHolder.type.setText(stringBuilder.toString());
            questionnaireHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionRemoveListener.onActionRemoved(position);
                }
            });
        }
        if (activityDetectionHolder != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Soglia di successo: ");
            stringBuilder.append(item.getDuration_threshold());
            stringBuilder.append(" minuti");
            activityDetectionHolder.details.setText(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(item.getType());
            activityDetectionHolder.type.setText(stringBuilder.toString());
            activityDetectionHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionRemoveListener.onActionRemoved(position);
                }
            });
        }
        if (sensingHolder != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(item.getType());
            sensingHolder.type.setText(builder.toString());
            builder = new StringBuilder();
            builder.append(item.getName());
            sensingHolder.description.setText(builder.toString());
            builder = new StringBuilder();
            String pipeline = getStringRep(Pipeline.Type.fromInt(item.getInput_type()));
            builder.append(pipeline);
            sensingHolder.inputType.setText(builder.toString());
            sensingHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionRemoveListener.onActionRemoved(position);
                }
            });

        }
        return convertView;
    }

    private String getStringRep(Pipeline.Type pipeline) {

        String[] input_type = context.getResources().getStringArray(R.array.sensing_most_input);

        switch (pipeline) {

            case AUDIO_CLASSIFIER:
                return input_type[0];
            case ACTIVITY_RECOGNITION_COMPARE:
                return input_type[1];
            case ACCELEROMETER:
                return input_type[2];
            case ACCELEROMETER_CLASSIFIER:
                return input_type[3];
            case RAW_AUDIO:
                return input_type[4];
            case AVERAGE_ACCELEROMETER:
                return input_type[5];
            case APP_ON_SCREEN:
                return input_type[6];
            case APPS_NET_TRAFFIC:
                return input_type[7];
            case BATTERY:
                return input_type[8];
            case BLUETOOTH:
                return input_type[9];
            case CELL:
                return input_type[10];
            case CONNECTION_TYPE:
                return input_type[11];
            case DEVICE_NET_TRAFFIC:
                return input_type[12];
            case GOOGLE_ACTIVITY_RECOGNITION:
                return input_type[13];
            case GYROSCOPE:
                return input_type[14];
            case INSTALLED_APPS:
                return input_type[15];
            case LIGHT:
                return input_type[16];
            case LOCATION:
                return input_type[17];
            case MAGNETIC_FIELD:
                return input_type[18];
            case PHONE_CALL_DURATION:
                return input_type[19];
            case PHONE_CALL_EVENT:
                return input_type[20];
            case SYSTEM_STATS:
                return input_type[21];
            case WIFI_SCAN:
                return input_type[22];
            case DR:
                return input_type[23];

            default:
                return null;

        }

    }

    public interface ActionRemoveListener {
        void onActionRemoved(int position);
    }


    static class ViewPhotoHolder {
        TextView type;
        TextView details;
        TextView name;
        ImageView imageView;
        ImageButton removeButton;
    }

    static class ViewActivityDetectionHolder {
        TextView type;
        TextView details;
        ImageView imageView;
        ImageButton removeButton;

    }

    static class ViewSensingHolder {
        TextView type;
        TextView description;
        TextView inputType;
        ImageView imageView;
        ImageButton removeButton;
    }

    static class ViewQuestionnaireHolder {
        TextView type;
        TextView title;
        TextView description;
        ImageView imageView;
        ImageButton removeButton;


    }


}
