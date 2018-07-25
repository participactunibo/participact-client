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
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.networkstate.DefaultNetworkStateChecker;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import it.unibo.participact.ParticipActService;
import it.unibo.participact.R;
import it.unibo.participact.activities.DashboardActivity;
import it.unibo.participact.domain.enums.SensingActionEnum;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskService;
import it.unibo.participact.domain.rest.ActionFlat;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.network.request.AcceptTaskRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.RejectTaskRequest;
import it.unibo.participact.services.PendingActiveTaskIntentService;
import it.unibo.participact.support.DialogFactory;
import it.unibo.participact.support.GeolocalizationTaskUtils;
import it.unibo.participact.support.LoginUtility;
import it.unibo.participact.support.StringUtils;

public class TaskAvailableCard extends Card implements OnClickListener {

    private final static Logger logger = LoggerFactory.getLogger(TaskAvailableCard.class);

    private SpiceManager contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
    private String lastRequestCacheKey;

    it.unibo.participact.domain.rest.TaskFlat task;
    NetworkStateChecker networkChecker;

    Button acceptButton;
    Button rejectButton;
    Button updateButton;

    View view;

    public TaskAvailableCard(it.unibo.participact.domain.rest.TaskFlat task) {
        super(task.getName(), task.getDescription());
        this.task = task;
        networkChecker = new DefaultNetworkStateChecker();
    }

    @Override
    public View getCardContent(Context context) {

        if (TaskService.isTaskCompatibleWithThisAppVersion(task)) {

            view = LayoutInflater.from(context).inflate(R.layout.card_task_available, null);

            TextView titleTextView = (TextView) view.findViewById(R.id.title);
            TextView descriptionTextView = (TextView) view.findViewById(R.id.description);
            TextView pointsTextView = (TextView) view.findViewById(R.id.points);

            titleTextView.setText(title);

            descriptionTextView.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_descrizione), desc));
            descriptionTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
            pointsTextView.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_punti), task.getPoints().toString()));
            pointsTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
            Collection<ActionFlat> actions = task.getActions();
            TextView sensors = ((TextView) view.findViewById(R.id.sensors));
            String sensorsList = "";
            boolean camera = false;
            boolean questionario = false;
            boolean geofence = false;

            for (it.unibo.participact.domain.rest.ActionFlat actionFlat : actions) {
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
                if (actionFlat.getType() == ActionType.GEOFENCE && !geofence) {
                    sensorsList += context.getString(R.string.geofence) + " ";
                    geofence = true;

                }
            }

            sensors.setText(StringUtils.formatForTextView(context.getString(R.string.card_label_sensori), sensorsList));
            sensors.setTextColor(context.getResources().getColor(R.color.secondary_text));


            ((TextView) view.findViewById(R.id.duration)).setText(StringUtils.formatForTextView(context.getString(R.string.card_label_durata), task.getDuration().toString()));
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/YYYY HH:mm");
            ((TextView) view.findViewById(R.id.deadline)).setText(StringUtils.formatForTextView(context.getString(R.string.card_label_deadline_date), formatter.print(task.getDeadline())));

            rejectButton = ((Button) view.findViewById(R.id.no_button));
            rejectButton.setOnClickListener(this);
            acceptButton = ((Button) view.findViewById(R.id.yes_button));
            acceptButton.setOnClickListener(this);
        } else {

            view = LayoutInflater.from(context).inflate(R.layout.card_task_not_compatible, null);
            ((TextView) view.findViewById(R.id.title)).setText(title);
            updateButton = ((Button) view.findViewById(R.id.update_button));
            updateButton.setOnClickListener(this);

        }

        return view;
    }

    @Override
    public void onClick(View v) {
        final Context context = v.getContext();
        if (!networkChecker.isNetworkAvailable(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Attenzione").setMessage("Nessuna rete. Riprovare più tardi").create().show();
            return;
        } else {

            if (!contentManager.isStarted()) {
                contentManager.start(context);
            }

            if (v.getId() == R.id.yes_button) {
                AcceptTaskRequest request = new AcceptTaskRequest(context, task.getId());
                lastRequestCacheKey = request.createCacheKey();
                contentManager.execute(request, lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED, new AcceptTaskRequestListener(context));
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);

            } else if (v.getId() == R.id.no_button) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Attenzione");
                builder.setMessage("Una volta rifiutato questo task non sarà più disponibile. Sei sicuro di voler continuare?");
                builder.setNegativeButton(android.R.string.cancel, null);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RejectTaskRequest request = new RejectTaskRequest(context, task.getId());
                        lastRequestCacheKey = request.createCacheKey();
                        contentManager.execute(request, lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED, new RejectTaskRequestListener(context));
                        acceptButton.setEnabled(false);
                        rejectButton.setEnabled(false);
                    }
                });

                builder.create().show();

            } else if (v.getId() == R.id.update_button) {
                String appName = "it.unibo.participact";
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                }
            }
        }
    }

    private void hide() {
        if (mCardLayout != null) {
            mCardLayout.setVisibility(View.GONE);
        }
    }

    private class AcceptTaskRequestListener implements RequestListener<ResponseMessage> {

        Context context;

        public AcceptTaskRequestListener(Context context) {
            this.context = context;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            logger.warn("Accept task request of task with id {} failed.", task.getId(), spiceException);
            if (LoginUtility.checkIfLoginException(context, spiceException)) {

            } else {
                DialogFactory.showCommunicationErrorWithServer(context);
                acceptButton.setEnabled(true);
                rejectButton.setEnabled(true);
            }
        }

        @Override
        public void onRequestSuccess(ResponseMessage result) {
            if (result != null && result.getResultCode() == ResponseMessage.RESULT_OK) {
                logger.info("Accept task request of task with id {} completed with success.", task.getId());

                it.unibo.participact.domain.persistence.TaskFlat taskDB = StateUtility.addTask(context, task);
                Location last = ParticipActService.getLastLocation();
                if (taskDB != null) {
                    if (!GeolocalizationTaskUtils.isActivatedByArea(taskDB) || (last != null && GeolocalizationTaskUtils.isActivatedByArea(taskDB) && GeolocalizationTaskUtils.isInside(context, last.getLongitude(), last.getLatitude(), task.getActivationArea()))) {
                        //task in running
                        StateUtility.changeTaskState(context, taskDB, TaskState.RUNNING);
                    } else {
                        StateUtility.changeTaskState(context, taskDB, TaskState.RUNNING_BUT_NOT_EXEC);
                    }
                }
                hide();
                PendingActiveTaskIntentService.startActionScheduleReceiver(context, task.getId());
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                localBroadcastManager.sendBroadcast(new Intent(DashboardActivity.GO_TO_TASK_ACTIVE_FRAGMENT));
            }
        }

    }

    private class RejectTaskRequestListener implements RequestListener<ResponseMessage> {

        Context context;

        public RejectTaskRequestListener(Context context) {
            this.context = context;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            logger.warn("Reject task request of task with id {} failed.", task.getId(), spiceException);
            if (LoginUtility.checkIfLoginException(context, spiceException)) {

            } else {
                DialogFactory.showCommunicationErrorWithServer(context);
                acceptButton.setEnabled(true);
                rejectButton.setEnabled(true);
            }
        }

        @Override
        public void onRequestSuccess(ResponseMessage result) {
            if (result != null && result.getResultCode() == ResponseMessage.RESULT_OK) {
                logger.info("Reject task request of task with id {} completed with success.", task.getId());
            }
            hide();
        }

    }

}
