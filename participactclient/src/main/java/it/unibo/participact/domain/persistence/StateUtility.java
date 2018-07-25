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

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import it.unibo.participact.R;
import it.unibo.participact.broadcastreceivers.GcmBroadcastReceiver;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.local.ImageDescriptor;
import it.unibo.participact.domain.persistence.support.DomainDBHelper;
import it.unibo.participact.domain.persistence.support.State;
import it.unibo.participact.network.CompleteTaskListener;
import it.unibo.participact.network.CompleteWithFailureTaskRequest;
import it.unibo.participact.network.request.CollectedBadgeListener;
import it.unibo.participact.network.request.CollectedBadgeRequest;
import it.unibo.participact.network.request.CompleteWithSuccessTaskRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.support.AlarmStateUtility;
import it.unibo.participact.support.ImageDescriptorUtility;
import it.unibo.participact.support.NotificationUtility;
import it.unibo.participact.support.SystemUpgrade;

public class StateUtility {

    private final static Logger logger = LoggerFactory.getLogger(StateUtility.class);


    public static synchronized State loadState(Context context) {
        State result = null;
        try {

            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            StackTraceElement e = stacktrace[3];//maybe this number needs to be corrected String methodName = e.getMethodName();

            logger.debug("Called by {} {}", e.getClassName(), e.getMethodName());

            SystemUpgrade.upgrade(context);

            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskStatus, Long> dao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);
            List<TaskStatus> list = dao.queryForAll();
            result = new State(list);

        } catch (Exception e) {
            logger.error("Exception loading state.", e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return result;
    }

    public static synchronized void convertTaskStatus(Context context, it.unibo.participact.domain.local.TaskStatus task) {
        try {

            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskFlat, Long> taskFlatDao = databaseHelper.getRuntimeExceptionDao(TaskFlat.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);
            RuntimeExceptionDao<ActionFlat, Long> actionDao = databaseHelper.getRuntimeExceptionDao(ActionFlat.class);
            RuntimeExceptionDao<RemainingPhotoPerAction, Long> remainPhotoDao = databaseHelper.getRuntimeExceptionDao(RemainingPhotoPerAction.class);
            RuntimeExceptionDao<QuestionnaireProgressPerAction, Long> questProgressDao = databaseHelper.getRuntimeExceptionDao(QuestionnaireProgressPerAction.class);
            RuntimeExceptionDao<Question, Long> questionDao = databaseHelper.getRuntimeExceptionDao(Question.class);
            RuntimeExceptionDao<ClosedAnswer, Long> closedAnswerDao = databaseHelper.getRuntimeExceptionDao(ClosedAnswer.class);


            TaskFlat taskFlat = new TaskFlat();
            taskFlat.setId(task.getTask().getId());
            taskFlat.setCanBeRefused(task.getTask().getCanBeRefused());
            taskFlat.setDeadline(task.getTask().getDeadline());
            taskFlat.setDescription(task.getTask().getDescription());
            taskFlat.setDuration(task.getTask().getDuration());
            taskFlat.setLatitude(task.getTask().getLatitude());
            taskFlat.setLongitude(task.getTask().getLongitude());
            taskFlat.setName(task.getTask().getName());
            taskFlat.setPoints(task.getTask().getPoints());
            taskFlat.setRadius(task.getTask().getRadius());
            taskFlat.setSensingDuration(task.getTask().getSensingDuration());
            taskFlat.setStart(task.getTask().getStart());
            taskFlat.setType(task.getTask().getType());

            taskFlatDao.createIfNotExists(taskFlat);

            for (it.unibo.participact.domain.persistence.ActionFlat actionOld : task.getTask().getActions()) {

                ActionFlat action = new ActionFlat();
                action.setDescription(actionOld.getDescription());
                action.setDuration_threshold(actionOld.getDuration_threshold());
                action.setId(actionOld.getId());
                action.setInput_type(actionOld.getInput_type());
                action.setName(actionOld.getName());
                action.setNumeric_threshold(actionOld.getNumeric_threshold());
                action.setTitle(actionOld.getTitle());
                action.setType(ActionType.convertFrom(actionOld.getType()));
                 /*alessandro*/
//                action.setDescriptionGeofence(actionOld.getDescriptionGeofence());
//                action.setInterestPointString(actionOld.getInterestPointString());
                /***********************/
                action.setTask(taskFlat);
                actionDao.createIfNotExists(action);

                if (actionOld.getQuestions() != null) {
                    for (it.unibo.participact.domain.persistence.Question questionOld : actionOld.getQuestions()) {
                        Question question = new Question();
                        question.setId(questionOld.getId());
                        question.setIsClosedAnswers(questionOld.getIsClosedAnswers());
                        question.setIsMultipleAnswers(questionOld.getIsMultipleAnswers());
                        question.setQuestion(questionOld.getQuestion());
                        question.setQuestionOrder(questionOld.getQuestionOrder());

                        question.setActionFlat(action);
                        questionDao.createIfNotExists(question);

                        for (it.unibo.participact.domain.persistence.ClosedAnswer answerOld : questionOld.getClosed_answers()) {
                            ClosedAnswer answer = new ClosedAnswer();
                            answer.setAnswerDescription(answerOld.getAnswerDescription());
                            answer.setAnswerOrder(answerOld.getAnswerOrder());
                            answer.setId(answerOld.getId());

                            answer.setQuestion(question);
                            closedAnswerDao.createIfNotExists(answer);
                        }
                    }
                }
            }

            taskFlatDao.refresh(taskFlat);

            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setId(taskFlat.getId());
            taskStatus.setAcceptedTime(task.getAcceptedTime());
            taskStatus.setLastCheckedTimestamp(task.getLastCheckedTimestamp());
            taskStatus.setPhotoProgress(task.getPhotoProgress());
            taskStatus.setQuestionnaireProgress(task.getQuestionnaireProgress());
            taskStatus.setSensingProgress(task.getSensingProgress());
            taskStatus.setState(task.getState());
            taskStatus.setPhotoThreshold(task.getPhotoThreshold());
            taskStatus.setQuestionnaireThreshold(task.getQuestionnaireThreshold());
            taskStatus.setTask(taskFlat);
            taskStatusdao.createIfNotExists(taskStatus);

            for (Entry<Long, Boolean> entry : task.getQuestionnaireProgressPerAction().entrySet()) {
                QuestionnaireProgressPerAction questProgress = new QuestionnaireProgressPerAction();
                questProgress.setDone(entry.getValue());
                ActionFlat action = actionDao.queryForId(entry.getKey());

                questProgress.setAction(action);
                questProgress.setTaskStatus(taskStatus);

                questProgressDao.createIfNotExists(questProgress);
            }

            for (Entry<Long, Integer> entry : task.getRemainingPhotoPerAction().entrySet()) {

                RemainingPhotoPerAction remPhoto = new RemainingPhotoPerAction();
                remPhoto.setRemaingPhoto(entry.getValue());

                ActionFlat action = actionDao.queryForId(entry.getKey());
                remPhoto.setAction(action);
                remPhoto.setTaskStatus(taskStatus);

                remainPhotoDao.createIfNotExists(remPhoto);
            }

            taskStatusdao.refresh(taskStatus);
            taskStatusdao.update(taskStatus);

            logger.info("Successfully converted task with id {} and state {} to new db format state.", task.getTask().getId());
        } finally {
            OpenHelperManager.releaseHelper();
        }

    }

    public static synchronized TaskFlat addTask(Context context, it.unibo.participact.domain.rest.TaskFlat task) {
        try {

            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskFlat, Long> taskFlatDao = databaseHelper.getRuntimeExceptionDao(TaskFlat.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);
            RuntimeExceptionDao<ActionFlat, Long> actionDao = databaseHelper.getRuntimeExceptionDao(ActionFlat.class);
            RuntimeExceptionDao<RemainingPhotoPerAction, Long> remainPhotoDao = databaseHelper.getRuntimeExceptionDao(RemainingPhotoPerAction.class);
            RuntimeExceptionDao<QuestionnaireProgressPerAction, Long> questProgressDao = databaseHelper.getRuntimeExceptionDao(QuestionnaireProgressPerAction.class);
            RuntimeExceptionDao<Question, Long> questionDao = databaseHelper.getRuntimeExceptionDao(Question.class);
            RuntimeExceptionDao<ClosedAnswer, Long> closedAnswerDao = databaseHelper.getRuntimeExceptionDao(ClosedAnswer.class);
            RuntimeExceptionDao<InterestPoint, Long> interestPointDao = databaseHelper.getRuntimeExceptionDao(InterestPoint.class);
//            databaseHelper.onCreate(OpenHelperManager.getHelper(context, DomainDBHelper.class).getWritableDatabase(), OpenHelperManager.getHelper(context, DomainDBHelper.class).getConnectionSource());
            if (taskFlatDao.idExists(task.getId())) {
                return taskFlatDao.queryForId(task.getId());
            }


            TaskFlat taskFlat = new TaskFlat();
            taskFlat.setId(task.getId());
            taskFlat.setCanBeRefused(task.getCanBeRefused());
            taskFlat.setDeadline(task.getDeadline());
            taskFlat.setDescription(task.getDescription());
            taskFlat.setDuration(task.getDuration());
            taskFlat.setLatitude(task.getLatitude());
            taskFlat.setLongitude(task.getLongitude());
            taskFlat.setName(task.getName());
            taskFlat.setPoints(task.getPoints());
            taskFlat.setRadius(task.getRadius());
            taskFlat.setSensingDuration(task.getSensingDuration());
            taskFlat.setStart(task.getStart());
            taskFlat.setType(task.getType());
            taskFlat.setNotificationArea(task.getNotificationArea());
            taskFlat.setActivationArea(task.getActivationArea());
            taskFlatDao.createIfNotExists(taskFlat);

            for (it.unibo.participact.domain.rest.ActionFlat actionflat : task.getActions()) {

                ActionFlat action = new ActionFlat();
                action.setDescription(actionflat.getDescription());
                action.setDuration_threshold(actionflat.getDuration_threshold());
                action.setId(actionflat.getId());
                action.setInput_type(actionflat.getInput_type());
                action.setName(actionflat.getName());
                action.setNumeric_threshold(actionflat.getNumeric_threshold());
                action.setTitle(actionflat.getTitle());
                action.setType(actionflat.getType());

                /*alessandro*/
                InterestPoint interestPoint = new InterestPoint();
                if (action.getType() == ActionType.GEOFENCE) {
                    interestPoint.setDesctioprionGeofence(actionflat.getDescriptionGeofence());
                    interestPoint.setInterestPointString(actionflat.getInterestPointString());
                    interestPoint.setActionFlatId(action.getId());
                    interestPoint.setCollected(false);
                    interestPointDao.createIfNotExists(interestPoint);
                }
                /***********************/


                action.setTask(taskFlat);

                actionDao.createIfNotExists(action);

                if (actionflat.getQuestions() != null) {
                    for (it.unibo.participact.domain.rest.Question questionOld : actionflat.getQuestions()) {
                        Question question = new Question();
                        question.setId(questionOld.getId());
                        question.setIsClosedAnswers(questionOld.getIsClosedAnswers());
                        question.setIsMultipleAnswers(questionOld.getIsMultipleAnswers());
                        question.setQuestion(questionOld.getQuestion());
                        question.setQuestionOrder(questionOld.getQuestionOrder());

                        question.setActionFlat(action);
                        questionDao.createIfNotExists(question);

                        for (it.unibo.participact.domain.rest.ClosedAnswer answerOld : questionOld.getClosed_answers()) {
                            ClosedAnswer answer = new ClosedAnswer();
                            answer.setAnswerDescription(answerOld.getAnswerDescription());
                            answer.setAnswerOrder(answerOld.getAnswerOrder());
                            answer.setId(answerOld.getId());

                            answer.setQuestion(question);
                            closedAnswerDao.createIfNotExists(answer);
                        }
                    }
                }
            }

            taskFlatDao.refresh(taskFlat);
            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setId(taskFlat.getId());
            taskStatus.setState(TaskState.UNKNOWN);

            taskStatus.setTask(taskFlat);
            taskStatusdao.createIfNotExists(taskStatus);
            taskStatusdao.refresh(taskStatus);

            for (it.unibo.participact.domain.rest.ActionFlat action : task.getActions()) {
                if (action.getType() == ActionType.PHOTO) {
//					photoThreshold += action.getNumeric_threshold();
//					remainingPhotoPerAction.put(action.getId(), action.getNumeric_threshold());

                    taskStatus.photoThreshold++;
                    RemainingPhotoPerAction remPhoto = new RemainingPhotoPerAction();
                    remPhoto.setRemaingPhoto(action.getNumeric_threshold());
                    ActionFlat actionFlat = actionDao.queryForId(action.getId());
                    remPhoto.setAction(actionFlat);
                    remPhoto.setTaskStatus(taskStatus);

                    remainPhotoDao.createIfNotExists(remPhoto);
                    taskStatusdao.update(taskStatus);
                }


                if (action.getType() == ActionType.QUESTIONNAIRE) {
//					questionnaireThreshold ++;
//					questionnaireProgressPerAction.put(action.getId(), false);
                    taskStatus.questionnaireThreshold++;
                    QuestionnaireProgressPerAction questProgress = new QuestionnaireProgressPerAction();
                    questProgress.setDone(false);
                    ActionFlat actionFlat = actionDao.queryForId(action.getId());
                    questProgress.setAction(actionFlat);
                    questProgress.setTaskStatus(taskStatus);

                    questProgressDao.createIfNotExists(questProgress);
                    taskStatusdao.update(taskStatus);
                }

                if (action.getType() == ActionType.ACTIVITY_DETECTION) {
                    taskStatus.setActivityDetectionDuration(action.getDuration_threshold());
                    taskStatusdao.update(taskStatus);
                }
            }

            taskStatusdao.update(taskStatus);
            taskFlatDao.update(taskFlat);

            logger.info("Successfully added task with id {} in db.", task.getId());

            return taskFlat;
        } catch (Exception e) {
            logger.warn("Exception adding task with id {} in db.", task.getId());
            return null;
        } finally {
            OpenHelperManager.releaseHelper();
        }

    }

    public static synchronized TaskFlat getTaskById(Context context, Long id) {
        try {

            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskFlat, Long> taskFlatDao = databaseHelper.getRuntimeExceptionDao(TaskFlat.class);
            return taskFlatDao.queryForId(id);

        } catch (Exception e) {
            logger.warn("Exception getting task with id {} in db.", id);
            return null;
        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    public static synchronized boolean setInterestPointCollected(Context context, InterestPoint interestPoint){
        boolean result = false;
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<InterestPoint, Long> interestPointDao= databaseHelper.getRuntimeExceptionDao(InterestPoint.class);
            InterestPoint currentInterestPoint = interestPointDao.queryForId(interestPoint.getId());
            currentInterestPoint.setCollected(true);

            interestPointDao.update(currentInterestPoint);
            logger.info("the interest point id:{} is set Collected");
            result = true;

        }finally {
            OpenHelperManager.releaseHelper();
        }
        return result;

    }

    public static synchronized boolean isInterestPointCollected(Context contex, InterestPoint interestPoint){
        boolean result = false;
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(contex, DomainDBHelper.class);
            RuntimeExceptionDao<InterestPoint, Long> interestPointDao= databaseHelper.getRuntimeExceptionDao(InterestPoint.class);
            InterestPoint currentInterestPoint = interestPointDao.queryForId(interestPoint.getId());
            if(currentInterestPoint.isCollected())
                result = true;

        }finally {
            OpenHelperManager.releaseHelper();
        }
        return result;
    }


    public static synchronized void changeTaskState(Context context, TaskFlat task, TaskState newState) {
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);

            for (TaskStatus t : taskStatusdao.queryForAll()) {
                if (t.getTask().getId().equals(task.getId())) {
                    TaskState oldState = t.getState();
                    if (t.getAcceptedTime() == null && (newState == TaskState.RUNNING || newState == TaskState.RUNNING_BUT_NOT_EXEC)) {
                        t.setAcceptedTime(new DateTime());
                    }
                    transactionToState(context, task, oldState, newState);
                    t.setState(newState);
                    taskStatusdao.update(t);
                    logger.info("Successfully changed state of task with id {} from {} to {}.", task.getId(), oldState, newState);
                }

            }

        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    public static void transactionToState(Context context, TaskFlat task, TaskState oldState,
                                          TaskState newState) {

        if (oldState == newState) {
            return;
        }

        switch (newState) {
            case RUNNING:
                TaskService.activateTask(context, task);
                break;
            case RUNNING_BUT_NOT_EXEC:
                if (oldState == TaskState.RUNNING) {
                    TaskService.suspendTask(context, task);
                }
                break;
            case SUSPENDED:
                TaskService.suspendTask(context, task);
                break;
            case ERROR:
                if (oldState == TaskState.RUNNING) {
                    TaskService.suspendTask(context, task);
                }
                break;
            case COMPLETED_NOT_SYNC_WITH_SERVER:
                if (oldState == TaskState.RUNNING) {
                    TaskService.suspendTask(context, task);
                }
                break;
            case GEO_NOTIFIED_AVAILABLE:
                if (oldState == TaskState.HIDDEN) {
                    NotificationUtility.addNotification(context, R.drawable.ic_new_task, context.getString(R.string.participact_notification), context.getString(R.string.new_tasks_notification), GcmBroadcastReceiver.NOTIFICATION_NEW_TASK);
                }
                break;

            default:
                break;
        }
    }

    public static synchronized void removeTask(Context context, TaskFlat task) {
        try {

            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskFlat, Long> taskFlatDao = databaseHelper.getRuntimeExceptionDao(TaskFlat.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);
            RuntimeExceptionDao<ActionFlat, Long> actionDao = databaseHelper.getRuntimeExceptionDao(ActionFlat.class);
            RuntimeExceptionDao<InterestPoint, Long> interestPointDao = databaseHelper.getRuntimeExceptionDao(InterestPoint.class);
            RuntimeExceptionDao<RemainingPhotoPerAction, Long> remainPhotoDao = databaseHelper.getRuntimeExceptionDao(RemainingPhotoPerAction.class);
            RuntimeExceptionDao<QuestionnaireProgressPerAction, Long> questProgressDao = databaseHelper.getRuntimeExceptionDao(QuestionnaireProgressPerAction.class);

            for (TaskStatus t : taskStatusdao.queryForAll()) {
                if (t.getTask().getId().equals(task.getId())) {

                    remainPhotoDao.delete(t.getRemainingPhotoPerAction());
                    questProgressDao.delete(t.getQuestionnaireProgressPerAction());
                    actionDao.delete(task.getActions());
                    taskStatusdao.delete(t);
                    for(ActionFlat actionFlat : task.getActions()){
                        if(actionFlat.getType().equals(ActionType.GEOFENCE)){
                            InterestPoint interestPoint =  interestPointDao.queryForEq("actionFlatId",actionFlat.getId()).get(0);
                            interestPointDao.delete(interestPoint);

                        }
                    }
                    taskFlatDao.delete(task);

                    logger.info("Successfully removed task with id {} from local state.", task.getId());
                    return;
                }
            }

        } finally {
            OpenHelperManager.releaseHelper();
        }
    }
    public static synchronized List<ActionFlat> getActionByType(Context context, ActionType actionType) {

        List<ActionFlat> result = new ArrayList<ActionFlat>();
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<ActionFlat, Long> actionFlatDao = databaseHelper.getRuntimeExceptionDao(ActionFlat.class);

            for (ActionFlat a : actionFlatDao.queryForAll()) {
                if (a.getType().equals(ActionType.GEOFENCE)) {
                    result.add(a);
                }
            }

        } finally {
            OpenHelperManager.releaseHelper();
        }
        return result;
    }


