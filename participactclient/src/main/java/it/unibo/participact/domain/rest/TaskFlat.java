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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import it.unibo.participact.support.JsonDateTimeDeserializer;
import it.unibo.participact.support.JsonDateTimeSerializer;

@JsonIgnoreProperties(ignoreUnknown = false)
public class TaskFlat implements Serializable {

    private static final long serialVersionUID = 1614961604356012108L;

    private Long id;

    private String name;

    private String description;

    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    private DateTime deadline;

    private Integer points;

    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    private DateTime start;

    private Long duration;

    private Long sensingDuration;

    private Double latitude;

    private Double longitude;

    private Double radius;

    private Boolean canBeRefused;

    private Set<ActionFlat> actions;

    private String notificationArea;

    private String activationArea;

    private String type;

    public TaskFlat() {
    }

    public TaskFlat(Task task) {
        init(task);
    }

    public TaskFlat(LocationAwareTask task) {
        init(task);
        this.latitude = task.getLatitude();
        this.longitude = task.getLongitude();
        this.radius = task.getRadius();
    }

    private void init(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.canBeRefused = task.getCanBeRefused();
        this.setDescription(task.getDescription());
        this.deadline = task.getDeadline();
        this.points = task.getPoints();
        this.type = task.getClass().getSimpleName();
        this.start = task.getStart();
        this.duration = task.getDuration();
        this.sensingDuration = task.getSensingDuration();
        actions = new HashSet<ActionFlat>();
        for (it.unibo.participact.domain.rest.Action action : task.getActions()) {
            actions.add(action.convertToActionFlat());
        }
    }

    public Boolean getCanBeRefused() {
        return canBeRefused;
    }

    public void setCanBeRefused(Boolean canBeRefused) {
        this.canBeRefused = canBeRefused;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(DateTime deadline) {
        this.deadline = deadline;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
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

    public Set<ActionFlat> getActions() {
        return actions;
    }

    public void setActions(Set<ActionFlat> actions) {
        this.actions = actions;
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

}