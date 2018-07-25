/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Represents a Train Line.
 * @author giuseppe giammarino
 */

package org.tracking;

public class TrainLine {
    private String lineName;
    private int lineIdOSM;

    public TrainLine(int lineIdOSM, String lineName) {
        super();
        this.lineIdOSM = lineIdOSM;
        this.lineName = lineName;
    }

    public int getLineIdOSM() {
        return lineIdOSM;
    }

    public void setLineIdOsm(int lineIdOSM) {
        this.lineIdOSM = lineIdOSM;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    @Override
    public String toString() {
        return "TrainLine [lineIdOSM=" + lineIdOSM + ", lineName=" + lineName + "]";
    }

}
