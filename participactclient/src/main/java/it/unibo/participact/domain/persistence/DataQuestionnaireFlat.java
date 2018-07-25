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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class DataQuestionnaireFlat {

    public static final int TYPE_OPEN_ANSWER = 0;
    public static final int TYPE_CLOSED_ANSWER = 1;

    @DatabaseField(generatedId = true)
    Long id;
    @DatabaseField
    Long taskId;
    @DatabaseField
    Long actionId;
    @DatabaseField
    Long questionId;
    @DatabaseField
    Integer type; // 0 open, 1 closed
    @DatabaseField
    Long answerId;
    @DatabaseField
    Boolean closedAnswerValue;
    @DatabaseField
    String openAnswerValue;
    @DatabaseField
    Long timestamp;

    public DataQuestionnaireFlat() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Boolean getClosedAnswerValue() {
        return closedAnswerValue;
    }

    public void setClosedAnswerValue(Boolean closedAnswerValue) {
        this.closedAnswerValue = closedAnswerValue;
    }

    public String getOpenAnswerValue() {
        return openAnswerValue;
    }

    public void setOpenAnswerValue(String openAnswerValue) {
        this.openAnswerValue = openAnswerValue;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
