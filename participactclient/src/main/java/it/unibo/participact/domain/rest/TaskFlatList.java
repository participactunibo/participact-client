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
import java.util.ArrayList;
import java.util.List;


public class TaskFlatList implements Serializable {

    private static final long serialVersionUID = 2215478037991502928L;

    private List<TaskFlat> _list;

    public TaskFlatList() {
        _list = new ArrayList<TaskFlat>();
    }

    public TaskFlatList(List<TaskFlat> list) {
        _list = list;
    }

    public List<TaskFlat> getList() {
        return _list;
    }

    public void setList(List<TaskFlat> _list) {
        this._list = _list;
    }

}