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

public class ScoreRestResult implements Comparable<ScoreRestResult> {

    private String userName;
    private long userId;
    private int scoreValue;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {

        if (userName == null)
            throw new NullPointerException();

        this.userName = userName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    @Override
    public int compareTo(ScoreRestResult o) {
        if (o == null)
            return 1;
        ScoreRestResult score = (ScoreRestResult) o;
        if (equals(o))
            return 0;
        if (scoreValue > o.scoreValue)
            return 1;
        else if (scoreValue < o.scoreValue)
            return -1;
        else {
            return userName.compareTo(score.userName);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + scoreValue;
        result = prime * result + (int) (userId ^ (userId >>> 32));
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (((Object) this).getClass() != obj.getClass())
            return false;
        ScoreRestResult other = (ScoreRestResult) obj;
        if (scoreValue != other.scoreValue)
            return false;
        if (userId != other.userId)
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }

}