    public static synchronized List<GeoBadgeCollected> getBadgeCollected(Context context){
        List<GeoBadgeCollected> result = new ArrayList<GeoBadgeCollected>();
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<GeoBadgeCollected, Long> geoCollectedBadgeDao= databaseHelper.getRuntimeExceptionDao(GeoBadgeCollected.class);
            for(GeoBadgeCollected geoBadgeCollected : geoCollectedBadgeDao.queryForAll()){
                result.add(geoBadgeCollected);
            }

        }finally {
            OpenHelperManager.releaseHelper();
        }
        return result;
    }

    public static synchronized int removeCollectedBadge(Context context,GeoBadgeCollected geoBadgeCollected){
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<GeoBadgeCollected, Long> geoCollectedBadgeDao= databaseHelper.getRuntimeExceptionDao(GeoBadgeCollected.class);
            return geoCollectedBadgeDao.delete(geoBadgeCollected);

        }finally {
            OpenHelperManager.releaseHelper();
        }
    }


    public static synchronized  void updateCollectedgeobadgeNotSync(Context context){
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<GeoBadgeCollected, Long> geoBadgeCollectedDao= databaseHelper.getRuntimeExceptionDao(GeoBadgeCollected.class);
            for(GeoBadgeCollected geo : geoBadgeCollectedDao.queryForAll()){
                syncBadgeCollected(context,geo);
            }

        }finally {
            OpenHelperManager.releaseHelper();
        }

    }

    public static synchronized  boolean addOnGeoBadgeCollected(Context context, GeoBadgeCollected geoBadge, InterestPoint interestPoint){

        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<GeoBadgeCollected, Long> geoCollectedBadgeDao= databaseHelper.getRuntimeExceptionDao(GeoBadgeCollected.class);
            RuntimeExceptionDao<InterestPoint, Long> interestPointDao= databaseHelper.getRuntimeExceptionDao(InterestPoint.class);
            GeoBadgeCollected geoBadgeCollected;
            if((geoBadgeCollected=geoCollectedBadgeDao.createIfNotExists(geoBadge)) != null){
                interestPoint.setCollected(true);
                interestPointDao.update(interestPoint);
                syncBadgeCollected(context,geoBadgeCollected);
                return true;
            }
            return false;

        }catch (Exception e){
            return false;
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }



    public static synchronized List<InterestPoint> getInterestPointByActionFlat(Context context, Long actionFlatId){
        List<InterestPoint> result = new ArrayList<InterestPoint>();
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<InterestPoint, Long> interestPointDao= databaseHelper.getRuntimeExceptionDao(InterestPoint.class);
            for(InterestPoint interestPoint : interestPointDao.queryForAll()){
                if(interestPoint.getActionFlatId().equals(actionFlatId)){
                    result.add(interestPoint);
                }
            }

        }finally {
            OpenHelperManager.releaseHelper();
        }
        return result;
    }



    public static synchronized List<TaskFlat> getTaskByState(Context context, TaskState taskState) {

        List<TaskFlat> result = new ArrayList<TaskFlat>();
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);

            for (TaskStatus t : taskStatusdao.queryForAll()) {
                if (t.getState() == taskState) {
                    result.add(t.getTask());
                }
            }

        } finally {
            OpenHelperManager.releaseHelper();
        }
        return result;
    }

    public static synchronized List<TaskStatus> getTaskStatusByState(Context context, TaskState taskState) {

        List<TaskStatus> result = new ArrayList<TaskStatus>();
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);

            for (TaskStatus t : taskStatusdao.queryForAll()) {
                if (t.getState() == taskState) {
                    result.add(t);
                }
            }

        } finally {
            OpenHelperManager.releaseHelper();
        }
        return result;
    }


    public static synchronized void activateAllTask(Context context, TaskState taskState) {

        for (TaskFlat task : getTaskByState(context, taskState)) {
            changeTaskState(context, task, TaskState.RUNNING);
        }
        logger.info("Activating all tasks in state {}.", taskState);
    }

    public static synchronized void suspendAllTask(Context context, TaskState taskState) {

        for (TaskFlat task : getTaskByState(context, taskState)) {
            changeTaskState(context, task, TaskState.SUSPENDED);
        }
        logger.info("Deactivating all tasks in state {}.", taskState);
    }

    public static synchronized void freezeAllTask(Context context) {

        for (TaskFlat task : getTaskByState(context, TaskState.SUSPENDED)) {
            logger.info("Freezing suspended task with id {}.", task.getId());
            changeTaskState(context, task, TaskState.ERROR);
        }
        for (TaskFlat task : getTaskByState(context, TaskState.RUNNING)) {
            logger.info("Freezing running task with id {}.", task.getId());
            changeTaskState(context, task, TaskState.ERROR);
        }
        logger.info("Freezed all tasks.");
    }

    public static synchronized void defreezeAllTask(Context context) {

        for (TaskFlat task : getTaskByState(context, TaskState.ERROR)) {
            logger.info("Defreezing running task with id {}.", task.getId());
            changeTaskState(context, task, TaskState.RUNNING);
        }
        logger.info("Defreezed all tasks.");
    }

    public static synchronized void incrementSensingProgress(Context context) {
        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);
            Long timestamp = System.currentTimeMillis();

            for (TaskStatus task : getTaskStatusByState(context, TaskState.COMPLETED_NOT_SYNC_WITH_SERVER)) {
                completeTask(context, task);
            }

            for (TaskStatus task : getTaskStatusByState(context, TaskState.RUNNING_BUT_NOT_EXEC)) {
                if (task.isExpired()) {
                    completeTask(context, task);
                }
            }

            for (TaskStatus task : getTaskStatusByState(context, TaskState.HIDDEN)) {
                if (task.getTask().getDeadline().isBefore(new DateTime())) {
                    removeTask(context, task.getTask());
                }
            }

            for (TaskStatus task : getTaskStatusByState(context, TaskState.GEO_NOTIFIED_AVAILABLE)) {
                if (task.getTask().getDeadline().isBefore(new DateTime())) {
                    removeTask(context, task.getTask());
                }
            }

            for (TaskStatus task : getTaskStatusByState(context, TaskState.SUSPENDED)) {
                if (task.isExpired()) {
                    AlarmStateUtility.removeAlarm(context.getApplicationContext(), task.getTask().getId());
                    completeTask(context, task);
                }
            }

            for (TaskStatus task : getTaskStatusByState(context, TaskState.RUNNING)) {
                task.incrementSensingProgress(timestamp);
                taskStatusdao.update(task);

                if (task.isExpired()) {
                    completeTask(context, task);
                }
            }

        } finally {
            OpenHelperManager.releaseHelper();
        }
    }


    public static synchronized void incrementPhotoProgress(Context context, TaskFlat task, Long actionId) {
        incrementPhotoProgress(context, task.getId(), actionId);
    }

    public static synchronized void incrementPhotoProgress(Context context, Long taskId, Long actionId) {

        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<RemainingPhotoPerAction, Long> remainPhotoDao = databaseHelper.getRuntimeExceptionDao(RemainingPhotoPerAction.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);

            for (RemainingPhotoPerAction r : remainPhotoDao.queryForAll()) {
                TaskStatus taskStatus = r.getTaskStatus();
                taskStatusdao.refresh(taskStatus);
                remainPhotoDao.refresh(r);
                if (r.getTaskStatus().getTask().getId().equals(taskId) && r.getAction().getId().equals(actionId)) {

                    if (r.getTaskStatus().getState() == TaskState.RUNNING) {
                        r.remaingPhoto--;
                        r.getTaskStatus().photoProgress++;
                        logger.info("Incremented photo progress of task with id {} and action id {}", taskId, actionId);
                        remainPhotoDao.update(r);
                        taskStatusdao.update(r.getTaskStatus());
                    } else {
                        //delete photo
                        File[] files = ImageDescriptorUtility.getImageDescriptors(context);
                        for (File file : files) {
                            ImageDescriptor imgDescr = ImageDescriptorUtility.loadImageDescriptor(context, file.getName());
                            if (imgDescr.getTaskId().equals(taskId) && imgDescr.getActionId().equals(actionId)) {
                                ImageDescriptorUtility.deleteImageDescriptorAndRelatedImage(context, imgDescr.getImageName());
                                logger.info("Deleted photo of task with id {} and action id {} because taken not in activation area.", taskId, actionId);
                            }
                        }
                    }

                    if (r.getTaskStatus().isExpired()) {
                        completeTask(context, r.getTaskStatus());
                    }
                }
            }

        } finally {
            OpenHelperManager.releaseHelper();
        }

    }

    public static synchronized void incrementQuestionnaireProgress(Context context, TaskFlat task, Long actionId) {
        incrementQuestionnaireProgress(context, task.getId(), actionId);
    }

    public static synchronized void incrementQuestionnaireProgress(Context context, Long taskId, Long actionId) {

        try {
            DomainDBHelper databaseHelper = OpenHelperManager.getHelper(context, DomainDBHelper.class);
            RuntimeExceptionDao<QuestionnaireProgressPerAction, Long> questProgressDao = databaseHelper.getRuntimeExceptionDao(QuestionnaireProgressPerAction.class);
            RuntimeExceptionDao<TaskStatus, Long> taskStatusdao = databaseHelper.getRuntimeExceptionDao(TaskStatus.class);

            for (QuestionnaireProgressPerAction q : questProgressDao.queryForAll()) {
                if (q.getTaskStatus().getTask().getId().equals(taskId) && q.getAction().getId().equals(actionId)) {

                    q.setDone(true);
                    q.getTaskStatus().questionnaireProgress++;
                    logger.info("Incremented questionnaire progress of task with id {} and action id {}", taskId, actionId);
                    questProgressDao.update(q);
                    taskStatusdao.update(q.getTaskStatus());

                    if (q.getTaskStatus().isExpired()) {
                        completeTask(context, q.getTaskStatus());
                    }
                }
            }

        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    private static void syncBadgeCollected(Context context, GeoBadgeCollected geoBadgeCollected){
        try {
            logger.info("Trying to sync badge collected with id {}.", geoBadgeCollected.getId());
            logger.info("Geobadge progress: description={}, task id={}, action id={}", geoBadgeCollected.getDesctioprionGeofence(),geoBadgeCollected.getTaskId(), geoBadgeCollected.getActionFlatId());



            //send new state at server
            SpiceManager contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
            if (!contentManager.isStarted()) {
                contentManager.start(context.getApplicationContext());
            }

            logger.info("Sending GeoBadge id {}. Result success.", geoBadgeCollected.getId());
            CollectedBadgeRequest request = new CollectedBadgeRequest(context,geoBadgeCollected);
            contentManager.execute(request, request.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new CollectedBadgeListener(context, geoBadgeCollected));


        } catch (Exception e) {
            logger.warn("Exception collected geobadge with id {}.", geoBadgeCollected.getId());
        }
    }



    private static void completeTask(Context context, TaskStatus status) {
        try {
            logger.info("Trying to complete task with id {}.", status.getTask().getId());
            logger.info("Task accept time + task duration = {} + {}.", status.getAcceptedTime(), status.getTask().getDuration());
            logger.info("Task progress: sensing progress={}, task photo progress={}, task questionnaire progress={}", status.getSensingProgress(), status.getPhotoProgress(), status.getQuestionnaireProgress());



            if (status.getState() != TaskState.COMPLETED_NOT_SYNC_WITH_SERVER) {
                changeTaskState(context, status.getTask(), TaskState.COMPLETED_NOT_SYNC_WITH_SERVER);
            }

            //send new state at server
            SpiceManager contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
            if (!contentManager.isStarted()) {
                contentManager.start(context.getApplicationContext());
            }
            //controllo se c'e almeno un action Geofence
            boolean geogenceType = false;
            for (ActionFlat a :status.getTask().getActions()){
                if(a.getType().equals(ActionType.GEOFENCE))
                    geogenceType=true;


            }



            if (status.isCompleted()) {
                if(!status.isCollected(context) && geogenceType){
                    //actionGeofence && notCollected task failure
                    logger.info("Sending final task state of task with id {}. Result unsuccess.", status.getTask().getId());
                    CompleteWithFailureTaskRequest request = new CompleteWithFailureTaskRequest(context, status);
                    contentManager.execute(request, request.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new CompleteTaskListener(context, status.getTask()));
                }else{
                    logger.info("Sending final task state of task with id {}. Result success.", status.getTask().getId());
                    CompleteWithSuccessTaskRequest request = new CompleteWithSuccessTaskRequest(context, status.getTask().getId());
                    contentManager.execute(request, request.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new CompleteTaskListener(context, status.getTask()));}

            } else {
                logger.info("Sending final task state of task with id {}. Result unsuccess.", status.getTask().getId());
                CompleteWithFailureTaskRequest request = new CompleteWithFailureTaskRequest(context, status);
                contentManager.execute(request, request.createCacheKey(), DurationInMillis.ALWAYS_EXPIRED, new CompleteTaskListener(context, status.getTask()));
            }

        } catch (Exception e) {
            logger.warn("Exception completing task with id {}.", status.getTask().getId());
        }
    }


}
