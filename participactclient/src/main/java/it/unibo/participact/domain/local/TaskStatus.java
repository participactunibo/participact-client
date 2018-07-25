/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.local;

import org.joda.time.DateTime;
import org.most.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import it.unibo.participact.broadcastreceivers.MoSTPingBroadcastReceiver;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.ActionFlat;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.support.ProgressAlarm;

public class TaskStatus implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TaskStatus.class);

    private static final long serialVersionUID = -9171961426004331159L;
    private static final long MAX_TIMESTAMP_PROGRESS_DIFF = ProgressAlarm.PERIOD + 1 * 60 * 1000; // 1+1
    private static final long MAX_TIMESTAMP_MOST_ALIVE = ProgressAlarm.PERIOD + 1 * 60 * 1000; // 1+1 MIN

    TaskFlat task;
    TaskState state;
    long lastCheckedTimestamp;
    long sensingProgress; // millisec
    int photoThreshold; // photo to do for the task
    int photoProgress; // photo done
    int questionnaireProgress; // bool questionnaire done
    int questionnaireThreshold;
    DateTime acceptedTime;
    Map<Long, Integer> remainingPhotoPerAction;
    Map<Long, Boolean> questionnaireProgressPerAction;


    public TaskStatus(TaskFlat task, TaskState state) {
        this.task = task;
        this.state = state;

        //taskStatus is created when a task is accepted by user and sync with server
        acceptedTime = new DateTime();

        lastCheckedTimestamp = 0L;
        sensingProgress = 0L;

        //set completed
        questionnaireProgress = 0;
        questionnaireThreshold = 0;
        photoProgress = 0;
        photoThreshold = 0;
        remainingPhotoPerAction = new LinkedHashMap<Long, Integer>();
        questionnaireProgressPerAction = new LinkedHashMap<Long, Boolean>();

        //set threshold for TaskType if exist
        for (ActionFlat action : task.getActions()) {
            if (action.getType() == ActionType.PHOTO) {
                photoThreshold += action.getNumeric_threshold();
                remainingPhotoPerAction.put(action.getId(), action.getNumeric_threshold());
            }

            if (action.getType() == ActionType.QUESTIONNAIRE) {
                questionnaireThreshold++;
                questionnaireProgressPerAction.put(action.getId(), false);
            }
        }
    }

    public TaskFlat getTask() {
        return task;
    }

    public void setTask(TaskFlat task) {
        this.task = task;
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
                if (action.getType() == ActionType.SENSING_MOST
                        && MoSTPingBroadcastReceiver.activePipeline != null) {
                    successThreshold++;
                    for (Pipeline.Type type : MoSTPingBroadcastReceiver.activePipeline) {
                        if (type.toInt() == action.getInput_type()) {
                            current++;
                        }
                    }
                }
            }

            if (successThreshold != 0 && current == successThreshold) {
                sensingProgress += diff;
            }
        }

    }

    public synchronized void incrementPhotoProgress(Long actionId) {
        if (!remainingPhotoPerAction.containsKey(actionId)) {
            return;
        }

        int photoRemaing = remainingPhotoPerAction.get(actionId);
        if (photoRemaing <= 0) {
            return;
        } else {
            photoRemaing--;
            remainingPhotoPerAction.put(actionId, photoRemaing);
            //increment total photo progress
            photoProgress++;
        }
    }

    public synchronized void incrementQuestionnaireProgress(Long actionId) {
        if (!questionnaireProgressPerAction.containsKey(actionId)) {
            return;
        } else {
            questionnaireProgressPerAction.put(actionId, true);
            questionnaireProgress++;
        }
    }

    public boolean isSuccesGeobadgeCollected(){
        if(getSensingProgress()>0)
            return true;
        return false;
    }

    public boolean isCompleted() {
        long sensingDuration = 0L;
        if (task.getSensingDuration() != null) {
            sensingDuration = task.getSensingDuration();
        }

        if (sensingProgress / 60000 >= sensingDuration && photoProgress >= photoThreshold && questionnaireProgress >= questionnaireThreshold) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isExpired() {
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
        if (!remainingPhotoPerAction.containsKey(actionId)) {
            return -1;
        } else {
            return remainingPhotoPerAction.get(actionId);
        }
    }

    public Boolean isQuestionnaireCompleted(Long actionId) {
        if (!questionnaireProgressPerAction.containsKey(actionId)) {
            return null;
        } else {
            return questionnaireProgressPerAction.get(actionId);
        }
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

    public Map<Long, Integer> getRemainingPhotoPerAction() {
        return remainingPhotoPerAction;
    }

    public void setRemainingPhotoPerAction(Map<Long, Integer> remainingPhotoPerAction) {
        this.remainingPhotoPerAction = remainingPhotoPerAction;
    }

    public Map<Long, Boolean> getQuestionnaireProgressPerAction() {
        return questionnaireProgressPerAction;
    }

    public void setQuestionnaireProgressPerAction(Map<Long, Boolean> questionnaireProgressPerAction) {
        this.questionnaireProgressPerAction = questionnaireProgressPerAction;
    }

}
