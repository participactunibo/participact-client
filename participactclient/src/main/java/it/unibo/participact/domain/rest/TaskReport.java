/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.rest;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.local.User;
import it.unibo.participact.domain.persistence.Task;


public class TaskReport implements Serializable {

    private static final long serialVersionUID = -6980945555990044969L;

    private Long id;

    private User user;

    private Task task;

    private TaskResult taskResult;

    private List<TaskHistory> history = new ArrayList<TaskHistory>();

    private TaskState currentState = TaskState.UNKNOWN;

    private DateTime acceptedDateTime;

    public TaskReport() {
        taskResult = new TaskResult();
        taskResult.setTaskReport(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<TaskHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TaskHistory> history) {
        this.history = history;
    }

    public TaskResult getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(TaskResult taskResult) {
        this.taskResult = taskResult;
    }

    public TaskState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(TaskState currentState) {
        this.currentState = currentState;
    }

    public DateTime getAcceptedDateTime() {
        return acceptedDateTime;
    }

    public void setAcceptedDateTime(DateTime acceptedDateTime) {
        this.acceptedDateTime = acceptedDateTime;
    }

    public void addHistory(TaskHistory taskHistory) {
        if (taskHistory == null) {
            throw new NullPointerException("taskHistory");
        }
        if (taskHistory.getState() == null) {
            throw new NullPointerException("taskHistory.getState");
        }
        getHistory().add(taskHistory);
        setCurrentState(taskHistory.getState());
    }

    public TaskHistory getTaskHistoryByState(TaskState taskState) {

        if (history == null) {
            return null;
        }

        for (TaskHistory taskHistory : history) {
            if (taskHistory.getState() == taskState) {
                return taskHistory;
            }
        }
        return null;
    }

}
