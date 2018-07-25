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

import java.io.Serializable;

@DatabaseTable
public class RemainingPhotoPerAction implements Serializable {

    private static final long serialVersionUID = 2543115181050810886L;

    @DatabaseField(generatedId = true)
    Long id;

    @DatabaseField(foreign = true)
    ActionFlat action;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    TaskStatus taskStatus;

    @DatabaseField
    int remaingPhoto;

    public RemainingPhotoPerAction() {

    }

    public RemainingPhotoPerAction(ActionFlat action, int remaingPhoto) {
        super();
        this.action = action;
        this.remaingPhoto = remaingPhoto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActionFlat getAction() {
        return action;
    }

    public void setAction(ActionFlat action) {
        this.action = action;
    }

    public int getRemaingPhoto() {
        return remaingPhoto;
    }

    public void setRemaingPhoto(int remaingPhoto) {
        this.remaingPhoto = remaingPhoto;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

}
