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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fima.cardsui.objects.Card;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.most.MoSTApplication;
import org.most.MoSTService;
import org.most.pipeline.Pipeline;
import org.most.pipeline.PipelineActivityRecognitionCompare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import it.unibo.participact.R;
import it.unibo.participact.domain.enums.SensingActionEnum;
import it.unibo.participact.domain.local.ImageDescriptor;
import it.unibo.participact.domain.persistence.ActionFlat;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.persistence.Question;
import it.unibo.participact.domain.persistence.TaskStatus;
import it.unibo.participact.questionnaire.QuestionnaireActivity;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.ImageDescriptorUtility;
import it.unibo.participact.support.StringUtils;
import it.unibo.participact.support.preferences.DataUploaderPhotoPreferences;

public class TaskActiveRunningNotExecCard extends Card implements OnClickListener {

    private final static Logger logger = LoggerFactory.getLogger(TaskActiveRunningNotExecCard.class);

    public final static String ACTIVITY_DETECTION_PREFS = "ActivityDetectionPrefs";
    public final static String KEY_AD_SELECTED = "ActivityDetectionPrefs.selected";

    private final static String STOP_AD_ACTION = "it.unibo.participact.STOP_ACTIVITY_DETECTION";

    TaskStatus taskStatus;
    Activity context;
    Fragment fragment;

    ToggleButton statico_btn;
    ToggleButton camminare_btn;
    ToggleButton corsa_btn;

    int spinnerPos;

    public TaskActiveRunningNotExecCard(Activity context, TaskStatus taskStatus) {
        super(taskStatus.getTask().getName(), taskStatus.getTask().getDescription());
        this.context = context;
        this.taskStatus = taskStatus;
    }

    @Override
    public View getCardContent(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_task_active_not_exec, null);
        view.setClickable(false);
        TextView titleTextView = (TextView) view.findViewById(R.id.title);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.description);
        TextView pointsTextView = (TextView) view.findViewById(R.id.points);
        titleTextView.setText(title);
        descriptionTextView.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_descrizione), desc));
        descriptionTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
        pointsTextView.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_punti), taskStatus.getTask().getPoints().toString()));
        pointsTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
        Collection<ActionFlat> unsortedActions = taskStatus.getTask().getActions();

        List<ActionFlat> actions = new ArrayList<ActionFlat>(unsortedActions);

