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

import android.annotation.SuppressLint;

public class UserRestResult implements Comparable<UserRestResult> {

    private long id;
    private String name;
    private String surname;
    private LevelRank sensingMostLevel;
    private LevelRank photoLevel;
    private LevelRank questionnaireLevel;
    private LevelRank activityDetectionLevel;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        if (name == null)
            throw new NullPointerException();

        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {

        if (surname == null)
            throw new NullPointerException();
        this.surname = surname;
    }

    public LevelRank getSensingMostLevel() {
        return sensingMostLevel;
    }

    public void setSensingMostLevel(LevelRank sensingMostLevel) {
        this.sensingMostLevel = sensingMostLevel;
    }

    public LevelRank getPhotoLevel() {
        return photoLevel;
    }

    public void setPhotoLevel(LevelRank photoLevel) {
        this.photoLevel = photoLevel;
    }

    public LevelRank getQuestionnaireLevel() {
        return questionnaireLevel;
    }

    public void setQuestionnaireLevel(LevelRank questionnaireLevel) {
        this.questionnaireLevel = questionnaireLevel;
    }

    public LevelRank getActivityDetectionLevel() {
        return activityDetectionLevel;
    }

    public void setActivityDetectionLevel(LevelRank activityDetectionLevel) {
        this.activityDetectionLevel = activityDetectionLevel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
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
        UserRestResult other = (UserRestResult) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        return true;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public int compareTo(UserRestResult o) {
        if (o == null) {
            return 1;
        }
        UserRestResult u = (UserRestResult) o;
        if (equals(u)) {
            return 0;
        }

        int nameOrder = getName().toUpperCase().compareTo(
                u.getName().toUpperCase());
        if (nameOrder != 0) {
            return nameOrder;
        }

        return getSurname().toUpperCase().compareTo(
                u.getSurname().toUpperCase());

    }

}