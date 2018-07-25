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

public class LocationAwareTask extends Task {

    private static final long serialVersionUID = -6855620555493420665L;

    private Double latitude;

    private Double longitude;

    private Double radius;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public String toString() {
        String actions = "";
        for (it.unibo.participact.domain.rest.Action act : getActions()) {
            actions += act.getName() + " ";
        }
        return String.format("%s Id:%s Name:%s DeadLine:%s Points:%s Latitude:%s Longitude:%s Actions:%s", Task.class.getSimpleName(), getId(), getName(), getDeadline(), getPoints(), getLatitude(), getLongitude(), actions);
    }

    public TaskFlat convertToTaskFlat() {
        return new TaskFlat(this);
    }
}