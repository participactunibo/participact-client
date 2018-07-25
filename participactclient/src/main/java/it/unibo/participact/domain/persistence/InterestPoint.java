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
public class InterestPoint {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField
    private String desctioprionGeofence;

    @DatabaseField
    private String interestPointString;

    @DatabaseField
    private Long actionFlatId;

    @DatabaseField
    private boolean collected;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDesctioprionGeofence() {
        return desctioprionGeofence;
    }

    public void setDesctioprionGeofence(String desctioprionGeofence) {
        this.desctioprionGeofence = desctioprionGeofence;
    }

    public String getInterestPointString() {
        return interestPointString;
    }

    public void setInterestPointString(String interestPointString) {
        this.interestPointString = interestPointString;
    }
    public Long getActionFlatId() {
        return actionFlatId;
    }

    public void setActionFlatId(Long actionFlatId) {
        this.actionFlatId = actionFlatId;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}
