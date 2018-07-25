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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class Question implements Serializable {

    private static final long serialVersionUID = -21876732993994461L;

    @DatabaseField(id = true)
    private Long id;

    @DatabaseField
    private String question;

    @DatabaseField
    private Integer question_order;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    private ForeignCollection<ClosedAnswer> closed_answers;

    @DatabaseField
    private Boolean isClosedAnswers;

    @DatabaseField
    private Boolean isMultipleAnswers;

    @DatabaseField(foreign = true)
    private ActionFlat actionFlat;

    public Boolean getIsClosedAnswers() {
        return isClosedAnswers;
    }

    public void setIsClosedAnswers(Boolean isClosedAnswers) {
        this.isClosedAnswers = isClosedAnswers;
    }

    public Boolean getIsMultipleAnswers() {
        return isMultipleAnswers;
    }

    public void setIsMultipleAnswers(Boolean isMultipleAnswers) {
        this.isMultipleAnswers = isMultipleAnswers;
    }

    public Question() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getQuestionOrder() {
        return question_order;
    }

    public void setQuestionOrder(int question_order) {
        this.question_order = question_order;
    }

    public ForeignCollection<ClosedAnswer> getClosed_answers() {
        return closed_answers;
    }

    public void setClosed_answers(ForeignCollection<ClosedAnswer> closed_answers) {
        this.closed_answers = closed_answers;
    }

    public ActionFlat getActionFlat() {
        return actionFlat;
    }

    public void setActionFlat(ActionFlat actionFlat) {
        this.actionFlat = actionFlat;
    }

    @JsonIgnore
    public int getNextClosedAnswerOrder() {
        if (closed_answers == null) {
            throw new IllegalStateException("closed answers can't be null");
        }
        if (closed_answers.size() == 0) {
            return 0;
        }
        int max = 0;
        for (ClosedAnswer ca : closed_answers) {
            if (ca.getAnswerOrder() > max) {
                max = ca.getAnswerOrder();
            }
        }
        max = max + 1;
        return max;
    }
}
