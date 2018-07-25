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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class StatisticsMessage implements Serializable {

    private static final long serialVersionUID = -7678989653187505735L;

    List<Properties> list;

    public StatisticsMessage() {
        list = new LinkedList<Properties>();
    }

    public List<Properties> getList() {
        return list;
    }

    public void setList(List<Properties> list) {
        this.list = list;
    }

}
