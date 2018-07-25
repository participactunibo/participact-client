/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.data;

import it.unibo.participact.domain.persistence.ClosedAnswer;
import it.unibo.participact.domain.persistence.Question;


public class DataQuestionaireClosedAnswer extends Data {

    private static final long serialVersionUID = -1900866387953714053L;

    private ClosedAnswer closedAnswer;

    private boolean answer_value;

    public ClosedAnswer getClosedAnswer() {
        return closedAnswer;
    }

    public void setClosedAnswer(ClosedAnswer closedAnswer) {
        this.closedAnswer = closedAnswer;
    }

    public boolean isAnswer_value() {
        return answer_value;
    }

    public void setAnswer_value(boolean answer_value) {
        this.answer_value = answer_value;
    }

    public Question getQuestion() {
        return closedAnswer.getQuestion();
    }


}
