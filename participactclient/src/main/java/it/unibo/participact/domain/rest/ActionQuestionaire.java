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

import java.util.LinkedList;
import java.util.List;


public class ActionQuestionaire extends Action {

    private static final long serialVersionUID = -2977490148767458942L;

    private List<Question> questions;

    private String title;

    private String description;

    public ActionQuestionaire() {
        questions = new LinkedList<Question>();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActionFlat convertToActionFlat() {
        return new ActionFlat(this);
    }

    @JsonIgnore
    public int getNextQuestionOrder() {
        if (questions == null) {
            throw new IllegalStateException("questions can't be null");
        }
        if (questions.size() == 0) {
            return 0;
        }
        int max = 0;
        for (Question q : questions) {
            if (q.getQuestionOrder() > max) {
                max = q.getQuestionOrder();
            }
        }
        max = max + 1;
        return max;
    }
}