//		Collections.sort(actions, new ActionFlatComparator());

        TextView sensors = ((TextView) view.findViewById(R.id.sensors));

        String sensorsList = "";
        boolean camera = false;
        boolean questionario = false;
        boolean geofence = false;

        for (ActionFlat actionFlat : actions) {
            if (actionFlat.getType() == ActionType.SENSING_MOST) {
                sensorsList += SensingActionEnum.Type.fromIntToHumanReadable(actionFlat.getInput_type().intValue()).toString() + " ";
            }

            if (actionFlat.getType() == ActionType.PHOTO && !camera) {
                sensorsList += context.getString(R.string.camera) + " ";
                camera = true;
            }

            if (actionFlat.getType() == ActionType.QUESTIONNAIRE && !questionario) {
                sensorsList += context.getString(R.string.questionnaire) + " ";
                questionario = true;
            }

            if (actionFlat.getType() == ActionType.ACTIVITY_DETECTION) {
                sensorsList += context.getString(R.string.activity_detecion) + " ";
                questionario = true;
            }
            if (actionFlat.getType() == ActionType.GEOFENCE && ! geofence) {
                sensorsList += context.getString(R.string.geofence) + " ";
                geofence = true;
            }

        }

        sensors.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_sensori), sensorsList));
        sensors.setTextColor(context.getResources().getColor(R.color.secondary_text));

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/YYYY HH:mm");
        ((TextView) view.findViewById(R.id.expirationDate)).setText(StringUtils.formatForTextView(context.getString(R.string.card_label_expiration_date), formatter.print(taskStatus.getAcceptedTime().plusMinutes(taskStatus.getTask().getDuration().intValue()))));

        SpannableString result = new SpannableString(context.getText(R.string.task_not_exec));
        result.setSpan(new StyleSpan(Typeface.BOLD), 0, context.getText(R.string.task_not_exec).length(), 0);
        ((TextView) view.findViewById(R.id.warning)).setText(result);
        ((TextView) view.findViewById(R.id.warning)).setTextColor(context.getResources().getColor(R.color.red));

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getTag() instanceof ActionFlat) {
            ActionFlat action = (ActionFlat) v.getTag();
            if (action.getType() == ActionType.PHOTO) {

                DataUploaderPhotoPreferences.getInstance(v.getContext()).setPhotoUpload(false);

                Date date = new Date();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(date);
                String imageFileName = "Photo_" + timeStamp;

                File image = null;
                image = new File(context.getExternalFilesDir(null), imageFileName + ".jpg");

                ImageDescriptor imgDescriptor = new ImageDescriptor();
                imgDescriptor.setTaskId(taskStatus.getTask().getId());
                imgDescriptor.setActionId(((ActionFlat) v.getTag()).getId());
                imgDescriptor.setSampleTimestamp(date.getTime());
                imgDescriptor.setImageName(imageFileName);
                imgDescriptor.setImagePath(image.getAbsolutePath());

                ImageDescriptorUtility.persistImageDescriptor(context, "temp.ids", imgDescriptor);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                //	    takePictureIntent.putExtra("return-data", true);

                logger.info("Taking Photo {} of task {} action id {}.", imgDescriptor.getImageName(), taskStatus.getTask().getId(), ((ActionFlat) v.getTag()).getId());
                context.startActivityForResult(takePictureIntent, Configuration.PHOTO_REQUEST_CODE);
            }

            if (action.getType() == ActionType.QUESTIONNAIRE) {

                Intent questionnaireIntent = new Intent(context, QuestionnaireActivity.class);
                questionnaireIntent.putExtra(QuestionnaireActivity.EXTRA_TASK_ID, taskStatus.getTask().getId());
                questionnaireIntent.putExtra(QuestionnaireActivity.EXTRA_ACTION_ID, action.getId());
                LinkedList<Question> list = new LinkedList<Question>();
                list.addAll(action.getQuestions());
                questionnaireIntent.putExtra(QuestionnaireActivity.EXTRA_QUESTION, list);

                logger.info("Start Questionnaire of task with id {} and actionId {}.", taskStatus.getTask().getId(), action.getId());
                context.startActivityForResult(questionnaireIntent, Configuration.QUESTIONNAIRE_REQUEST_CODE);

            }

            if (action.getType() == ActionType.ACTIVITY_DETECTION) {

                if (v instanceof ToggleButton) {
                    ToggleButton clicked = (ToggleButton) v;
                    boolean isChecked = clicked.isChecked();

                    // spengo pipeline

                    Intent i = new Intent(context, MoSTService.class);
                    i.setAction(MoSTService.STOP);
                    i.putExtra(MoSTService.KEY_PIPELINE_TYPE, Pipeline.Type.ACTIVITY_RECOGNITION_COMPARE.toInt());//TODO change
                    context.startService(i);

                    //rimuovo eventuali alarm

                    AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent();
                    intent.setAction(STOP_AD_ACTION);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mgr.cancel(pendingIntent);

                    if (!isChecked) {
                        Editor editor = context.getSharedPreferences(ACTIVITY_DETECTION_PREFS, Context.MODE_PRIVATE).edit();
                        editor.putInt(KEY_AD_SELECTED, -1);
                        editor.apply();
                        logger.info("Activity Detection stopped by user.");
                        return;
                    } else {

                        statico_btn.setChecked(false);
                        camminare_btn.setChecked(false);
                        corsa_btn.setChecked(false);
                        Editor editor = context.getSharedPreferences(MoSTApplication.PREF_PIPELINES, Context.MODE_PRIVATE).edit();
                        String userAD = "";
                        switch (v.getId()) {
                            case R.id.statico_in_tasca_btn:
                                statico_btn.setChecked(true);
                                userAD = "statico in tasca";
                                break;
                            case R.id.camminare_btn:
                                camminare_btn.setChecked(true);
                                userAD = "camminando";
                                break;
                            case R.id.corsa_btn:
                                corsa_btn.setChecked(true);
                                userAD = "correndo";
                                break;
                            default:
                                return;
                        }
                        editor.putString(PipelineActivityRecognitionCompare.PREF_KEY_USER_ACTIVITY, userAD);
                        editor.apply();

                        //avvio pipeline
                        i = new Intent(context, MoSTService.class);
                        i.setAction(MoSTService.START);
                        i.putExtra(MoSTService.KEY_PIPELINE_TYPE, Pipeline.Type.ACTIVITY_RECOGNITION_COMPARE.toInt());//TODO change
                        context.startService(i);

                        //salvo stato
                        editor = context.getSharedPreferences(ACTIVITY_DETECTION_PREFS, Context.MODE_PRIVATE).edit();
                        editor.putInt(KEY_AD_SELECTED, v.getId());
                        editor.apply();

                        //set alarm
                        int minutes = (spinnerPos + 1) * 5;

                        intent = new Intent();
                        intent.setAction(STOP_AD_ACTION);
                        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutes * 60000, pendingIntent);

                        logger.info("Started Activity Detection for {} user activity for {} minutes. Override last detection if exists.", userAD, minutes);

                    }
                }
            }
        }
    }
}
