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
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fima.cardsui.objects.Card;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

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
import it.unibo.participact.domain.persistence.support.DomainDBHelper;
import it.unibo.participact.questionnaire.QuestionnaireActivity;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.ImageDescriptorUtility;
import it.unibo.participact.support.StringUtils;
import it.unibo.participact.support.preferences.DataUploaderPhotoPreferences;

public class TaskActiveCard extends Card implements OnClickListener {

    private final static Logger logger = LoggerFactory.getLogger(TaskActiveCard.class);

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

    public TaskActiveCard(Activity context, TaskStatus taskStatus) {
        super(taskStatus.getTask().getName(), taskStatus.getTask().getDescription());
        this.context = context;
        this.taskStatus = taskStatus;
    }

    @Override
    public View getCardContent(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_task_active, null);
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
        boolean geofence= false;

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
            if (actionFlat.getType() == ActionType.GEOFENCE && !geofence ) {
                sensorsList += context.getString(R.string.geofence) + " ";
                geofence = true;
            }
        }

        sensors.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_sensori), sensorsList));
        sensors.setTextColor(context.getResources().getColor(R.color.secondary_text));
//		((TextView) view.findViewById(R.id.duration)).setText(StringUtils.formatForTextView(context.getString(R.string.card_label_durata), taskStatus.getTask().getDuration().toString()));

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/YYYY HH:mm");
        TextView expirationDateTextView = (TextView) view.findViewById(R.id.expirationDate);
        expirationDateTextView.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_expiration_date), formatter.print(taskStatus.getAcceptedTime().plusMinutes(taskStatus.getTask().getDuration().intValue()))));
        expirationDateTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));

        ((TextView) view.findViewById(R.id.progress)).setVisibility(View.GONE);

//		float progressThreshold = (((float)taskStatus.getTask().getSensingDuration())/((float)taskStatus.getTask().getDuration())*100);	
        ((TextView) view.findViewById(R.id.progress_threshold)).setVisibility(View.GONE);

        LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll_photo);
        for (ActionFlat actionFlat : actions) {
            if (actionFlat.getType() == ActionType.PHOTO) {
                ll.setVisibility(View.VISIBLE);
                int remaingPhoto = taskStatus.getRemainingPhotoPerAction(actionFlat.getId());
                int takenPhoto = actionFlat.getNumeric_threshold() - remaingPhoto;
                View v = LayoutInflater.from(context).inflate(R.layout.action_photo, null);
                String status = String.format(context.getString(R.string.photo) + " %s " + context.getString(R.string.of) + " %s", takenPhoto, actionFlat.getNumeric_threshold());
                TextView text = ((TextView) v.findViewById(R.id.text));
                text.setText(actionFlat.getName());
                text.setTextColor(context.getResources().getColor(R.color.secondary_text));
                text.setTag(actionFlat.getName());
                text.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (v.getTag() instanceof String) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setTitle(context.getString(R.string.photo_description)).setMessage(v.getTag().toString()).setPositiveButton(android.R.string.ok, null);
                            builder.create().show();
                        }
                    }
                });
                Button b = (Button) v.findViewById(R.id.button);
                b.setTag(actionFlat.getId());
                b.setOnClickListener(this);
                b.setText(context.getString(R.string.take_picture_short) + "\n" + status);
                if (takenPhoto == actionFlat.getNumeric_threshold()) {
                    b.setBackgroundResource(R.drawable.gray_button);
                    b.setEnabled(false);
                    b.setText(context.getString(R.string.completed));
                }
                ll.addView(v);
            }
        }


        LinearLayout llQuestionnaire = (LinearLayout) view.findViewById(R.id.ll_questionnaire);
        for (ActionFlat actionFlat : actions) {
            if (actionFlat.getType() == ActionType.QUESTIONNAIRE) {
                llQuestionnaire.setVisibility(View.VISIBLE);
                View v = LayoutInflater.from(context).inflate(R.layout.action_questionnaire, null);

                TextView text = ((TextView) v.findViewById(R.id.text));
                text.setText(actionFlat.getTitle());
                text.setTextColor(context.getResources().getColor(R.color.secondary_text));
                text.setTag(actionFlat.getTitle());
                text.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (v.getTag() instanceof String) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setTitle(context.getString(R.string.questionnaire_description)).setMessage(v.getTag().toString()).setPositiveButton(android.R.string.ok, null);
                            builder.create().show();
                        }
                    }
                });

                Button b = (Button) v.findViewById(R.id.button);
                b.setText(R.string.complete_questionnaire);
                b.setTag(actionFlat.getId());
                b.setOnClickListener(this);
                if (taskStatus.isQuestionnaireCompleted(actionFlat.getId())) {
                    b.setBackgroundResource(R.drawable.gray_button);
                    b.setEnabled(false);
                    b.setText(context.getString(R.string.completed));
                }
                llQuestionnaire.addView(v);
            }
        }




