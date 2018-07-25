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
import org.parceler.Parcel;

import java.util.Set;

/**
 * Created by alessandro on 25/11/14.
 */
@Parcel
public class TaskFlatRequest {

    String name;

    String description;

    Long duration;

    Long sensingDuration;

    Double latitude;

    Double longitude;

    Double radius;

    Boolean canBeRefused;

    String type;

    String notificationArea;

    String activationArea;

    DateTime deadline;

    DateTime start;

    Set<ActionFlatRequest> actions;

    Set<Long> idFriends;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getSensingDuration() {
        return sensingDuration;
    }

    public void setSensingDuration(Long sensingDuration) {
        this.sensingDuration = sensingDuration;
    }

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

    public Boolean getCanBeRefused() {
        return canBeRefused;
    }

    public void setCanBeRefused(Boolean canBeRefused) {
        this.canBeRefused = canBeRefused;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotificationArea() {
        return notificationArea;
    }

    public void setNotificationArea(String notificationArea) {
        this.notificationArea = notificationArea;
    }

    public String getActivationArea() {
        return activationArea;
    }

    public void setActivationArea(String activationArea) {
        this.activationArea = activationArea;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(DateTime deadline) {
        this.deadline = deadline;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public Set<ActionFlatRequest> getActions() {
        return actions;
    }

    public void setActions(Set<ActionFlatRequest> actions) {
        this.actions = actions;
    }

    public Set<Long> getIdFriends() {
        return idFriends;
    }

    public void setIdFriends(Set<Long> idFriends) {
        this.idFriends = idFriends;
    }

}
