/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.questionnaire;

import android.support.v4.app.Fragment;

import it.unibo.participact.domain.persistence.Question;

public class FragmentFactory {

    public static Fragment getFragment(Question question) {
        if (question.getIsClosedAnswers()) {
            if (question.getIsMultipleAnswers()) {
                //multi
                return MultipleChoiceFragment.create(question);
            } else {
                return SingleChoiceFragment.create(question);
            }
        } else {
            //open
            return TextFragment.create(question);
        }
    }
}
