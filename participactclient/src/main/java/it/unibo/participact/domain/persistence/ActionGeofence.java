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


public class ActionGeofence extends Action {

    private String descriptionGeofence;
    private String interestPointString;

    public String getDescriptionGeofence() {
        return descriptionGeofence;
    }

    public void setDescriptionGeofence(String description) {
        this.descriptionGeofence = description;
    }


    public ActionFlat convertToActionFlat() {
        return new ActionFlat(this);
    }

    public String getInterestPointString() {
        return interestPointString;
    }

    public void setInterestPointString(String interestPoints) {
        this.interestPointString = interestPoints;
    }


}