//		ActionFlat af = new ActionFlat();
//		af.setType(ActionType.ACTIVITY_DETECTION);
//		actions.add(af);

        for (ActionFlat actionFlat : actions) {
            if (actionFlat.getType() == ActionType.ACTIVITY_DETECTION) {

                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View adRoot = li.inflate(R.layout.activity_detection_layout, null);

                Spinner spinner = (Spinner) adRoot.findViewById(R.id.ad_spinner);
                spinner.setBackgroundColor(context.getResources().getColor(R.color.primary_dark));
                String[] values = {"5 min", "10 min", "15 min"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, values);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        spinnerPos = pos;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

                statico_btn = (ToggleButton) adRoot.findViewById(R.id.statico_in_tasca_btn);
                camminare_btn = (ToggleButton) adRoot.findViewById(R.id.camminare_btn);
                corsa_btn = (ToggleButton) adRoot.findViewById(R.id.corsa_btn);

                //init

                statico_btn.setTag(actionFlat.getId());
                statico_btn.setOnClickListener(this);
                camminare_btn.setTag(actionFlat.getId());
                camminare_btn.setOnClickListener(this);
                corsa_btn.setTag(actionFlat.getId());
                corsa_btn.setOnClickListener(this);

                // Ripristino stato

                SharedPreferences prefs = context.getSharedPreferences(ACTIVITY_DETECTION_PREFS, Context.MODE_PRIVATE);
                int selected = prefs.getInt(KEY_AD_SELECTED, -1);

                switch (selected) {
                    case R.id.statico_in_tasca_btn:
                        statico_btn.setChecked(true);
                        break;
                    case R.id.camminare_btn:
                        camminare_btn.setChecked(true);
                        break;
                    case R.id.corsa_btn:
                        corsa_btn.setChecked(true);
                        break;
                    default:
                        break;
                }

                LinearLayout content = (LinearLayout) view.findViewById(R.id.content_ll);
                content.addView(adRoot);
            }
        }

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getTag() instanceof Long) {
            Long actionId = (Long) v.getTag();

            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<ActionFlat, Long> actionDao = databaseHelper.getRuntimeExceptionDao(ActionFlat.class);
            ActionFlat action = actionDao.queryForId(actionId);

            OpenHelperManager.releaseHelper();

            if (action == null) {
                logger.warn("Action id {} not found.", actionId);
                return;
            }

            if (action.getType() == ActionType.PHOTO) {
                DataUploaderPhotoPreferences.getInstance(v.getContext()).setPhotoUpload(false);

                Date date = new Date();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(date);
                String imageFileName = "Photo_" + timeStamp;

                File image = null;
                image = new File(context.getExternalFilesDir(null), imageFileName + ".jpg");

                ImageDescriptor imgDescriptor = new ImageDescriptor();
                imgDescriptor.setTaskId(taskStatus.getTask().getId());
                imgDescriptor.setActionId(actionId);
                imgDescriptor.setSampleTimestamp(date.getTime());
                imgDescriptor.setImageName(imageFileName);
                imgDescriptor.setImagePath(image.getAbsolutePath());

                ImageDescriptorUtility.persistImageDescriptor(context, "temp.ids", imgDescriptor);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                //	    takePictureIntent.putExtra("return-data", true);

                logger.info("Taking Photo {} of task {} action id {}.", imgDescriptor.getImageName(), taskStatus.getTask().getId(), actionId);
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
