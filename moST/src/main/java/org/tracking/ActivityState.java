/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tracking;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represent a state for the Activity Recognition
 */
public class ActivityState {
    private String state;
    private String pole;
    private long timestamp;

    private double score;

    public ActivityState(String state, String pole, double score, long timestamp) {
        super();
        this.state = state;
        this.timestamp = timestamp;
        this.setPole(pole);
        this.setScore(score);
    }

    public boolean isWalking() {
        if (this.state.equals("walk"))
            return true;
        else
            return false;
    }

    public boolean isStill() {
        if (this.state.equals("still"))
            return true;
        else
            return false;
    }

    public boolean isRunning() {
        if (this.state.equals("run"))
            return true;
        else
            return false;
    }

    public boolean isOnVehicle() {
        if (this.state.equals("vehicle"))
            return true;
        else
            return false;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long from) {
        this.timestamp = from;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getPole() {
        return pole;
    }

    public void setPole(String pole) {
        this.pole = pole;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public String toString() {
        return "Activity [state=" + state + ", pole=" + pole
                + ", timestamp=" + new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date(timestamp * 1000)) + ", score=" + score + "]";
    }


}
