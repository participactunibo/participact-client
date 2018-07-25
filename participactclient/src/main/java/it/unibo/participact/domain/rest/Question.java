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


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Question implements Serializable {

    private static final long serialVersionUID = 4337352852760317052L;

    private Long id;

    private String question;

    private Integer question_order;

    private List<ClosedAnswer> closed_answers;

    private Boolean isClosedAnswers;

    private Boolean isMultipleAnswers;

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
        closed_answers = new LinkedList<ClosedAnswer>();
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

    public List<ClosedAnswer> getClosed_answers() {
        return closed_answers;
    }

    public void setClosed_answers(List<ClosedAnswer> closed_answers) {
        this.closed_answers = closed_answers;
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
