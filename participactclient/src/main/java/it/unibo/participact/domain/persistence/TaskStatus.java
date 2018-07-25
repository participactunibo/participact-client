/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.persistence;

import android.content.Context;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;
import org.most.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import it.unibo.participact.broadcastreceivers.MoSTPingBroadcastReceiver;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.support.ProgressAlarm;

@DatabaseTable
public class TaskStatus implements Serializable {

    private static final long serialVersionUID = 3262602610008783643L;

    private static final Logger logger = LoggerFactory.getLogger(TaskStatus.class);

    private static final long MAX_TIMESTAMP_PROGRESS_DIFF = ProgressAlarm.PERIOD + 1 * 60 * 1000; // 1+1
    private static final long MAX_TIMESTAMP_MOST_ALIVE = ProgressAlarm.PERIOD + 1 * 60 * 1000; // 1+1 MIN

    @DatabaseField(id = true)
    Long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    TaskFlat task;

    @DatabaseField(dataType = DataType.ENUM_STRING)
    TaskState state;

    @DatabaseField
    long lastCheckedTimestamp;

    @DatabaseField
    long sensingProgress; // millisec

    @DatabaseField
    int photoThreshold; // photo to do for the task

    @DatabaseField
    int photoProgress; // photo done

    @DatabaseField
    int questionnaireProgress; // bool questionnaire done

    @DatabaseField
    int questionnaireThreshold;

    @DatabaseField
    long activityDetectionProgress;

    @DatabaseField
    int activityDetectionDuration;

    @DatabaseField(dataType = DataType.DATE_TIME)
    DateTime acceptedTime;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    ForeignCollection<RemainingPhotoPerAction> remainingPhotoPerAction;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    ForeignCollection<QuestionnaireProgressPerAction> questionnaireProgressPerAction;

