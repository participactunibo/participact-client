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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import it.unibo.participact.domain.data.Data;


public class TaskResult implements Serializable {

    private static final long serialVersionUID = -8735101887225128L;

    private Long id;

    private TaskReport taskReport;

    private Set<Data> data = new HashSet<Data>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaskReport getTaskReport() {
        return taskReport;
    }

    public void setTaskReport(TaskReport taskReport) {
        this.taskReport = taskReport;
    }

    public Set<Data> getData() {
        return data;
    }

    public void setData(Set<Data> data) {
        this.data = data;
    }

}
