/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.persistence.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.persistence.TaskStatus;

public class State implements Serializable {

    private static final long serialVersionUID = 3222104785874040804L;

    private Map<Long, TaskStatus> tasks;

    public State(List<TaskStatus> list) {

        tasks = new HashMap<Long, TaskStatus>();
        for (TaskStatus taskStatus : list) {
            tasks.put(taskStatus.getTask().getId(), taskStatus);
        }

    }

    public List<TaskFlat> getTaskByState(TaskState state) {
        List<TaskFlat> result = new ArrayList<TaskFlat>();
        for (TaskStatus taskStatus : tasks.values()) {
            if (state == TaskState.ANY) {
                result.add(taskStatus.getTask());
            } else {
                if (taskStatus.getState() == state) {
                    result.add(taskStatus.getTask());
                }
            }
        }
        return result;
    }

    public List<TaskStatus> getTaskStatusByState(TaskState state) {
        List<TaskStatus> result = new ArrayList<TaskStatus>();
        for (TaskStatus taskStatus : tasks.values()) {
            if (taskStatus.getState() == state) {
                result.add(taskStatus);
            }
        }
        return result;
    }

    public TaskStatus getTaskById(Long id) {
        return tasks.get(id);
    }

    public Map<Long, TaskStatus> getTasks() {
        return tasks;
    }


}