    public TaskStatus() {
        acceptedTime = null;
        lastCheckedTimestamp = 0L;
        sensingProgress = 0L;
        questionnaireProgress = 0;
        questionnaireThreshold = 0;
        photoProgress = 0;
        photoThreshold = 0;
        activityDetectionProgress = 0L;
        activityDetectionDuration = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaskFlat getTask() {
        return task;
    }

    public void setTask(TaskFlat task) {
        this.task = task;
    }

    public TaskState taskId() {
        return state;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public long getLastCheckedTimestamp() {
        return lastCheckedTimestamp;
    }

    public void setLastCheckedTimestamp(long timestamp) {
        this.lastCheckedTimestamp = timestamp;
    }

    public DateTime getAcceptedTime() {
        return acceptedTime;
    }

    public void setAcceptedTime(DateTime acceptedTime) {
        this.acceptedTime = acceptedTime;
    }

    public long getSensingProgress() {
        return sensingProgress;
    }

    public void setSensingProgress(long sensingProgress) {
        this.sensingProgress = sensingProgress;
    }

    public int getPhotoProgress() {
        return photoProgress;
    }

    public void setPhotoProgress(int photoProgress) {
        this.photoProgress = photoProgress;
    }

    public int getQuestionnaireProgress() {
        return questionnaireProgress;
    }

    public void setQuestionnaireProgress(int questionnaireProgress) {
        this.questionnaireProgress = questionnaireProgress;
    }

    public int getPhotoThreshold() {
        return photoThreshold;
    }

    public void setPhotoThreshold(int photoThreshold) {
        this.photoThreshold = photoThreshold;
    }

    public int getQuestionnaireThreshold() {
        return questionnaireThreshold;
    }

    public void setQuestionnaireThreshold(int questionnaireThreshold) {
        this.questionnaireThreshold = questionnaireThreshold;
    }

    public long getActivityDetectionProgress() {
        return activityDetectionProgress;
    }

    public void setActivityDetectionProgress(long activityDetectionProgress) {
        this.activityDetectionProgress = activityDetectionProgress;
    }

    public int getActivityDetectionDuration() {
        return activityDetectionDuration;
    }

    public void setActivityDetectionDuration(int activityDetectionDuration) {
        this.activityDetectionDuration = activityDetectionDuration;
    }

    public ForeignCollection<RemainingPhotoPerAction> getRemainingPhotoPerAction() {
        return remainingPhotoPerAction;
    }

    public void setRemainingPhotoPerAction(
            ForeignCollection<RemainingPhotoPerAction> remainingPhotoPerAction) {
        this.remainingPhotoPerAction = remainingPhotoPerAction;
    }

    public ForeignCollection<QuestionnaireProgressPerAction> getQuestionnaireProgressPerAction() {
        return questionnaireProgressPerAction;
    }

    public void setQuestionnaireProgressPerAction(
            ForeignCollection<QuestionnaireProgressPerAction> questionnaireProgressPerAction) {
        this.questionnaireProgressPerAction = questionnaireProgressPerAction;
    }

    public synchronized void incrementSensingProgress(long timestamp) {
        // lastCheckedTimestamp � l'ultima volta che ho cercato di incremetare
        // il progresso di questo task
        long diff = timestamp - lastCheckedTimestamp;
        long mostDiff = timestamp
                - MoSTPingBroadcastReceiver.moSTlastResponsePing;

        lastCheckedTimestamp = timestamp;

        if (diff < 0) {
            logger.warn("Trying to increment progress with negative difference {}. Timestamp {}.", diff, timestamp);
            return;
        }

        if (diff <= MAX_TIMESTAMP_PROGRESS_DIFF
                && mostDiff < MAX_TIMESTAMP_MOST_ALIVE) {
            int successThreshold = 0;
            int current = 0;
            for (ActionFlat action : task.getActions()) {
                if (action.getType() == ActionType.SENSING_MOST && MoSTPingBroadcastReceiver.activePipeline != null) {
                    successThreshold++;
                    for (Pipeline.Type type : MoSTPingBroadcastReceiver.activePipeline) {
                        if (type.toInt() == action.getInput_type()) {
                            current++;
                        }
                    }
                }

                if (action.getType() == ActionType.ACTIVITY_DETECTION && MoSTPingBroadcastReceiver.activePipeline != null) {
                    for (Pipeline.Type type : MoSTPingBroadcastReceiver.activePipeline) {
                        if (type.toInt() == Pipeline.Type.ACTIVITY_RECOGNITION_COMPARE.toInt()) {//TODO change
                            // increment adProgress if the ACTIVITY_RECOGNITION_COMPARE is active
                            activityDetectionProgress += diff;
                        }
                    }
                }
            }

            if (successThreshold != 0 && current == successThreshold) {
                sensingProgress += diff;
            }

        }

    }

    public boolean isCollected(Context context){
        boolean result = false;
        for(ActionFlat a : task.getActions()){
                for(InterestPoint i : StateUtility.getInterestPointByActionFlat(context, a.getId())){
                    //se è stato racconto almeno un badge è completato con successo
                    if(StateUtility.isInterestPointCollected(context,i))
                        result = true;
                }
        }
        return result;
    }

    public boolean isCompleted() {
        long sensingDuration = 0L;
        if (task.getSensingDuration() != null) {
            sensingDuration = task.getSensingDuration();
        }

        if (sensingProgress / 60000 >= sensingDuration && photoProgress >= photoThreshold && questionnaireProgress >= questionnaireThreshold && activityDetectionProgress / 60000 >= activityDetectionDuration) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isExpired() {
        //accptedTime setted when the task goes on RUNNING or RUNNING_NOT_EXEC for the first time
        //in geolocalized task in HIDDEN state is null
        if (acceptedTime == null) {
            return false;
        }

        if (acceptedTime.plusMinutes(task.getDuration().intValue()).isBefore(new DateTime())) {
            return true;
        } else {
            return false;
        }
    }

    public float getProgressSensingPercentual() {
        return (sensingProgress / 600) / (float) task.getDuration();
    }

    public int getRemainingPhotoPerAction(Long actionId) {

        for (RemainingPhotoPerAction photo : remainingPhotoPerAction) {
            if (photo.getAction().getId().equals(actionId)) {
                return photo.getRemaingPhoto();
            }
        }
        return -1;
    }

    public Boolean isQuestionnaireCompleted(Long actionId) {
        for (QuestionnaireProgressPerAction quest : questionnaireProgressPerAction) {
            if (quest.getAction().getId().equals(actionId)) {
                return quest.isDone();
            }
        }
        return null;
    }

}
